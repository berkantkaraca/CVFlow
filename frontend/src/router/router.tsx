import { createBrowserRouter } from 'react-router'
import { Layout } from '../components/Layout'
import { CvEditorPage } from '../pages/CvEditorPage'
import { CvListPage } from '../pages/CvListPage'
import { DashboardPage } from '../pages/DashboardPage'
import { LanguageEditorPage } from '../pages/LanguageEditorPage'

export const router = createBrowserRouter([
  {
    element: <Layout />,
    children: [
      { path: '/', element: <DashboardPage /> },
      { path: '/cvs', element: <CvListPage /> },
      { path: '/cvs/:cvId', element: <CvEditorPage /> },
      { path: '/cvs/:cvId/languages/:languageId', element: <LanguageEditorPage /> },
    ],
  },
])
