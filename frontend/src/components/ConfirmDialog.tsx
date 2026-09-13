import type { ReactNode } from 'react'
import { useT } from '../i18n'
import { Dialog } from './Dialog'
import { DangerButton, SecondaryButton } from './ui'

export function IconButton({
  title,
  onClick,
  danger,
  children,
}: {
  title: string
  onClick: () => void
  danger?: boolean
  children: ReactNode
}) {
  return (
    <button
      title={title}
      aria-label={title}
      onClick={onClick}
      className={`rounded-lg p-2 transition ${
        danger
          ? 'text-red-600 hover:bg-red-50'
          : 'text-ink-500 hover:bg-paper hover:text-ink-900'
      }`}
    >
      {children}
    </button>
  )
}

export function ConfirmDialog({
  title,
  message,
  confirmLabel,
  pending,
  onConfirm,
  onCancel,
}: {
  title: string
  message: string
  confirmLabel: string
  pending?: boolean
  onConfirm: () => void
  onCancel: () => void
}) {
  const t = useT()
  return (
    <Dialog title={title} onClose={onCancel}>
      <p className="mb-5 text-sm text-ink-700">{message}</p>
      <div className="flex justify-end gap-2">
        <SecondaryButton onClick={onCancel} disabled={pending}>
          {t('common.cancel')}
        </SecondaryButton>
        <DangerButton onClick={onConfirm} disabled={pending}>
          {pending ? '…' : confirmLabel}
        </DangerButton>
      </div>
    </Dialog>
  )
}
