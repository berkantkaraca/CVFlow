import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from '../../api/client'
import { downloadPdf } from '../pdf/pdfApi'
import type { CoverLetter, CoverLetterSummary, LanguageCode } from '../../types/cv'

export interface CreateLetterInput {
  languageCode: LanguageCode
  title: string
  recipient?: string
  company?: string
  position?: string
}

export interface UpdateLetterInput {
  title: string
  recipient?: string | null
  company?: string | null
  position?: string | null
  body: string
  version: number
}

export const letterKeys = {
  list: (cvId: string) => ['cvs', cvId, 'cover-letters'] as const,
  detail: (cvId: string, letterId: string) => ['cvs', cvId, 'cover-letters', letterId] as const,
}

export function useCoverLetters(cvId: string) {
  return useQuery({
    queryKey: letterKeys.list(cvId),
    queryFn: () => apiGet<CoverLetterSummary[]>(`/cvs/${cvId}/cover-letters`),
  })
}

export function useCoverLetter(cvId: string, letterId: string) {
  return useQuery({
    queryKey: letterKeys.detail(cvId, letterId),
    queryFn: () => apiGet<CoverLetter>(`/cvs/${cvId}/cover-letters/${letterId}`),
  })
}

export function useCreateCoverLetter(cvId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateLetterInput) => apiPost<CoverLetter>(`/cvs/${cvId}/cover-letters`, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: letterKeys.list(cvId) }),
  })
}

export function useUpdateCoverLetter(cvId: string, letterId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: UpdateLetterInput) =>
      apiPut<CoverLetter>(`/cvs/${cvId}/cover-letters/${letterId}`, input),
    onSuccess: (data) => {
      queryClient.setQueryData(letterKeys.detail(cvId, letterId), data)
      queryClient.invalidateQueries({ queryKey: letterKeys.list(cvId) })
    },
  })
}

export function useDeleteCoverLetter(cvId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (letterId: string) => apiDelete(`/cvs/${cvId}/cover-letters/${letterId}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: letterKeys.list(cvId) }),
  })
}

export function exportCoverLetterPdf(cvId: string, letterId: string): Promise<void> {
  return downloadPdf(`/cvs/${cvId}/cover-letters/${letterId}/export/pdf`, 'cover-letter.pdf')
}
