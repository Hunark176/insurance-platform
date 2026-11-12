import { ROLE_LABELS } from './roles'
import { useAuth } from './useAuth'

interface UserMenuProps {
  onLoginClick: () => void
}

export function UserMenu({ onLoginClick }: UserMenuProps) {
  const { session, logout } = useAuth()

  if (!session) {
    return (
      <button
        type="button"
        onClick={onLoginClick}
        className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-100"
      >
        Anmelden
      </button>
    )
  }

  return (
    <div className="flex items-center gap-3 text-sm">
      <span className="text-gray-600">
        <span className="font-medium text-gray-900">{session.username}</span>
        {session.roles.length > 0 && (
          <span className="ml-2 rounded-full bg-blue-50 px-2 py-0.5 text-xs font-semibold text-blue-700 ring-1 ring-inset ring-blue-200">
            {session.roles.map((role) => ROLE_LABELS[role] ?? role).join(', ')}
          </span>
        )}
      </span>
      <button
        type="button"
        onClick={logout}
        className="rounded-lg px-3 py-2 font-medium text-gray-600 transition hover:bg-gray-100 hover:text-gray-900"
      >
        Abmelden
      </button>
    </div>
  )
}
