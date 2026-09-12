import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPost } from '../../api/client'
import type { AiSuggestion, AiSuggestionStatus, CvSection } from '../../types/cv'

export interface ImproveInput {
  languageVersionId: string
  section: CvSection
  entityId?: string | null
  field: string
  content: string
}

export interface TranslateInput {
  sourceLanguageVersionId: string
  targetLanguageVersionId: string
}

export const suggestionKeys = {
  list: (languageVersionId: string, status?: AiSuggestionStatus | null) =>
    ['suggestions', languageVersionId, status ?? 'ALL'] as const,
}

export function useSuggestions(languageVersionId: string, status?: AiSuggestionStatus | null) {
  const params = new URLSearchParams({ languageVersionId })
  if (status) params.set('status', status)
  return useQuery({
    queryKey: suggestionKeys.list(languageVersionId, status),
    queryFn: () => apiGet<AiSuggestion[]>(`/ai/suggestions?${params.toString()}`),
  })
}

function useInvalidateSuggestions() {
  const queryClient = useQueryClient()
  return (languageVersionId: string) =>
    queryClient.invalidateQueries({ queryKey: ['suggestions', languageVersionId] })
}

export function useImprove(languageVersionId: string) {
  const invalidate = useInvalidateSuggestions()
  return useMutation({
    mutationFn: (input: ImproveInput) => apiPost<AiSuggestion>('/ai/improve', input),
    onSuccess: () => invalidate(languageVersionId),
  })
}

export function useRewrite(languageVersionId: string) {
  const invalidate = useInvalidateSuggestions()
  return useMutation({
    mutationFn: (input: ImproveInput) => apiPost<AiSuggestion>('/ai/rewrite', input),
    onSuccess: () => invalidate(languageVersionId),
  })
}

export function useTranslate() {
  return useMutation({
    mutationFn: (input: TranslateInput) => apiPost<AiSuggestion[]>('/ai/translate', input),
  })
}
export function useAcceptSuggestion(languageVersionId: string) {
  const invalidate = useInvalidateSuggestions()
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => apiPost<AiSuggestion>(`/ai/suggestions/${id}/accept`),
    onSuccess: () => {
      invalidate(languageVersionId)
      queryClient.invalidateQueries({ queryKey: ['cvs'] })
    },
  })
}

export function useRejectSuggestion(languageVersionId: string) {
  const invalidate = useInvalidateSuggestions()
  return useMutation({
    mutationFn: (id: string) => apiPost<AiSuggestion>(`/ai/suggestions/${id}/reject`),
    onSuccess: () => invalidate(languageVersionId),
  })
}

export function useAcceptAll(languageVersionId: string) {
  const invalidate = useInvalidateSuggestions()
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => apiPost<AiSuggestion[]>('/ai/suggestions/accept-all', { languageVersionId }),
    onSuccess: () => {
      invalidate(languageVersionId)
      queryClient.invalidateQueries({ queryKey: ['cvs'] })
    },
  })
}
