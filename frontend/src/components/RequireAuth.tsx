import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { isAuthenticated } from '../features/auth/authApi'

export function RequireAuth({ children }: { children: ReactNode }) {
  const location = useLocation()
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return children
}
