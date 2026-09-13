import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useParams } from 'react-router'
import { z } from 'zod'
import { Dialog } from '../components/Dialog'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { DangerButton, Field, PrimaryButton, SecondaryButton, TextInput } from '../components/ui'
import { CvDocumentEditor } from '../features/editor/CvDocumentEditor'
import { useCv, useUpdateCv } from '../features/cvs/cvApi'
import { useCreateLanguage, useDeleteLanguage, useLanguages } from '../features/languages/languageApi'
import { useCoverLetters, useCreateCoverLetter, useDeleteCoverLetter } from '../features/letters/letterApi'
import { RevisionPanel } from '../features/revisions/RevisionPanel'
import { useMasterRevisions, useRestoreMasterRevision } from '../features/revisions/revisionApi'
import { useT } from '../i18n'
import { toastFromError, useUiStore } from '../stores/uiStore'
import type { LanguageCode, TemplateCode } from '../types/cv'

const languageSchema = z.object({
  languageCode: z.enum(['TR', 'EN', 'DE', 'FR', 'ES']),
  name: z.string().trim().min(1).max(150),
  templateCode: z.enum(['MODERN', 'MINIMAL', 'PROFESSIONAL', 'ATS']),
})
type LanguageForm = z.infer<typeof languageSchema>

const LANGUAGE_NAMES: Record<LanguageCode, string> = {
  TR: 'Turkish',
  EN: 'English',
  DE: 'German',
  FR: 'French',
  ES: 'Spanish',
}

