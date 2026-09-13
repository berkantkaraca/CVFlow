import { useMutation, useQueryClient } from '@tanstack/react-query'
import { getToken } from '../../api/client'

export interface BackupImportResult {
  importedCvs: number
  importedLanguages: number
}

export async function downloadBackup(): Promise<void> {
  const token = getToken()
  const res = await fetch('/api/v1/backup/export', {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  if (!res.ok) {
    throw new Error(`Backup export failed with status ${res.status}`)
  }
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'cvflow-backup.json'
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

async function importBackupDocument(document: unknown): Promise<BackupImportResult> {
  const token = getToken()
  const res = await fetch('/api/v1/backup/import', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(document),
  })
  if (!res.ok) {
    throw new Error(`Backup import failed with status ${res.status}`)
  }
  return (await res.json()) as BackupImportResult
}

export function useImportBackup() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: importBackupDocument,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['cvs'] }),
  })
}
