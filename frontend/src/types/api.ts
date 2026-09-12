export interface ApiErrorBody {
  code: string
  message: string
  details: Record<string, string> | null
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: ApiErrorBody | null
  timestamp: string
}

export class ApiError extends Error {
  code: string
  status: number
  details: Record<string, string> | null

  constructor(code: string, message: string, status: number, details: Record<string, string> | null = null) {
    super(message)
    this.code = code
    this.status = status
    this.details = details
  }
}
