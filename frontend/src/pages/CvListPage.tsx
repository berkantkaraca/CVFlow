import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router'
import { z } from 'zod'
import { Dialog } from '../components/Dialog'
import { DangerButton, Field, PrimaryButton, SecondaryButton, TextInput } from '../components/ui'
import { useCreateCv, useCvs, useDeleteCv, useDuplicateCv } from '../features/cvs/cvApi'
import { toastFromError, useUiStore } from '../stores/uiStore'

const createSchema = z.object({ name: z.string().trim().min(1, 'Name is required').max(150) })
type CreateForm = z.infer<typeof createSchema>

export function CvListPage() {
  const { data: cvs, isLoading, refetch } = useCvs()
  const createCv = useCreateCv()
  const deleteCv = useDeleteCv()
  const duplicateCv = useDuplicateCv()
  const notify = useUiStore((s) => s.notify)
  const [dialogOpen, setDialogOpen] = useState(false)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateForm>()

  async function onCreate(data: CreateForm) {
    const parsed = createSchema.safeParse(data)
    if (!parsed.success) return
    try {
      await createCv.mutateAsync(parsed.data)
      notify('CV created', 'success')
      setDialogOpen(false)
      reset()
    } catch (e) {
      notify(toastFromError(e, 'Create failed'), 'error')
    }
  }

  async function onDelete(id: string, name: string) {
    if (!window.confirm(`Delete "${name}"?`)) return
    try {
      await deleteCv.mutateAsync(id)
      notify('CV deleted', 'info')
    } catch (e) {
      notify(toastFromError(e, 'Delete failed'), 'error')
    }
  }

  async function onDuplicate(id: string) {
    try {
      await duplicateCv.mutateAsync(id)
      notify('CV duplicated', 'success')
      void refetch()
    } catch (e) {
      notify(toastFromError(e, 'Duplicate failed'), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Master CVs</h1>
        <PrimaryButton onClick={() => setDialogOpen(true)}>+ New CV</PrimaryButton>
      </div>
      {isLoading && <p>Loading…</p>}
      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-2">Name</th>
              <th className="px-4 py-2">Updated</th>
              <th className="px-4 py-2">Version</th>
              <th className="px-4 py-2 text-right">Actions</th>
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
                  <SecondaryButton onClick={() => void onDuplicate(cv.id)}>Duplicate</SecondaryButton>
                  <DangerButton onClick={() => void onDelete(cv.id, cv.name)}>Delete</DangerButton>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {(cvs?.length ?? 0) === 0 && !isLoading && (
          <p className="px-4 py-6 text-center text-sm text-slate-500">No CVs yet. Create your first Master CV.</p>
        )}
      </div>
      {dialogOpen && (
        <Dialog title="New Master CV" onClose={() => setDialogOpen(false)}>
          <form onSubmit={handleSubmit(onCreate)} className="flex flex-col gap-3">
            <Field label="Name">
              <TextInput {...register('name')} placeholder="Backend Developer CV" autoFocus />
            </Field>
            {errors.name && <p className="text-sm text-red-600">{errors.name.message}</p>}
            <div className="flex justify-end gap-2">
              <SecondaryButton type="button" onClick={() => setDialogOpen(false)}>
                Cancel
              </SecondaryButton>
              <PrimaryButton type="submit" disabled={createCv.isPending}>
                {createCv.isPending ? 'Creating…' : 'Create'}
              </PrimaryButton>
            </div>
          </form>
        </Dialog>
      )}
    </div>
  )
}
