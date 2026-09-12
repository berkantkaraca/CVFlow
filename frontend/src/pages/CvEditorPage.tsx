import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useNavigate, useParams } from 'react-router'
import { z } from 'zod'
import { Dialog } from '../components/Dialog'
import { DangerButton, Field, PrimaryButton, SecondaryButton, TextInput } from '../components/ui'
import { CvDocumentEditor } from '../features/editor/CvDocumentEditor'
import { useCv, useUpdateCv } from '../features/cvs/cvApi'
import { useCreateLanguage, useDeleteLanguage, useLanguages } from '../features/languages/languageApi'
import { toastFromError, useUiStore } from '../stores/uiStore'
import type { LanguageCode, TemplateCode } from '../types/cv'

const languageSchema = z.object({
  languageCode: z.enum(['TR', 'EN']),
  name: z.string().trim().min(1, 'Name is required').max(150),
  templateCode: z.enum(['MODERN', 'MINIMAL', 'PROFESSIONAL']),
})
type LanguageForm = z.infer<typeof languageSchema>

export function CvEditorPage() {
  const { cvId = '' } = useParams()
  const navigate = useNavigate()
  const notify = useUiStore((s) => s.notify)
  const { data: cv, isLoading, refetch } = useCv(cvId)
  const updateCv = useUpdateCv(cvId)
  const { data: languages } = useLanguages(cvId)
  const createLanguage = useCreateLanguage(cvId)
  const deleteLanguage = useDeleteLanguage(cvId)
  const [dialogOpen, setDialogOpen] = useState(false)
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
      notify('Language version created', 'success')
      setDialogOpen(false)
      reset()
      navigate(`/cvs/${cvId}/languages/${created.id}`)
    } catch (e) {
      notify(toastFromError(e, 'Create failed'), 'error')
    }
  }

  async function onDeleteLanguage(id: string, name: string) {
    if (!window.confirm(`Delete "${name}"?`)) return
    try {
      await deleteLanguage.mutateAsync(id)
      notify('Language version deleted', 'info')
    } catch (e) {
      notify(toastFromError(e, 'Delete failed'), 'error')
    }
  }

  if (isLoading) return <p>Loading…</p>
  if (!cv) return <p>CV not found.</p>

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-sm text-slate-500">
        <Link to="/cvs" className="hover:underline">
          CVs
        </Link>
        <span>/</span>
        <span className="font-medium text-slate-800">Master CV</span>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <span className="text-sm text-slate-500">Preview style:</span>
        {(['MODERN', 'MINIMAL', 'PROFESSIONAL'] as TemplateCode[]).map((t) => (
          <button
            key={t}
            onClick={() => setPreviewTemplate(t)}
            className={`rounded border px-2 py-1 text-xs ${previewTemplate === t ? 'border-slate-900 bg-slate-900 text-white' : 'border-slate-300 bg-white'}`}
          >
            {t}
          </button>
        ))}
        <span className="ml-auto text-xs text-slate-400">Master has no template — preview only</span>
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

      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <div className="mb-2 flex items-center justify-between">
          <h2 className="font-semibold">Language versions</h2>
          <PrimaryButton onClick={() => setDialogOpen(true)}>+ Add language</PrimaryButton>
        </div>
        <ul className="flex flex-col gap-1">
          {(languages ?? []).map((l: { id: string; languageCode: LanguageCode; name: string; templateCode: string }) => (
            <li key={l.id} className="flex items-center gap-3 rounded border border-slate-100 px-3 py-2 text-sm">
              <span className="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-xs">{l.languageCode}</span>
              <Link to={`/cvs/${cvId}/languages/${l.id}`} className="font-medium text-blue-700 hover:underline">
                {l.name}
              </Link>
              <span className="text-slate-400">{l.templateCode}</span>
              <DangerButton onClick={() => void onDeleteLanguage(l.id, l.name)} className="ml-auto">
                Delete
              </DangerButton>
            </li>
          ))}
        </ul>
        {(languages?.length ?? 0) === 0 && (
          <p className="text-sm text-slate-500">No language versions yet. Create one to translate and export.</p>
        )}
      </div>

      {dialogOpen && (
        <Dialog title="New language version" onClose={() => setDialogOpen(false)}>
          <form onSubmit={handleSubmit(onCreateLanguage)} className="flex flex-col gap-3">
            <Field label="Language">
              <select {...register('languageCode')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
                <option value="EN">EN — English</option>
                <option value="TR">TR — Turkish</option>
              </select>
            </Field>
            <Field label="Name">
              <TextInput {...register('name')} />
            </Field>
            {errors.name && <p className="text-sm text-red-600">{errors.name.message}</p>}
            <Field label="Template">
              <select {...register('templateCode')} className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm">
                <option value="MODERN">MODERN</option>
                <option value="MINIMAL">MINIMAL</option>
                <option value="PROFESSIONAL">PROFESSIONAL</option>
              </select>
            </Field>
            <div className="flex justify-end gap-2">
              <SecondaryButton type="button" onClick={() => setDialogOpen(false)}>
                Cancel
              </SecondaryButton>
              <PrimaryButton type="submit" disabled={createLanguage.isPending}>
                {createLanguage.isPending ? 'Creating…' : 'Create'}
              </PrimaryButton>
            </div>
          </form>
        </Dialog>
      )}
    </div>
  )
}
