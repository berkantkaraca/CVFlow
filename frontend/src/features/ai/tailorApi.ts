import { useMutation } from '@tanstack/react-query'
import { apiPost } from '../../api/client'
import type { AiSuggestion } from '../../types/cv'

export function useTailor() {
  return useMutation({
    mutationFn: (input: { languageVersionId: string; jobDescription: string }) =>
      apiPost<AiSuggestion[]>('/ai/tailor', input),
  })
}

export function useCoverLetterDraft() {
  return useMutation({
    mutationFn: (input: { masterCvId: string; languageCode: string; jobDescription?: string }) =>
      apiPost<{ body: string }>('/ai/cover-letter', input),
  })
}
