import { Link, Outlet, useLocation, useNavigate } from 'react-router'
import { useLogout, useSession } from '../features/auth/authApi'
import { useLocaleStore, useT } from '../i18n'
import { useUiStore } from '../stores/uiStore'

function navClass(active: boolean): string {
  return active
    ? 'border-b-2 border-accent-700 pb-3 text-sm font-semibold text-ink-900'
    : 'border-b-2 border-transparent pb-3 text-sm font-medium text-ink-500 transition hover:text-ink-900'
}

const toastClass: Record<string, string> = {
  error: 'border-red-200 bg-white text-red-800',
  success: 'border-accent-100 bg-white text-accent-800',
  info: 'border-line bg-ink-900 text-white',
}

export function Layout() {
  const location = useLocation()
  const navigate = useNavigate()
  const toasts = useUiStore((s) => s.toasts)
  const dismiss = useUiStore((s) => s.dismiss)
  const email = useSession((s) => s.email)
  const logout = useLogout()
  const t = useT()
  const locale = useLocaleStore((s) => s.locale)
  const setLocale = useLocaleStore((s) => s.setLocale)
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register'
  return (
    <div className="min-h-screen bg-paper text-ink-900">
      <header className="sticky top-0 z-40 border-b border-line bg-card/95 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-end gap-8 px-6 pt-4">
          <Link to="/" className="flex items-center gap-2.5 pb-3">
            <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-ink-900 text-sm font-bold text-white">
              C
            </span>
            <span className="text-[17px] font-bold tracking-tight">
              CVFlow
            </span>
          </Link>
          {!isAuthPage && (
            <nav className="flex gap-6">
              <Link to="/" className={navClass(location.pathname === '/')}>
                {t('nav.dashboard')}
              </Link>
              <Link to="/cvs" className={navClass(location.pathname.startsWith('/cvs'))}>
                {t('nav.cvs')}
              </Link>
            </nav>
          )}
          <div className="ml-auto flex items-center gap-3 pb-2.5 text-sm">
            <button
              onClick={() => setLocale(locale === 'tr' ? 'en' : 'tr')}
              className="rounded-md px-2 py-1.5 text-xs font-semibold text-ink-500 transition hover:bg-paper hover:text-ink-900"
              title="Dil / Language"
            >
              {locale === 'tr' ? 'EN' : 'TR'}
            </button>
            {email ? (
              <>
                <span className="hidden max-w-48 truncate text-ink-500 sm:inline">{email}</span>
                <button
                  onClick={() => {
                    logout()
                    navigate('/login')
                  }}
                  className="rounded-lg border border-line px-3 py-1.5 font-medium text-ink-700 transition hover:border-ink-300 hover:bg-paper"
                >
                  {t('nav.logout')}
                </button>
              </>
            ) : (
              !isAuthPage && (
                <Link to="/login" className="font-semibold text-accent-800 hover:underline">
                  {t('nav.login')}
                </Link>
              )
            )}
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-6 py-8">
        <Outlet />
      </main>
      <div className="fixed bottom-5 right-5 z-50 flex w-80 flex-col gap-2">
        {toasts.map((toast) => (
          <button
            key={toast.id}
            onClick={() => dismiss(toast.id)}
            className={`rounded-xl border px-4 py-3 text-left text-sm shadow-lg transition ${toastClass[toast.kind] ?? toastClass.info}`}
          >
            {toast.message}
          </button>
        ))}
      </div>
    </div>
  )
}
