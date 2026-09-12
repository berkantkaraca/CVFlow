import { describe, expect, it, vi, afterEach } from 'vitest'
import { apiGet } from '../api/client'
import { ApiError } from '../types/api'

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('api client', () => {
  it('unwraps successful ApiResponse data', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () =>
        jsonResponse({ success: true, data: [{ id: '1' }], error: null, timestamp: 't' }),
      ),
    )
    const data = await apiGet<{ id: string }[]>('/cvs')
    expect(data).toEqual([{ id: '1' }])
  })

  it('maps backend errors to ApiError with code and status', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () =>
        jsonResponse(
          {
            success: false,
            data: null,
            error: { code: 'CV_NOT_FOUND', message: 'CV not found', details: null },
            timestamp: 't',
          },
          404,
        ),
      ),
    )
    try {
      await apiGet('/cvs/missing')
      expect.unreachable()
    } catch (e) {
      expect(e).toBeInstanceOf(ApiError)
      const err = e as ApiError
      expect(err.code).toBe('CV_NOT_FOUND')
      expect(err.status).toBe(404)
    }
  })
})
