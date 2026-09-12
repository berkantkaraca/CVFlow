import { ApiError, type ApiResponse } from '../types/api'

const BASE_URL = '/api/v1'

async function parseJson<T>(res: Response): Promise<T> {
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
  return parseJson<T>(await fetch(`${BASE_URL}${path}`))
}

export async function apiPost<T>(path: string, body?: unknown): Promise<T> {
  return parseJson<T>(
    await fetch(`${BASE_URL}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    }),
  )
}

export async function apiPut<T>(path: string, body: unknown): Promise<T> {
  return parseJson<T>(
    await fetch(`${BASE_URL}${path}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }),
  )
}

export async function apiDelete(path: string): Promise<void> {
  await parseJson<unknown>(await fetch(`${BASE_URL}${path}`, { method: 'DELETE' }))
}

export async function apiDownload(path: string, body: unknown, filename: string): Promise<void> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
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
