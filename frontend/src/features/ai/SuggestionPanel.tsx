import { useState } from 'react'
import { useT } from '../../i18n'
import { SecondaryButton } from '../../components/ui'
import { useUiStore, toastFromError } from '../../stores/uiStore'
import type { CvSection, LanguageVersionSummary } from '../../types/cv'
import {
  useAcceptAll,
  useAcceptSuggestion,
  useImprove,
  useRejectSuggestion,
  useRewrite,
  useSuggestions,
  useTranslate,
} from './aiApi'
import { useTailor } from './tailorApi'

export function AiFieldButtons({
  languageVersionId,
  section,
  entityId,
  field,
  value,
}: {
  languageVersionId: string
  section: CvSection
  entityId: string | null
  field: string
  value: string
}) {
  const notify = useUiStore((s) => s.notify)
  const t = useT()
  const improve = useImprove(languageVersionId)
  const rewrite = useRewrite(languageVersionId)

  async function run(kind: 'improve' | 'rewrite') {
    try {
      const mutate = kind === 'improve' ? improve.mutateAsync : rewrite.mutateAsync
      await mutate({ languageVersionId, section, entityId, field, content: value })
      notify(`${t(kind === 'improve' ? 'ai.improve' : 'ai.rewrite')} — ${t('ai.suggestionCreated')}`, 'success')
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  const busy = improve.isPending || rewrite.isPending
  return (
    <>
      <button
        onClick={() => void run('improve')}
        disabled={busy}
        title="AI Improve"
        className="rounded border border-slate-300 px-2 py-0.5 text-xs text-slate-600 hover:bg-slate-100 disabled:opacity-50"
      >
        {t('ai.improve')}
      </button>
      <button
        onClick={() => void run('rewrite')}
        disabled={busy}
        title="AI Rewrite"
        className="rounded border border-slate-300 px-2 py-0.5 text-xs text-slate-600 hover:bg-slate-100 disabled:opacity-50"
      >
        {t('ai.rewrite')}
      </button>
    </>
  )
}

export function SuggestionPanel({
  languageVersionId,
  languages,
  currentLanguageCode,
  onApplied,
}: {
  languageVersionId: string
  languages: LanguageVersionSummary[]
  currentLanguageCode: string
  onApplied: () => void
}) {
  const notify = useUiStore((s) => s.notify)
  const t = useT()
  const { data: suggestions, isLoading, refetch } = useSuggestions(languageVersionId, 'PENDING')
  const accept = useAcceptSuggestion(languageVersionId)
  const reject = useRejectSuggestion(languageVersionId)
  const acceptAll = useAcceptAll(languageVersionId)
  const translate = useTranslate()
  const tailor = useTailor()
  const [sourceId, setSourceId] = useState('')
  const [tailorOpen, setTailorOpen] = useState(false)
  const [jobDescription, setJobDescription] = useState('')

  const others = languages.filter((l) => l.id !== languageVersionId)

  async function handleAccept(id: string) {
    try {
      await accept.mutateAsync(id)
      notify(t('ai.accepted'), 'success')
      onApplied()
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function handleReject(id: string) {
    try {
      await reject.mutateAsync(id)
      notify(t('ai.rejected'), 'info')
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function handleAcceptAll() {
    try {
      const result = await acceptAll.mutateAsync()
      notify(`${result.length} ${t('ai.acceptedAll')}`, 'success')
      onApplied()
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function handleTranslate() {
    if (!sourceId) return
    try {
      const result = await translate.mutateAsync({
        sourceLanguageVersionId: sourceId,
        targetLanguageVersionId: languageVersionId,
      })
      notify(`${result.length} ${t('ai.translated')}`, 'success')
      void refetch()
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function handleTailor() {
    if (!jobDescription.trim()) return
    try {
      const result = await tailor.mutateAsync({ languageVersionId, jobDescription: jobDescription.trim() })
      notify(`${result.length} ${t('ai.tailored')}`, 'success')
      setTailorOpen(false)
      void refetch()
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="rounded-lg border border-slate-200 bg-white p-4">
        <h3 className="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-700">
          {t('ai.translateTitle')}
        </h3>
        <div className="flex gap-2">
          <select
            value={sourceId}
            onChange={(e) => setSourceId(e.target.value)}
            className="flex-1 rounded-md border border-slate-300 px-2 py-1.5 text-sm"
          >
            <option value="">{t('ai.fromLanguage')}</option>
            {others.map((l) => (
              <option key={l.id} value={l.id}>
                {l.languageCode} — {l.name}
              </option>
            ))}
          </select>
          <SecondaryButton onClick={() => void handleTranslate()} disabled={!sourceId || translate.isPending}>
            {translate.isPending ? t('ai.translating') : `${t('ai.translateTo')} ${currentLanguageCode}`}
          </SecondaryButton>
          <SecondaryButton onClick={() => setTailorOpen((v) => !v)}>
            {t('ai.tailor')}
          </SecondaryButton>
        </div>
        {tailorOpen && (
          <div className="mt-2 flex flex-col gap-2">
            <textarea
              value={jobDescription}
              onChange={(e) => setJobDescription(e.target.value)}
              rows={4}
              placeholder={t('ai.jobDescriptionPh')}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm"
            />
            <SecondaryButton
              onClick={() => void handleTailor()}
              disabled={!jobDescription.trim() || tailor.isPending}
            >
              {tailor.isPending ? t('ai.tailoring') : t('ai.tailorRun')}
            </SecondaryButton>
          </div>
        )}
      </div>

      <div className="rounded-lg border border-violet-200 bg-violet-50 p-4">
        <div className="mb-2 flex items-center justify-between">
          <h3 className="text-sm font-semibold uppercase tracking-wide text-violet-800">
            {t('ai.pending')} ({suggestions?.length ?? 0})
          </h3>
          {(suggestions?.length ?? 0) > 0 && (
            <SecondaryButton onClick={() => void handleAcceptAll()} disabled={acceptAll.isPending}>
              {t('ai.acceptAll')}
            </SecondaryButton>
          )}
        </div>
        {isLoading && <p className="text-sm text-slate-500">{t('common.loading')}</p>}
        {(suggestions ?? []).map((s) => (
          <div key={s.id} className="mb-2 rounded border border-violet-200 bg-white p-3 text-sm">
            <div className="mb-1 flex gap-2 text-xs text-slate-500">
              <span className="rounded bg-violet-100 px-1.5 py-0.5 font-medium text-violet-700">{s.operation}</span>
              <span>
                {s.payload.section} · {s.payload.field}
              </span>
            </div>
            <p className="text-slate-500 line-through">{s.payload.original || '(empty)'}</p>
            <p className="font-medium text-green-700">{s.payload.suggested || '(empty)'}</p>
            <div className="mt-2 flex gap-2">
              <SecondaryButton onClick={() => void handleAccept(s.id)} disabled={accept.isPending}>
                {t('ai.accept')}
              </SecondaryButton>
              <SecondaryButton onClick={() => void handleReject(s.id)} disabled={reject.isPending}>
                {t('ai.reject')}
              </SecondaryButton>
            </div>
          </div>
        ))}
        {(suggestions?.length ?? 0) === 0 && !isLoading && (
          <p className="text-sm text-slate-500">{t('ai.noPending')}</p>
        )}
      </div>
    </div>
  )
}
