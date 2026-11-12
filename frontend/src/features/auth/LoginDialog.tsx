import { useEffect, useRef, useState, type FormEvent } from 'react'
import { fetchDemoAccounts } from '@/api/authService'
import type { DemoAccount } from '@/types/auth'
import { ROLE_DESCRIPTIONS, ROLE_LABELS } from './roles'
import { useAuth } from './useAuth'

interface LoginDialogProps {
  open: boolean
  onClose: () => void
}

export function LoginDialog({ open, onClose }: LoginDialogProps) {
  const { login } = useAuth()
  const [accounts, setAccounts] = useState<DemoAccount[]>([])
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [pending, setPending] = useState<string | null>(null)
  const dialogRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) {
      return
    }
    let active = true
    fetchDemoAccounts()
      .then((result) => {
        if (active) setAccounts(result)
      })
      .catch(() => {
        if (active) setAccounts([])
      })
    dialogRef.current?.focus()

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', onKeyDown)
    return () => {
      active = false
      window.removeEventListener('keydown', onKeyDown)
    }
  }, [open, onClose])

  if (!open) {
    return null
  }

  const submit = async (name: string, secret: string, pendingKey: string) => {
    setPending(pendingKey)
    setError('')
    const result = await login(name, secret)
    setPending(null)
    if (result.ok) {
      setUsername('')
      setPassword('')
      onClose()
    } else {
      setError(result.message)
    }
  }

  const onSubmit = (event: FormEvent) => {
    event.preventDefault()
    void submit(username.trim(), password, 'form')
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/40 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose()
      }}
    >
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby="login-title"
        tabIndex={-1}
        className="w-full max-w-md rounded-xl bg-white p-6 text-left shadow-xl outline-none"
      >
        <div className="mb-5 flex items-start justify-between gap-4">
          <div>
            <h2 id="login-title" className="text-lg font-semibold text-gray-900">
              Anmelden
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              Lesen ist ohne Anmeldung möglich. Zum Bearbeiten eine Rolle wählen.
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Dialog schließen"
            className="rounded-lg px-2 py-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
          >
            ✕
          </button>
        </div>

        {accounts.length > 0 && (
          <div className="mb-5 space-y-2">
            <p className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              Demo-Zugang mit einem Klick
            </p>
            {accounts.map((account) => (
              <button
                key={account.username}
                type="button"
                disabled={pending !== null}
                onClick={() => void submit(account.username, account.password, account.username)}
                className="flex w-full items-center justify-between gap-3 rounded-lg border border-gray-200 px-4 py-3 text-left transition hover:border-blue-300 hover:bg-blue-50 disabled:cursor-wait disabled:opacity-60"
              >
                <span>
                  <span className="block text-sm font-medium text-gray-900">
                    Als {ROLE_LABELS[account.role]}
                  </span>
                  <span className="block text-xs text-gray-500">
                    {ROLE_DESCRIPTIONS[account.role]}
                  </span>
                </span>
                <span className="shrink-0 font-mono text-xs text-gray-400">
                  {pending === account.username ? '…' : `${account.username} / ${account.password}`}
                </span>
              </button>
            ))}
          </div>
        )}

        <form onSubmit={onSubmit} className="space-y-3">
          {accounts.length > 0 && (
            <p className="text-xs font-semibold uppercase tracking-wider text-gray-500">
              Oder manuell
            </p>
          )}
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-gray-700">Benutzername</span>
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              required
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-gray-700">Passwort</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
          </label>

          {error && (
            <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={pending !== null}
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-blue-700 disabled:cursor-wait disabled:opacity-60"
          >
            {pending === 'form' ? 'Anmeldung läuft…' : 'Anmelden'}
          </button>
        </form>
      </div>
    </div>
  )
}
