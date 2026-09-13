import { useState } from 'react'
import { useT } from '../../i18n'
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
  const t = useT()
  const [previewId, setPreviewId] = useState<string | null>(null)
  const preview = revisions?.find((r) => r.id === previewId) ?? null

  async function handleRestore(revision: Revision) {
    try {
      await onRestore(revision.id, currentVersion)
      notify(`${t('history.restored')} v${revision.version}`, 'success')
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <h2 className="mb-2 font-semibold">{t('history.title')}</h2>
      {isLoading && <p className="text-sm text-slate-500">{t('common.loading')}</p>}
      {(revisions?.length ?? 0) === 0 && !isLoading && (
        <p className="text-sm text-slate-500">
          {t('history.empty')}
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
              <SecondaryButton onClick={() => setPreviewId(r.id)}>{t('history.preview')}</SecondaryButton>
              <SecondaryButton onClick={() => void handleRestore(r)} disabled={restoring}>
                {t('history.restore')}
              </SecondaryButton>
            </span>
          </li>
        ))}
      </ul>
      {preview && (
        <Dialog title={`v${preview.version}`} onClose={() => setPreviewId(null)}>
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
              {t('history.restoreThis')}
            </SecondaryButton>
          </div>
        </Dialog>
      )}
    </div>
  )
}
