import { useQuery } from '@tanstack/react-query'
import { apiGet } from '../../api/client'
import type { Usage } from '../../types/cv'

export function useUsage() {
  return useQuery({
    queryKey: ['billing', 'usage'],
    queryFn: () => apiGet<Usage>('/billing/usage'),
  })
}
