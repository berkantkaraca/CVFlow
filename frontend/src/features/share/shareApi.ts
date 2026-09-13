import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost } from '../../api/client'
import type { ShareLink } from '../../types/cv'

export const shareKeys = {
  list: (cvId: string, languageId: string) => ['cvs', cvId, 'languages', languageId, 'share'] as const,
}

export function useShareLinks(cvId: string, languageId: string) {
  return useQuery({
    queryKey: shareKeys.list(cvId, languageId),
    queryFn: () => apiGet<ShareLink[]>(`/cvs/${cvId}/languages/${languageId}/share`),
  })
}

export function useCreateShareLink(cvId: string, languageId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: { expiresInDays?: number }) =>
      apiPost<ShareLink>(`/cvs/${cvId}/languages/${languageId}/share`, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: shareKeys.list(cvId, languageId) }),
  })
}

export function useRevokeShareLink(cvId: string, languageId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (shareId: string) => apiDelete(`/cvs/${cvId}/languages/${languageId}/share/${shareId}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: shareKeys.list(cvId, languageId) }),
  })
}
