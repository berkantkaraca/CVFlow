import React, { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router'
import { z } from 'zod'
import { Dialog } from '../components/Dialog'
import { DangerButton, Field, PrimaryButton, SecondaryButton, TextInput } from '../components/ui'
import { useCreateCv, useCvs, useDeleteCv, useDuplicateCv } from '../features/cvs/cvApi'
import { downloadBackup, useImportBackup } from '../features/backup/backupApi'
import { useT } from '../i18n'
import { toastFromError, useUiStore } from '../stores/uiStore'

const createSchema = z.object({ name: z.string().trim().min(1).max(150) })
type CreateForm = z.infer<typeof createSchema>

export function CvListPage() {
  const { data: cvs, isLoading, refetch } = useCvs()
  const createCv = useCreateCv()
  const deleteCv = useDeleteCv()
  const duplicateCv = useDuplicateCv()
  const importBackup = useImportBackup()
  const notify = useUiStore((s) => s.notify)
  const t = useT()
  const [dialogOpen, setDialogOpen] = useState(false)
  const fileInputRef = React.useRef<HTMLInputElement>(null)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateForm>()

  async function onCreate(data: CreateForm) {
    const parsed = createSchema.safeParse(data)
    if (!parsed.success) {
      notify(t('cvlist.nameRequired'), 'error')
      return
    }
    try {
      await createCv.mutateAsync(parsed.data)
      notify(t('cvlist.created'), 'success')
      setDialogOpen(false)
      reset()
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function onDelete(id: string, name: string) {
    if (!window.confirm(`${t('cvlist.confirmDelete')} "${name}"?`)) return
    try {
      await deleteCv.mutateAsync(id)
      notify(t('cvlist.deleted'), 'info')
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function onDuplicate(id: string) {
    try {
      await duplicateCv.mutateAsync(id)
      notify(t('cvlist.duplicated'), 'success')
      void refetch()
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">{t('cvlist.title')}</h1>
        <div className="flex gap-2">
          <SecondaryButton
            onClick={() => {
              downloadBackup()
                .then(() => notify(t('backup.downloaded'), 'success'))
                .catch(() => notify(t('backup.exportFailed'), 'error'))
            }}
          >
            {t('backup.backup')}
          </SecondaryButton>
          <SecondaryButton onClick={() => fileInputRef.current?.click()} disabled={importBackup.isPending}>
            {t('backup.import')}
          </SecondaryButton>
          <input
            ref={fileInputRef}
            type="file"
            accept="application/json"
            className="hidden"
            onChange={(e) => {
              const file = e.target.files?.[0]
              e.target.value = ''
              if (!file) return
              const reader = new FileReader()
              reader.onload = () => {
                try {
                  const document = JSON.parse(String(reader.result))
                  importBackup.mutate(document, {
                    onSuccess: (result) =>
                      notify(
                        `${t('backup.imported')} ${result.importedCvs} ${t('backup.cvs')}, ${result.importedLanguages} ${t('backup.languages')}`,
                        'success',
                      ),
                    onError: () => notify(t('backup.importFailed'), 'error'),
                  })
                } catch {
                  notify(t('backup.invalidJson'), 'error')
                }
              }
              reader.readAsText(file)
            }}
          />
          <PrimaryButton onClick={() => setDialogOpen(true)}>{t('cvlist.new')}</PrimaryButton>
        </div>
      </div>
      {isLoading && <p>{t('common.loading')}</p>}
      <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-2">{t('cvlist.name')}</th>
              <th className="px-4 py-2">{t('cvlist.updated')}</th>
              <th className="px-4 py-2">{t('cvlist.version')}</th>
              <th className="px-4 py-2 text-right">{t('cvlist.actions')}</th>
            </tr>
          </thead>
          <tbody>
            {(cvs ?? []).map((cv) => (
              <tr key={cv.id} className="border-t border-slate-100">
                <td className="px-4 py-2 font-medium">
                  <Link to={`/cvs/${cv.id}`} className="text-blue-700 hover:underline">
                    {cv.name}
                  </Link>
                </td>
                <td className="px-4 py-2 text-slate-500">{new Date(cv.updatedAt).toLocaleString()}</td>
                <td className="px-4 py-2 text-slate-500">v{cv.version}</td>
                <td className="flex justify-end gap-2 px-4 py-2">
                  <SecondaryButton onClick={() => void onDuplicate(cv.id)}>{t('cvlist.duplicate')}</SecondaryButton>
                  <DangerButton onClick={() => void onDelete(cv.id, cv.name)}>{t('common.delete')}</DangerButton>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {(cvs?.length ?? 0) === 0 && !isLoading && (
          <p className="px-4 py-6 text-center text-sm text-slate-500">{t('cvlist.empty')}</p>
        )}
      </div>
      {dialogOpen && (
        <Dialog title={t('cvlist.dialogTitle')} onClose={() => setDialogOpen(false)}>
          <form onSubmit={handleSubmit(onCreate)} className="flex flex-col gap-3">
            <Field label={t('cvlist.nameLabel')}>
              <TextInput {...register('name')} placeholder={t('cvlist.namePlaceholder')} autoFocus />
            </Field>
            {errors.name && <p className="text-sm text-red-600">{t('cvlist.nameRequired')}</p>}
            <div className="flex justify-end gap-2">
              <SecondaryButton type="button" onClick={() => setDialogOpen(false)}>
                {t('common.cancel')}
              </SecondaryButton>
              <PrimaryButton type="submit" disabled={createCv.isPending}>
                {createCv.isPending ? t('common.creating') : t('common.create')}
              </PrimaryButton>
            </div>
          </form>
        </Dialog>
      )}
    </div>
  )
}
