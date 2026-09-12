import { Link, Outlet, useLocation, useNavigate } from 'react-router'
import { useLogout, useSession } from '../features/auth/authApi'
import { useUiStore } from '../stores/uiStore'

function navClass(active: boolean): string {
  return active
    ? 'rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white'
    : 'rounded-md px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-200'
}

export function Layout() {
  const location = useLocation()
  const navigate = useNavigate()
  const toasts = useUiStore((s) => s.toasts)
  const dismiss = useUiStore((s) => s.dismiss)
  const email = useSession((s) => s.email)
  const logout = useLogout()
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register'
  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center gap-4 px-4 py-3">
          <Link to="/" className="text-lg font-bold tracking-tight">
            CVFlow
          </Link>
          {!isAuthPage && (
            <nav className="flex gap-1">
              <Link to="/" className={navClass(location.pathname === '/')}>
                Dashboard
              </Link>
              <Link to="/cvs" className={navClass(location.pathname.startsWith('/cvs'))}>
                CVs
              </Link>
            </nav>
          )}
          <div className="ml-auto flex items-center gap-3 text-sm">
            {email ? (
              <>
                <span className="text-slate-500">{email}</span>
                <button
                  onClick={() => {
                    logout()
                    navigate('/login')
                  }}
                  className="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100"
                >
                  Logout
                </button>
              </>
            ) : (
              !isAuthPage && (
                <Link to="/login" className="font-medium text-blue-700 hover:underline">
                  Log in
                </Link>
              )
            )}
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-6">
        <Outlet />
      </main>
      <div className="fixed bottom-4 right-4 flex flex-col gap-2">
        {toasts.map((t) => (
          <button
            key={t.id}
            onClick={() => dismiss(t.id)}
            className={`rounded-md px-4 py-2 text-sm shadow-lg ${
              t.kind === 'error' ? 'bg-red-600 text-white' : t.kind === 'success' ? 'bg-green-600 text-white' : 'bg-slate-800 text-white'
            }`}
          >
            {t.message}
          </button>
        ))}
      </div>
    </div>
  )
}
