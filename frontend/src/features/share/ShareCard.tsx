import { useState } from 'react'
import { SecondaryButton, DangerButton } from '../../components/ui'
import { useT } from '../../i18n'
import { toastFromError, useUiStore } from '../../stores/uiStore'
import { useCreateShareLink, useRevokeShareLink, useShareLinks } from './shareApi'

export function ShareCard({ cvId, languageId }: { cvId: string; languageId: string }) {
  const t = useT()
  const notify = useUiStore((s) => s.notify)
  const { data: links, isLoading } = useShareLinks(cvId, languageId)
  const createLink = useCreateShareLink(cvId, languageId)
  const revokeLink = useRevokeShareLink(cvId, languageId)
  const [expiry, setExpiry] = useState('')

  function publicUrl(token: string): string {
    return `${window.location.origin}/api/v1/public/cv/${token}`
  }

  async function onCreate() {
    try {
      const link = await createLink.mutateAsync(expiry ? { expiresInDays: Number(expiry) } : {})
      notify(t('share.created'), 'success')
      await navigator.clipboard.writeText(publicUrl(link.token)).catch(() => undefined)
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  async function onCopy(token: string) {
    try {
      await navigator.clipboard.writeText(publicUrl(token))
      notify(t('share.copied'), 'success')
    } catch {
      notify(t('error.generic'), 'error')
    }
  }

  async function onRevoke(id: string) {
    try {
      await revokeLink.mutateAsync(id)
      notify(t('share.revoked'), 'info')
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <h2 className="mb-2 font-semibold">{t('share.title')}</h2>
      <p className="mb-2 text-sm text-slate-500">{t('share.hint')}</p>
      <div className="mb-2 flex gap-2">
        <select
          value={expiry}
          onChange={(e) => setExpiry(e.target.value)}
          className="rounded-md border border-slate-300 px-2 py-1.5 text-sm"
        >
          <option value="">{t('share.never')}</option>
          <option value="7">{t('share.days7')}</option>
          <option value="30">{t('share.days30')}</option>
        </select>
        <SecondaryButton onClick={() => void onCreate()} disabled={createLink.isPending}>
          {t('share.create')}
        </SecondaryButton>
      </div>
      {isLoading && <p className="text-sm text-slate-500">{t('common.loading')}</p>}
      <ul className="flex flex-col gap-1">
        {(links ?? []).map((link) => (
          <li
            key={link.id}
            className="flex items-center gap-2 rounded border border-slate-100 px-3 py-2 font-mono text-xs"
          >
            <span className="truncate">{publicUrl(link.token)}</span>
            <span className="ml-auto flex shrink-0 gap-2">
              <SecondaryButton onClick={() => void onCopy(link.token)}>{t('share.copy')}</SecondaryButton>
              <DangerButton onClick={() => void onRevoke(link.id)}>{t('share.revoke')}</DangerButton>
            </span>
          </li>
        ))}
      </ul>
      {(links?.length ?? 0) === 0 && !isLoading && (
        <p className="text-sm text-slate-500">{t('share.empty')}</p>
      )}
    </div>
  )
}
