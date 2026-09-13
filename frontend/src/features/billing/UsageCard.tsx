import { useT } from '../../i18n'
import { useUsage } from './billingApi'

export function UsageCard() {
  const t = useT()
  const { data, isLoading } = useUsage()

  if (isLoading || !data) return null
  const pct = Math.min(100, Math.round((data.aiCallsUsed / Math.max(1, data.aiCallsLimit)) * 100))
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <p className="text-xs uppercase tracking-wide text-slate-500">
        {t('billing.usage')} · {data.plan}
      </p>
      <div className="mt-1 h-2 rounded bg-slate-200">
        <div className="h-2 rounded bg-blue-600" style={{ width: `${pct}%` }} />
      </div>
      <p className="mt-1 text-sm text-slate-600">
        {data.aiCallsUsed}/{data.aiCallsLimit} {t('billing.aiCalls')} · {data.cvsUsed}/{data.cvsMax}{' '}
        {t('dashboard.masterCvs')}
      </p>
    </div>
  )
}
