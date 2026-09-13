import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { DangerButton, Field, PrimaryButton, SecondaryButton, TextArea, TextInput } from '../components/ui'
import { useCoverLetterDraft } from '../features/ai/tailorApi'
import {
  exportCoverLetterPdf,
  useCoverLetter,
  useDeleteCoverLetter,
  useUpdateCoverLetter,
} from '../features/letters/letterApi'
import { useT } from '../i18n'
import { toastFromError, useUiStore } from '../stores/uiStore'

export function CoverLetterEditorPage() {
  const { cvId = '', letterId = '' } = useParams()
  const navigate = useNavigate()
  const notify = useUiStore((s) => s.notify)
  const t = useT()
  const { data: letter, isLoading, isError, refetch } = useCoverLetter(cvId, letterId)
  const updateLetter = useUpdateCoverLetter(cvId, letterId)
  const deleteLetter = useDeleteCoverLetter(cvId)
  const draft = useCoverLetterDraft()
  const [saving, setSaving] = useState(false)
  const [dirty, setDirty] = useState(false)
  const [form, setForm] = useState({ title: '', recipient: '', company: '', position: '', body: '' })
  const [initializedFor, setInitializedFor] = useState<string | null>(null)
  const [jobDescription, setJobDescription] = useState('')
  const [draftBody, setDraftBody] = useState<string | null>(null)
  const [exporting, setExporting] = useState(false)

  if (letter && initializedFor !== `${letter.id}:${letter.version}`) {
    setForm({
      title: letter.title ?? '',
      recipient: letter.recipient ?? '',
      company: letter.company ?? '',
      position: letter.position ?? '',
      body: letter.body ?? '',
    })
    setInitializedFor(`${letter.id}:${letter.version}`)
    setDirty(false)
  }

  async function onSave() {
    if (!letter || saving) return
    setSaving(true)
    try {
      await updateLetter.mutateAsync({ ...form, version: letter.version })
      notify(t('letter.saved'), 'success')
      void refetch()
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    } finally {
      setSaving(false)
    }
  }

  function edit(patch: Partial<typeof form>) {
    setForm((f) => ({ ...f, ...patch }))
    setDirty(true)
  }

  async function onGenerate() {
    if (!letter) return
    try {
      const result = await draft.mutateAsync({
        masterCvId: cvId,
        languageCode: letter.languageCode,
        jobDescription: jobDescription.trim() || undefined,
      })
      setDraftBody(result.body)
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function onDelete() {
    if (!window.confirm(t('letter.confirmDelete'))) return
    try {
      await deleteLetter.mutateAsync(letterId)
      notify(t('letter.deleted'), 'info')
      navigate(`/cvs/${cvId}`)
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function onExport() {
    try {
      setExporting(true)
      await exportCoverLetterPdf(cvId, letterId)
      notify(t('letter.pdfDownloaded'), 'success')
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    } finally {
      setExporting(false)
    }
  }

  if (isLoading) return <p>{t('common.loading')}</p>
  if (isError || !letter)
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm">
        <p className="font-medium text-red-700">{t('letter.loadFailed')}</p>
        <button onClick={() => void refetch()} className="mt-2 rounded bg-red-600 px-3 py-1 text-white">
          {t('common.retry')}
        </button>
      </div>
    )

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-sm text-slate-500">
        <Link to="/cvs" className="hover:underline">
          {t('nav.cvs')}
        </Link>
        <span>/</span>
        <Link to={`/cvs/${cvId}`} className="hover:underline">
          {t('editor.masterCv')}
        </Link>
        <span>/</span>
        <span className="font-medium text-slate-800">{letter.title}</span>
      </div>

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <div className="grid grid-cols-2 gap-3">
          <Field label={t('letter.title')}>
            <TextInput value={form.title} onChange={(e) => edit({ title: e.target.value })} />
          </Field>
          <Field label={t('letter.language')}>
            <TextInput value={letter.languageCode} disabled />
          </Field>
          <Field label={t('letter.recipient')}>
            <TextInput value={form.recipient} onChange={(e) => edit({ recipient: e.target.value })} />
          </Field>
          <Field label={t('letter.company')}>
            <TextInput value={form.company} onChange={(e) => edit({ company: e.target.value })} />
          </Field>
          <Field label={t('letter.position')}>
            <TextInput value={form.position} onChange={(e) => edit({ position: e.target.value })} />
          </Field>
        </div>
        <div className="mt-3">
          <Field label={t('letter.body')}>
            <TextArea value={form.body} rows={12} onChange={(e) => edit({ body: e.target.value })} />
          </Field>
        </div>
        <div className="mt-3 flex gap-2">
          <PrimaryButton onClick={() => void onSave()} disabled={saving || !dirty}>
            {saving ? t('common.saving') : t('common.save')}
          </PrimaryButton>
          <SecondaryButton onClick={() => void onExport()} disabled={exporting}>
            {exporting ? t('editor.exporting') : t('letter.exportPdf')}
          </SecondaryButton>
          <DangerButton onClick={() => void onDelete()} className="ml-auto">
            {t('common.delete')}
          </DangerButton>
        </div>
      </div>

      <div className="rounded-lg border border-violet-200 bg-violet-50 p-4">
        <h2 className="mb-2 font-semibold text-violet-800">{t('letter.aiTitle')}</h2>
        <Field label={t('letter.jobDescription')}>
          <TextArea
            value={jobDescription}
            rows={4}
            placeholder={t('letter.jobDescriptionPh')}
            onChange={(e) => setJobDescription(e.target.value)}
          />
        </Field>
        <div className="mt-2">
          <SecondaryButton onClick={() => void onGenerate()} disabled={draft.isPending}>
            {draft.isPending ? t('letter.generating') : t('letter.generate')}
          </SecondaryButton>
        </div>
        {draftBody !== null && (
          <div className="mt-3 rounded border border-violet-200 bg-white p-3 text-sm">
            <p className="mb-2 whitespace-pre-wrap">{draftBody}</p>
            <SecondaryButton
              onClick={() => {
                edit({ body: draftBody })
                setDraftBody(null)
                notify(t('letter.inserted'), 'success')
              }}
            >
              {t('letter.useText')}
            </SecondaryButton>
          </div>
        )}
      </div>
    </div>
  )
}
