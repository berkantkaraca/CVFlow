import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from '../../api/client'
import type { CvContent, CvSection, LanguageCode, LanguageVersion, LanguageVersionSummary, TemplateCode } from '../../types/cv'

export interface CreateLanguageInput {
  languageCode: LanguageCode
  name: string
  templateCode: TemplateCode
}

export interface UpdateLanguageInput {
  name: string
  templateCode: TemplateCode
  content: CvContent
  sectionOrder: CvSection[]
  version: number
}

export const languageKeys = {
  list: (cvId: string) => ['cvs', cvId, 'languages'] as const,
  detail: (cvId: string, languageId: string) => ['cvs', cvId, 'languages', languageId] as const,
}

export function useLanguages(cvId: string) {
  return useQuery({
    queryKey: languageKeys.list(cvId),
    queryFn: () => apiGet<LanguageVersionSummary[]>(`/cvs/${cvId}/languages`),
  })
}

export function useLanguage(cvId: string, languageId: string) {
  return useQuery({
    queryKey: languageKeys.detail(cvId, languageId),
    queryFn: () => apiGet<LanguageVersion>(`/cvs/${cvId}/languages/${languageId}`),
  })
}

export function useCreateLanguage(cvId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateLanguageInput) => apiPost<LanguageVersion>(`/cvs/${cvId}/languages`, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: languageKeys.list(cvId) }),
  })
}

export function useUpdateLanguage(cvId: string, languageId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: UpdateLanguageInput) =>
      apiPut<LanguageVersion>(`/cvs/${cvId}/languages/${languageId}`, input),
    onSuccess: (data) => {
      queryClient.setQueryData(languageKeys.detail(cvId, languageId), data)
      queryClient.invalidateQueries({ queryKey: languageKeys.list(cvId) })
    },
  })
}

export function useDeleteLanguage(cvId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (languageId: string) => apiDelete(`/cvs/${cvId}/languages/${languageId}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: languageKeys.list(cvId) }),
  })
}
