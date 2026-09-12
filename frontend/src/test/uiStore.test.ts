import { beforeEach, describe, expect, it } from 'vitest'
import { useUiStore } from '../stores/uiStore'

beforeEach(() => {
  useUiStore.setState({ toasts: [] })
})

describe('uiStore', () => {
  it('adds and dismisses toasts', () => {
    useUiStore.getState().notify('hello', 'success')
    expect(useUiStore.getState().toasts).toHaveLength(1)
    expect(useUiStore.getState().toasts[0].message).toBe('hello')
    const id = useUiStore.getState().toasts[0].id
    useUiStore.getState().dismiss(id)
    expect(useUiStore.getState().toasts).toHaveLength(0)
  })
})
