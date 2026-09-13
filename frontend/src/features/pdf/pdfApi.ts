import { apiDownload } from '../../api/client'
import type { LanguageCode } from '../../types/cv'

export function exportPdf(cvId: string, languageCode: LanguageCode): Promise<void> {
  return apiDownload(`/cvs/${cvId}/export/pdf`, { languageCode }, `cv-${languageCode.toLowerCase()}.pdf`)
}

export function exportDocx(cvId: string, languageCode: LanguageCode): Promise<void> {
  return apiDownload(`/cvs/${cvId}/export/docx`, { languageCode }, `cv-${languageCode.toLowerCase()}.docx`)
}

export function downloadPdf(path: string, filename: string): Promise<void> {
  return apiDownload(path, undefined, filename)
}
