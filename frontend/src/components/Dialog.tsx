import type { ReactNode } from 'react'

export function Dialog({ title, onClose, children }: { title: string; onClose: () => void; children: ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink-950/50 p-4 backdrop-blur-[2px]" onClick={onClose}>
      <div
        className="w-full max-w-md rounded-2xl border border-line bg-card p-6 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="mb-4 text-base font-semibold tracking-tight text-ink-900">{title}</h2>
        {children}
      </div>
    </div>
  )
}
