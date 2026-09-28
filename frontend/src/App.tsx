import { useState } from 'react'
import ClaimForm from '@/features/claims/components/ClaimForm'
import { PolicyManagement } from '@/features/policies/components/PolicyManagement'

function App() {
  const [activeView, setActiveView] = useState<'claims' | 'policies'>('claims')

  return (
    <main>
      <nav className="flex justify-center gap-3 border-b border-gray-200 bg-white px-6 py-3">
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
            activeView === 'policies' ? 'bg-blue-600 text-white' : 'text-gray-700 hover:bg-gray-100'
          }`}
        >
          Policen
        </button>
      </nav>
      {activeView === 'claims' ? <ClaimForm /> : <PolicyManagement />}
    </main>
  )
}

export default App
