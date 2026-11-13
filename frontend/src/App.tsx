import { useCallback, useState } from 'react'
import ClaimForm from '@/features/claims/components/ClaimForm'
import { PolicyManagement } from '@/features/policies/components/PolicyManagement'
import { LoginDialog } from '@/features/auth/LoginDialog'
import { UserMenu } from '@/features/auth/UserMenu'

function App() {
  const [activeView, setActiveView] = useState<'claims' | 'policies'>('claims')
  const [loginOpen, setLoginOpen] = useState(false)
  const openLogin = useCallback(() => setLoginOpen(true), [])
  const closeLogin = useCallback(() => setLoginOpen(false), [])

  return (
    <main>
      <nav className="flex flex-wrap items-center justify-between gap-3 border-b border-gray-200 bg-white px-6 py-3 sm:grid sm:grid-cols-[1fr_auto_1fr]">
        <span className="hidden text-xs text-gray-400 sm:block">
          Demo-Umgebung · Lesen ohne Anmeldung
        </span>
        <div className="flex gap-3">
          <button
            type="button"
            aria-pressed={activeView === 'claims'}
            onClick={() => setActiveView('claims')}
            className={`rounded-lg px-4 py-2 text-sm font-medium ${
              activeView === 'claims' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'
            }`}
          >
            Schadenfälle
          </button>
          <button
            type="button"
            aria-pressed={activeView === 'policies'}
            onClick={() => setActiveView('policies')}
            className={`rounded-lg px-4 py-2 text-sm font-medium ${
              activeView === 'policies'
                ? 'bg-blue-600 text-white'
                : 'text-gray-700 hover:bg-gray-100'
            }`}
          >
            Policen
          </button>
        </div>
        <div className="flex justify-end">
          <UserMenu onLoginClick={openLogin} />
        </div>
      </nav>
      {activeView === 'claims' ? (
        <ClaimForm onLoginRequired={openLogin} />
      ) : (
        <PolicyManagement onLoginRequired={openLogin} />
      )}
      <LoginDialog open={loginOpen} onClose={closeLogin} />
    </main>
  )
}

export default App
