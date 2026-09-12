import { apiDownload } from '../../api/client'
import type { LanguageCode } from '../../types/cv'

export function exportPdf(cvId: string, languageCode: LanguageCode): Promise<void> {
  return apiDownload(`/cvs/${cvId}/export/pdf`, { languageCode }, `cv-${languageCode.toLowerCase()}.pdf`)
}
