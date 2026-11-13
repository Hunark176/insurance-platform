import type { Role, Session } from '@/types/auth'

const STORAGE_KEY = 'insurance-platform.session'

type Listener = () => void

const listeners = new Set<Listener>()
let session: Session | null = readStoredSession()

function readStoredSession(): Session | null {
  try {
    const raw = window.sessionStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return null
    }
    const parsed = JSON.parse(raw) as Partial<Session>
    if (
      typeof parsed.username === 'string' &&
      typeof parsed.token === 'string' &&
      Array.isArray(parsed.roles)
    ) {
      return {
        username: parsed.username,
        token: parsed.token,
        roles: parsed.roles as Role[],
        customerNumber: typeof parsed.customerNumber === 'string' ? parsed.customerNumber : null,
      }
    }
  } catch {
    // Storage can be unavailable (private mode, blocked site data); the app works without it.
  }
  return null
}

function persist(next: Session | null): void {
  try {
    if (next) {
      window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next))
    } else {
      window.sessionStorage.removeItem(STORAGE_KEY)
    }
  } catch {
    // Ignore: the session then only lives until the page is reloaded.
  }
}

/** Encodes credentials for the HTTP Basic header, UTF-8 safe. */
export function toBasicToken(username: string, password: string): string {
  const bytes = new TextEncoder().encode(`${username}:${password}`)
  let binary = ''
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte)
  })
  return btoa(binary)
}

export const authStore = {
  getSession(): Session | null {
    return session
  },
  setSession(next: Session | null): void {
    session = next
    persist(next)
    listeners.forEach((listener) => listener())
  },
  subscribe(listener: Listener): () => void {
    listeners.add(listener)
    return () => listeners.delete(listener)
  },
}
