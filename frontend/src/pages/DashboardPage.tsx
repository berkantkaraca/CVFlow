import { Link } from 'react-router'
import { useCvs } from '../features/cvs/cvApi'

export function DashboardPage() {
  const { data: cvs, isLoading } = useCvs()
  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-slate-600">Write your CV once. Generate every language and version automatically.</p>
      </div>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <p className="text-xs uppercase tracking-wide text-slate-500">Master CVs</p>
          <p className="text-3xl font-bold">{isLoading ? '…' : (cvs?.length ?? 0)}</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <p className="text-xs uppercase tracking-wide text-slate-500">Languages</p>
          <p className="text-3xl font-bold">TR · EN</p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <p className="text-xs uppercase tracking-wide text-slate-500">AI</p>
          <p className="text-3xl font-bold">Translate · Improve · Rewrite</p>
        </div>
      </div>
      <div className="flex gap-2">
        <Link to="/cvs" className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700">
          Manage CVs
        </Link>
      </div>
      {cvs && cvs.length > 0 && (
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <h2 className="mb-2 font-semibold">Recent</h2>
          <ul className="flex flex-col gap-1">
            {cvs.slice(0, 5).map((cv) => (
              <li key={cv.id}>
                <Link to={`/cvs/${cv.id}`} className="text-sm text-blue-700 hover:underline">
                  {cv.name}
                </Link>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
