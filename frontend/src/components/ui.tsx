import type { InputHTMLAttributes, ReactNode, TextareaHTMLAttributes } from 'react'

export function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-[11px] font-semibold uppercase tracking-[0.08em] text-ink-500">{label}</span>
      {children}
    </label>
  )
}

const inputClass =
  'w-full rounded-lg border border-line bg-card px-3 py-2 text-sm text-ink-900 placeholder:text-ink-300 focus:border-accent-700 focus:outline-none focus:ring-2 focus:ring-accent-100'

export function TextInput(props: InputHTMLAttributes<HTMLInputElement>) {
  return <input {...props} className={`${inputClass} ${props.className ?? ''}`} />
}

export function TextArea(props: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea {...props} rows={props.rows ?? 3} className={`${inputClass} ${props.className ?? ''}`} />
}

export function PrimaryButton({
  children,
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      {...props}
      className={`rounded-lg bg-ink-900 px-4 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-ink-950 disabled:opacity-50 ${props.className ?? ''}`}
    >
      {children}
    </button>
  )
}

export function SecondaryButton({
  children,
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      {...props}
      className={`rounded-lg border border-line bg-card px-3 py-1.5 text-sm font-medium text-ink-700 transition hover:border-ink-300 hover:bg-paper disabled:opacity-50 ${props.className ?? ''}`}
    >
      {children}
    </button>
  )
}

export function DangerButton({
  children,
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      {...props}
      className={`rounded-lg border border-red-200 bg-card px-3 py-1.5 text-sm font-medium text-red-700 transition hover:bg-red-50 disabled:opacity-50 ${props.className ?? ''}`}
    >
      {children}
    </button>
  )
}

export function SectionCard({
  title,
  actions,
  children,
}: {
  title: string
  actions?: ReactNode
  children: ReactNode
}) {
  return (
    <div className="rounded-xl border border-line bg-card p-5 shadow-[0_1px_2px_rgba(20,32,46,0.05)]">
      <div className="mb-4 flex items-center justify-between border-b border-line pb-3">
        <h3 className="text-[11px] font-semibold uppercase tracking-[0.1em] text-ink-500">{title}</h3>
        {actions && <div className="flex gap-1">{actions}</div>}
      </div>
      <div className="flex flex-col gap-3">{children}</div>
    </div>
  )
}
