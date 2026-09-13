import { useT } from '../../i18n'
import { useAtsScore } from '../ats/atsApi'

export function AtsCard({ cvId, languageId }: { cvId: string; languageId: string }) {
  const t = useT()
  const { data, isLoading } = useAtsScore(cvId, languageId)

  function color(score: number): string {
    if (score >= 80) return 'text-green-700'
    if (score >= 50) return 'text-amber-600'
    return 'text-red-600'
  }

  return (
    <div className="rounded-lg border border-line bg-card p-4">
      <h2 className="mb-2 font-semibold">{t('ats.title')}</h2>
      {isLoading && <p className="text-sm text-ink-500">{t('common.loading')}</p>}
      {data && (
        <>
          <p className={`text-3xl font-bold ${color(data.score)}`}>
            {data.score}
            <span className="text-sm font-normal text-ink-300">/100</span>
          </p>
          <ul className="mt-2 flex flex-col gap-1">
            {data.checks.map((check) => (
              <li key={check.code} className="flex gap-2 text-sm">
                <span>{check.passed ? '✅' : '⬜'}</span>
                <span className={check.passed ? '' : 'text-ink-500'}>{check.label}</span>
                <span className="ml-auto text-xs text-ink-300">{check.detail}</span>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  )
}
