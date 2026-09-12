import { create } from 'zustand'

interface Toast {
  id: number
  message: string
  kind: 'info' | 'error' | 'success'
}

interface UiState {
  toasts: Toast[]
  notify: (message: string, kind?: Toast['kind']) => void
  dismiss: (id: number) => void
}

let nextId = 1

export const useUiStore = create<UiState>((set) => ({
  toasts: [],
  notify: (message, kind = 'info') => {
    const id = nextId++
    set((state) => ({ toasts: [...state.toasts, { id, message, kind }] }))
    setTimeout(() => {
      set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) }))
    }, 4000)
  },
  dismiss: (id) => set((state) => ({ toasts: state.toasts.filter((t) => t.id !== id) })),
}))

export function toastFromError(e: unknown, fallback: string): string {
  if (e !== null && typeof e === 'object' && 'code' in e && 'message' in e) {
    const err = e as { code: string; message: string }
    return `${err.code}: ${err.message}`
  }
  return fallback
}
