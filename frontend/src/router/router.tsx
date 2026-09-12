import { createBrowserRouter } from 'react-router'
import { Layout } from '../components/Layout'
import { RequireAuth } from '../components/RequireAuth'
import { CvEditorPage } from '../pages/CvEditorPage'
import { CvListPage } from '../pages/CvListPage'
import { DashboardPage } from '../pages/DashboardPage'
import { LanguageEditorPage } from '../pages/LanguageEditorPage'
import { LoginPage } from '../pages/LoginPage'
import { RegisterPage } from '../pages/RegisterPage'

function protectedElement(element: React.ReactElement) {
  return <RequireAuth>{element}</RequireAuth>
}

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
      { path: '/', element: protectedElement(<DashboardPage />) },
      { path: '/cvs', element: protectedElement(<CvListPage />) },
      { path: '/cvs/:cvId', element: protectedElement(<CvEditorPage />) },
      { path: '/cvs/:cvId/languages/:languageId', element: protectedElement(<LanguageEditorPage />) },
    ],
  },
])
