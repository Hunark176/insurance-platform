import { beforeEach, describe, expect, it, vi } from 'vitest'
import { authStore, toBasicToken } from './authStore'

const clerk = {
  username: 'clerk',
  roles: ['CLERK' as const],
  token: toBasicToken('clerk', 'clerk'),
}

describe('toBasicToken', () => {
  it('encodes username and password for the HTTP Basic header', () => {
    expect(toBasicToken('clerk', 'clerk')).toBe(btoa('clerk:clerk'))
  })

  it('encodes non-ASCII characters as UTF-8 instead of failing', () => {
    // btoa alone throws for characters outside Latin-1.
    expect(() => toBasicToken('jürgen', 'pässwört€')).not.toThrow()
    const decoded = new TextDecoder().decode(
      Uint8Array.from(atob(toBasicToken('jürgen', 'pässwört€')), (c) => c.charCodeAt(0)),
    )
    expect(decoded).toBe('jürgen:pässwört€')
  })
})

describe('authStore', () => {
  beforeEach(() => authStore.setSession(null))

  it('keeps the session for the browser tab in sessionStorage', () => {
    authStore.setSession(clerk)

    expect(authStore.getSession()).toEqual(clerk)
    expect(JSON.parse(window.sessionStorage.getItem('insurance-platform.session') ?? '{}')).toEqual(
      clerk,
    )
  })

  it('removes the stored session on logout', () => {
    authStore.setSession(clerk)
    authStore.setSession(null)

    expect(authStore.getSession()).toBeNull()
    expect(window.sessionStorage.getItem('insurance-platform.session')).toBeNull()
  })

  it('notifies subscribers until they unsubscribe', () => {
    const listener = vi.fn()
    const unsubscribe = authStore.subscribe(listener)

    authStore.setSession(clerk)
    unsubscribe()
    authStore.setSession(null)

    expect(listener).toHaveBeenCalledTimes(1)
  })

  it('keeps working when storage is unavailable', () => {
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('blocked')
    })

    expect(() => authStore.setSession(clerk)).not.toThrow()
    expect(authStore.getSession()).toEqual(clerk)
  })
})