export function CvEditorPage() {
  const { cvId = '' } = useParams()
  const navigate = useNavigate()
  const notify = useUiStore((s) => s.notify)
  const t = useT()
  const { data: cv, isLoading, isError, refetch } = useCv(cvId)
  const updateCv = useUpdateCv(cvId)
  const { data: languages } = useLanguages(cvId)
  const { data: letters } = useCoverLetters(cvId)
  const createLetter = useCreateCoverLetter(cvId)
  const deleteLetter = useDeleteCoverLetter(cvId)
  const { data: revisions, isLoading: revisionsLoading } = useMasterRevisions(cvId)
  const restoreRevision = useRestoreMasterRevision(cvId)
  const createLanguage = useCreateLanguage(cvId)
  const deleteLanguage = useDeleteLanguage(cvId)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [deleteLanguageTarget, setDeleteLanguageTarget] = useState<{ id: string; name: string } | null>(null)
  const [deleteLetterTarget, setDeleteLetterTarget] = useState<{ id: string; title: string } | null>(null)
  const [previewTemplate, setPreviewTemplate] = useState<TemplateCode>('MODERN')
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<LanguageForm>({ defaultValues: { languageCode: 'EN', name: 'English', templateCode: 'MODERN' } })

  async function onCreateLanguage(data: LanguageForm) {
    const parsed = languageSchema.safeParse(data)
    if (!parsed.success) return
    try {
      const created = await createLanguage.mutateAsync(parsed.data)
      notify(t('editor.langCreated'), 'success')
      setDialogOpen(false)
      reset()
      navigate(`/cvs/${cvId}/languages/${created.id}`)
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function onDeleteLanguage() {
    if (!deleteLanguageTarget) return
    try {
      await deleteLanguage.mutateAsync(deleteLanguageTarget.id)
      notify(t('editor.langDeleted'), 'info')
      setDeleteLanguageTarget(null)
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function onAddLetter() {
    try {
      const created = await createLetter.mutateAsync({ languageCode: 'EN', title: 'Cover letter' })
      navigate(`/cvs/${cvId}/cover-letters/${created.id}`)
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function onDeleteLetter() {
    if (!deleteLetterTarget) return
    try {
      await deleteLetter.mutateAsync(deleteLetterTarget.id)
      notify(t('letter.deleted'), 'info')
      setDeleteLetterTarget(null)
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  if (isLoading) return <p>{t('common.loading')}</p>
  if (isError || !cv)
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm">
        <p className="font-medium text-red-700">{t('error.loadCv')}</p>
        <button onClick={() => void refetch()} className="mt-2 rounded bg-red-600 px-3 py-1 text-white">
          {t('common.retry')}
        </button>
      </div>
    )

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-sm text-ink-500">
        <Link to="/cvs" className="hover:underline">
          {t('nav.cvs')}
        </Link>
        <span>/</span>
        <span className="font-medium text-ink-900">{t('editor.masterCv')}</span>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <span className="text-sm text-ink-500">{t('editor.previewStyle')}</span>
        {(['MODERN', 'MINIMAL', 'PROFESSIONAL', 'ATS'] as TemplateCode[]).map((code) => (
          <button
            key={code}
            onClick={() => setPreviewTemplate(code)}
            className={`rounded border px-2 py-1 text-xs ${previewTemplate === code ? 'border-slate-900 bg-ink-900 text-white' : 'border-line bg-card'}`}
          >
            {code}
          </button>
        ))}
        <span className="ml-auto text-xs text-ink-300">{t('editor.previewOnly')}</span>
      </div>

      <CvDocumentEditor
        key={`${cv.id}:${cv.version}`}
        initial={{ name: cv.name, content: cv.content, sectionOrder: cv.sectionOrder, version: cv.version }}
        onSave={async (input) => {
          const saved = await updateCv.mutateAsync(input)
          return { version: saved.version }
        }}
        onReload={() => void refetch()}
        previewTemplate={previewTemplate}
        previewLanguage="EN"
      />

      <RevisionPanel
        revisions={revisions}
        isLoading={revisionsLoading}
        currentVersion={cv.version}
        previewTemplate={previewTemplate}
        previewLanguage="EN"
        restoring={restoreRevision.isPending}
        onRestore={async (revisionId, version) => {
          await restoreRevision.mutateAsync({ revisionId, version })
          void refetch()
        }}
      />

      <div className="rounded-lg border border-line bg-card p-4">
        <div className="mb-2 flex items-center justify-between">
          <h2 className="font-semibold">{t('editor.languagesTitle')}</h2>
          <PrimaryButton onClick={() => setDialogOpen(true)}>{t('editor.addLanguage')}</PrimaryButton>
        </div>
        <ul className="flex flex-col gap-1">
          {(languages ?? []).map((l: { id: string; languageCode: LanguageCode; name: string; templateCode: string }) => (
            <li key={l.id} className="flex items-center gap-3 rounded border border-line px-3 py-2 text-sm">
              <span className="rounded bg-paper px-1.5 py-0.5 font-mono text-xs">{l.languageCode}</span>
              <Link to={`/cvs/${cvId}/languages/${l.id}`} className="font-medium text-accent-800 hover:underline">
                {l.name}
              </Link>
              <span className="text-ink-300">{l.templateCode}</span>
              <DangerButton onClick={() => setDeleteLanguageTarget({ id: l.id, name: l.name })} className="ml-auto">
                {t('common.delete')}
              </DangerButton>
            </li>
          ))}
        </ul>
        {(languages?.length ?? 0) === 0 && (
          <p className="text-sm text-ink-500">{t('editor.noLanguages')}</p>
        )}
      </div>

      <div className="rounded-lg border border-line bg-card p-4">
        <div className="mb-2 flex items-center justify-between">
          <h2 className="font-semibold">{t('letter.title')}</h2>
          <PrimaryButton onClick={() => void onAddLetter()} disabled={createLetter.isPending}>
            {t('letter.add')}
          </PrimaryButton>
        </div>
        <ul className="flex flex-col gap-1">
          {(letters ?? []).map((l) => (
            <li key={l.id} className="flex items-center gap-3 rounded border border-line px-3 py-2 text-sm">
              <Link to={`/cvs/${cvId}/cover-letters/${l.id}`} className="font-medium text-accent-800 hover:underline">
                {l.title}
              </Link>
              <DangerButton onClick={() => setDeleteLetterTarget({ id: l.id, title: l.title })} className="ml-auto">
                {t('common.delete')}
              </DangerButton>
            </li>
          ))}
        </ul>
        {(letters?.length ?? 0) === 0 && (
          <p className="text-sm text-ink-500">{t('letter.empty')}</p>
        )}
      </div>

      {deleteLanguageTarget && (
        <ConfirmDialog
          title={t('confirm.deleteTitle')}
          message={`${t('editor.confirmDeleteLang')} "${deleteLanguageTarget.name}"?`}
          confirmLabel={t('common.delete')}
          pending={deleteLanguage.isPending}
          onConfirm={() => void onDeleteLanguage()}
          onCancel={() => setDeleteLanguageTarget(null)}
        />
      )}
      {deleteLetterTarget && (
        <ConfirmDialog
          title={t('confirm.deleteTitle')}
          message={`${t('letter.confirmDelete')} "${deleteLetterTarget.title}"?`}
          confirmLabel={t('common.delete')}
          pending={deleteLetter.isPending}
          onConfirm={() => void onDeleteLetter()}
          onCancel={() => setDeleteLetterTarget(null)}
        />
      )}
      {dialogOpen && (
        <Dialog title={t('editor.newLangTitle')} onClose={() => setDialogOpen(false)}>
          <form onSubmit={handleSubmit(onCreateLanguage)} className="flex flex-col gap-3">
            <Field label={t('editor.language')}>
              <select {...register('languageCode')} className="w-full rounded-md border border-line px-3 py-2 text-sm">
                {(Object.keys(LANGUAGE_NAMES) as LanguageCode[]).map((code) => (
                  <option key={code} value={code}>
                    {code} — {LANGUAGE_NAMES[code]}
                  </option>
                ))}
              </select>
            </Field>
            <Field label={t('editor.name')}>
              <TextInput {...register('name')} />
            </Field>
            {errors.name && <p className="text-sm text-red-600">{t('cvlist.nameRequired')}</p>}
            <Field label={t('editor.template')}>
              <select {...register('templateCode')} className="w-full rounded-md border border-line px-3 py-2 text-sm">
                <option value="MODERN">MODERN</option>
                <option value="MINIMAL">MINIMAL</option>
                <option value="PROFESSIONAL">PROFESSIONAL</option>
                <option value="ATS">ATS (plain)</option>
              </select>
            </Field>
            <div className="flex justify-end gap-2">
              <SecondaryButton type="button" onClick={() => setDialogOpen(false)}>
                {t('common.cancel')}
              </SecondaryButton>
              <PrimaryButton type="submit" disabled={createLanguage.isPending}>
                {createLanguage.isPending ? t('common.creating') : t('common.create')}
              </PrimaryButton>
            </div>
          </form>
        </Dialog>
      )}
    </div>
  )
}
