import { ApiError, type ApiResponse } from '../types/api'

const BASE_URL = '/api/v1'
const TOKEN_KEY = 'cvflow.token'

export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setToken(token: string | null): void {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token)
    else localStorage.removeItem(TOKEN_KEY)
  } catch {
    /* storage unavailable */
  }
}

function authHeaders(): Record<string, string> {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

function handleUnauthorized(status: number): void {
  if (status === 401 && !window.location.pathname.startsWith('/login')) {
    setToken(null)
    window.location.assign('/login')
  }
}

async function parseJson<T>(res: Response): Promise<T> {
  if (res.status === 401) {
    handleUnauthorized(401)
  }
  const body = (await res.json()) as ApiResponse<T>
  if (!res.ok || !body.success) {
    throw new ApiError(
      body.error?.code ?? 'REQUEST_FAILED',
      body.error?.message ?? `Request failed with status ${res.status}`,
      res.status,
      body.error?.details ?? null,
    )
  }
  return body.data as T
}

export async function apiGet<T>(path: string): Promise<T> {
  return parseJson<T>(await fetch(`${BASE_URL}${path}`, { headers: authHeaders() }))
}

export async function apiPost<T>(path: string, body?: unknown): Promise<T> {
  return parseJson<T>(
    await fetch(`${BASE_URL}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: body === undefined ? undefined : JSON.stringify(body),
    }),
  )
}

export async function apiPut<T>(path: string, body: unknown): Promise<T> {
  return parseJson<T>(
    await fetch(`${BASE_URL}${path}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: JSON.stringify(body),
    }),
  )
}

export async function apiDelete(path: string): Promise<void> {
  await parseJson<unknown>(await fetch(`${BASE_URL}${path}`, { method: 'DELETE', headers: authHeaders() }))
}

export async function apiDownload(path: string, body: unknown, filename: string): Promise<void> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    try {
      await parseJson<unknown>(res.clone())
    } catch (e) {
      throw e
    }
    throw new ApiError('REQUEST_FAILED', `Request failed with status ${res.status}`, res.status)
  }
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}
