import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { SecondaryButton } from '../components/ui'
import { AiFieldButtons, SuggestionPanel } from '../features/ai/SuggestionPanel'
import { CvDocumentEditor } from '../features/editor/CvDocumentEditor'
import { useLanguage, useLanguages, useUpdateLanguage } from '../features/languages/languageApi'
import { exportPdf } from '../features/pdf/pdfApi'
import { toastFromError, useUiStore } from '../stores/uiStore'
import type { TemplateCode } from '../types/cv'

export function LanguageEditorPage() {
  const { cvId = '', languageId = '' } = useParams()
  const notify = useUiStore((s) => s.notify)
  const { data: language, isLoading, isError, refetch } = useLanguage(cvId, languageId)
  const { data: languages, refetch: refetchLanguages } = useLanguages(cvId)
  const updateLanguage = useUpdateLanguage(cvId, languageId)
  const [exporting, setExporting] = useState(false)

  async function onExport() {
    if (!language) return
    setExporting(true)
    try {
      await exportPdf(cvId, language.languageCode)
      notify('PDF downloaded', 'success')
    } catch (e) {
      notify(toastFromError(e, 'PDF export failed'), 'error')
    } finally {
      setExporting(false)
    }
  }

  async function onTemplateChange(templateCode: TemplateCode) {
    if (!language) return
    try {
      await updateLanguage.mutateAsync({
        name: language.name,
        templateCode,
        content: language.content,
        sectionOrder: language.sectionOrder,
        version: language.version,
      })
      notify(`Template set to ${templateCode}`, 'success')
      void refetch()
      void refetchLanguages()
    } catch (e) {
      notify(toastFromError(e, 'Template change failed — save your edits first'), 'error')
    }
  }

  if (isLoading) return <p>Loading…</p>
  if (isError || !language)
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm">
        <p className="font-medium text-red-700">Could not load this language version.</p>
        <button onClick={() => void refetch()} className="mt-2 rounded bg-red-600 px-3 py-1 text-white">
          Retry
        </button>
      </div>
    )

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-2 text-sm text-slate-500">
        <Link to="/cvs" className="hover:underline">
          CVs
        </Link>
        <span>/</span>
        <Link to={`/cvs/${cvId}`} className="hover:underline">
          Master CV
        </Link>
        <span>/</span>
        <span className="font-medium text-slate-800">
          {language.languageCode} — {language.name}
        </span>
      </div>

      <CvDocumentEditor
        key={`${language.id}:${language.version}`}
        initial={{
          name: language.name,
          content: language.content,
          sectionOrder: language.sectionOrder,
          version: language.version,
        }}
        onSave={async (input) => {
          const saved = await updateLanguage.mutateAsync({ ...input, templateCode: language.templateCode })
          return { version: saved.version }
        }}
        onReload={() => void refetch()}
        headerActions={
          <>
            <select
              value={language.templateCode}
              onChange={(e) => void onTemplateChange(e.target.value as TemplateCode)}
              className="rounded-md border border-slate-300 px-2 py-2 text-sm"
              title="Template"
            >
              <option value="MODERN">MODERN</option>
              <option value="MINIMAL">MINIMAL</option>
              <option value="PROFESSIONAL">PROFESSIONAL</option>
            </select>
            <SecondaryButton onClick={() => void onExport()} disabled={exporting}>
              {exporting ? 'Exporting…' : '⬇ Export PDF'}
            </SecondaryButton>
          </>
        }
        fieldActions={(section, entityId, field, value) => (
          <AiFieldButtons
            languageVersionId={language.id}
            section={section}
            entityId={entityId}
            field={field}
            value={value}
          />
        )}
        previewTemplate={language.templateCode}
        previewLanguage={language.languageCode}
      />

      <SuggestionPanel
        languageVersionId={language.id}
        languages={languages ?? []}
        currentLanguageCode={language.languageCode}
        onApplied={() => {
          void refetch()
          void refetchLanguages()
        }}
      />
    </div>
  )
}
