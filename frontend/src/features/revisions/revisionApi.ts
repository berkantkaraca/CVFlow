import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiGet, apiPost } from '../../api/client'
import type { Revision } from '../../types/cv'

export const revisionKeys = {
  master: (cvId: string) => ['cvs', cvId, 'revisions'] as const,
  language: (cvId: string, languageId: string) => ['cvs', cvId, 'languages', languageId, 'revisions'] as const,
}

export function useMasterRevisions(cvId: string) {
  return useQuery({
    queryKey: revisionKeys.master(cvId),
    queryFn: () => apiGet<Revision[]>(`/cvs/${cvId}/revisions`),
  })
}

export function useLanguageRevisions(cvId: string, languageId: string) {
  return useQuery({
    queryKey: revisionKeys.language(cvId, languageId),
    queryFn: () => apiGet<Revision[]>(`/cvs/${cvId}/languages/${languageId}/revisions`),
  })
}

export function useRestoreMasterRevision(cvId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ revisionId, version }: { revisionId: string; version: number }) =>
      apiPost(`/cvs/${cvId}/revisions/${revisionId}/restore`, { version }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cvs', cvId] })
      queryClient.invalidateQueries({ queryKey: revisionKeys.master(cvId) })
    },
  })
}

export function useRestoreLanguageRevision(cvId: string, languageId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ revisionId, version }: { revisionId: string; version: number }) =>
      apiPost(`/cvs/${cvId}/languages/${languageId}/revisions/${revisionId}/restore`, { version }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cvs', cvId, 'languages', languageId] })
      queryClient.invalidateQueries({ queryKey: revisionKeys.language(cvId, languageId) })
    },
  })
}
