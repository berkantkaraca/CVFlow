import { useMutation } from '@tanstack/react-query'
import { create } from 'zustand'
import { apiPost, getToken, setToken } from '../../api/client'

export interface AuthResponse {
  token: string
  userId: string
  email: string
}

interface SessionState {
  email: string | null
  setSession: (email: string | null, token: string | null) => void
}

const EMAIL_KEY = 'cvflow.email'

function readEmail(): string | null {
  try {
    return localStorage.getItem(EMAIL_KEY)
  } catch {
    return null
  }
}

export const useSession = create<SessionState>((set) => ({
  email: typeof window === 'undefined' ? null : readEmail(),
  setSession: (email, token) => {
    setToken(token)
    try {
      if (email) localStorage.setItem(EMAIL_KEY, email)
      else localStorage.removeItem(EMAIL_KEY)
    } catch {
      /* storage unavailable */
    }
    set({ email })
  },
}))

export function isAuthenticated(): boolean {
  return getToken() !== null
}

export function useRegister() {
  const setSession = useSession((s) => s.setSession)
  return useMutation({
    mutationFn: (input: { email: string; password: string }) =>
      apiPost<AuthResponse>('/auth/register', input),
    onSuccess: (data) => setSession(data.email, data.token),
  })
}

export function useLogin() {
  const setSession = useSession((s) => s.setSession)
  return useMutation({
    mutationFn: (input: { email: string; password: string }) =>
      apiPost<AuthResponse>('/auth/login', input),
    onSuccess: (data) => setSession(data.email, data.token),
  })
}

export function useLogout() {
  const setSession = useSession((s) => s.setSession)
  return () => setSession(null, null)
}
