import { useCallback, useSyncExternalStore } from 'react'
import axios from 'axios'
import { fetchCurrentUser } from '@/api/authService'
import type { Role } from '@/types/auth'
import { authStore, toBasicToken } from './authStore'

export type LoginResult = { ok: true } | { ok: false; message: string }

export function useAuth() {
  const session = useSyncExternalStore(authStore.subscribe, authStore.getSession)

  const login = useCallback(async (username: string, password: string): Promise<LoginResult> => {
    const token = toBasicToken(username, password)
    try {
      const user = await fetchCurrentUser(token)
      authStore.setSession({
        username: user.username,
        roles: user.roles,
        token,
        customerNumber: user.customerNumber ?? null,
      })
      return { ok: true }
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        return { ok: false, message: 'Benutzername oder Passwort ist falsch.' }
      }
      return { ok: false, message: 'Anmeldung fehlgeschlagen. Ist der Server erreichbar?' }
    }
  }, [])

  const logout = useCallback(() => authStore.setSession(null), [])

  const hasAnyRole = useCallback(
    (...roles: Role[]) => session !== null && session.roles.some((role) => roles.includes(role)),
    [session],
  )

  return { session, login, logout, hasAnyRole }
}
