import { useState } from 'react'
import { CvPreview } from '../../components/CvPreview'
import { Dialog } from '../../components/Dialog'
import { SecondaryButton } from '../../components/ui'
import { toastFromError, useUiStore } from '../../stores/uiStore'
import type { LanguageCode, Revision, TemplateCode } from '../../types/cv'

export function RevisionPanel({
  revisions,
  isLoading,
  currentVersion,
  previewTemplate,
  previewLanguage,
  onRestore,
  restoring,
}: {
  revisions: Revision[] | undefined
  isLoading: boolean
  currentVersion: number
  previewTemplate: TemplateCode
  previewLanguage: LanguageCode
  onRestore: (revisionId: string, version: number) => Promise<unknown>
  restoring: boolean
}) {
  const notify = useUiStore((s) => s.notify)
  const [previewId, setPreviewId] = useState<string | null>(null)
  const preview = revisions?.find((r) => r.id === previewId) ?? null

  async function handleRestore(revision: Revision) {
    try {
      await onRestore(revision.id, currentVersion)
      notify(`Restored version v${revision.version}`, 'success')
    } catch (e) {
      notify(toastFromError(e, 'Restore failed'), 'error')
    }
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <h2 className="mb-2 font-semibold">Version history</h2>
      {isLoading && <p className="text-sm text-slate-500">Loading…</p>}
      {(revisions?.length ?? 0) === 0 && !isLoading && (
        <p className="text-sm text-slate-500">
          No revisions yet. Every save and AI accept creates a restorable snapshot (last 20 kept).
        </p>
      )}
      <ul className="flex flex-col gap-1">
        {(revisions ?? []).map((r) => (
          <li
            key={r.id}
            className="flex items-center gap-3 rounded border border-slate-100 px-3 py-2 text-sm"
          >
            <span className="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-xs">v{r.version}</span>
            <span className="text-slate-500">{new Date(r.createdAt).toLocaleString()}</span>
            <span className="ml-auto flex gap-2">
              <SecondaryButton onClick={() => setPreviewId(r.id)}>Preview</SecondaryButton>
              <SecondaryButton onClick={() => void handleRestore(r)} disabled={restoring}>
                Restore
              </SecondaryButton>
            </span>
          </li>
        ))}
      </ul>
      {preview && (
        <Dialog title={`Revision v${preview.version}`} onClose={() => setPreviewId(null)}>
          <div className="max-h-[60vh] overflow-y-auto">
            <CvPreview
              content={preview.content}
              sectionOrder={preview.sectionOrder}
              template={previewTemplate}
              language={previewLanguage}
            />
          </div>
          <div className="mt-3 flex justify-end">
            <SecondaryButton
              onClick={() => {
                setPreviewId(null)
                void handleRestore(preview)
              }}
              disabled={restoring}
            >
              Restore this version
            </SecondaryButton>
          </div>
        </Dialog>
      )}
    </div>
  )
}
