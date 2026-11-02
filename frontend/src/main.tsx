import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { FrontendErrorBoundary } from './shared/FrontendErrorBoundary'

window.addEventListener('error', (event) => {
  console.error('[InsurancePlatform]', {
    timestamp: new Date().toISOString(),
    event: 'window.error',
    errorType: event.error instanceof Error ? event.error.name : 'UnknownError',
  })
})

window.addEventListener('unhandledrejection', (event) => {
  console.error('[InsurancePlatform]', {
    timestamp: new Date().toISOString(),
    event: 'window.unhandledrejection',
    errorType: event.reason instanceof Error ? event.reason.name : 'UnknownError',
  })
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <FrontendErrorBoundary>
      <App />
    </FrontendErrorBoundary>
  </StrictMode>,
)
