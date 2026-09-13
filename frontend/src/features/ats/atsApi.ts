import { useQuery } from '@tanstack/react-query'
import { apiGet } from '../../api/client'
import type { AtsScore } from '../../types/cv'

export function useAtsScore(cvId: string, languageId: string) {
  return useQuery({
    queryKey: ['cvs', cvId, 'languages', languageId, 'ats-score'],
    queryFn: () => apiGet<AtsScore>(`/cvs/${cvId}/languages/${languageId}/ats-score`),
  })
}
