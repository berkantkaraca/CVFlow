import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiDelete, apiGet, apiPost, apiPut } from '../../api/client'
import type { CvContent, CvDetail, CvSection, CvSummary } from '../../types/cv'

export interface CreateCvInput {
  name: string
}

export interface UpdateCvInput {
  name: string
  content: CvContent
  sectionOrder: CvSection[]
  version: number
}

export const cvKeys = {
  all: ['cvs'] as const,
  detail: (id: string) => ['cvs', id] as const,
}

export function useCvs() {
  return useQuery({ queryKey: cvKeys.all, queryFn: () => apiGet<CvSummary[]>('/cvs') })
}

export function useCv(id: string) {
  return useQuery({ queryKey: cvKeys.detail(id), queryFn: () => apiGet<CvDetail>(`/cvs/${id}`) })
}

export function useCreateCv() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateCvInput) => apiPost<CvDetail>('/cvs', input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: cvKeys.all }),
  })
}

export function useUpdateCv(id: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (input: UpdateCvInput) => apiPut<CvDetail>(`/cvs/${id}`, input),
    onSuccess: (data) => {
      queryClient.setQueryData(cvKeys.detail(id), data)
      queryClient.invalidateQueries({ queryKey: cvKeys.all })
    },
  })
}

export function useDeleteCv() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => apiDelete(`/cvs/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: cvKeys.all }),
  })
}

export function useDuplicateCv() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => apiPost<CvDetail>(`/cvs/${id}/duplicate`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: cvKeys.all }),
  })
}
