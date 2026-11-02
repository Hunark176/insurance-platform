import { Component, type ErrorInfo, type ReactNode } from 'react'

type Props = { children: ReactNode }
type State = { hasError: boolean }

export class FrontendErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error('[InsurancePlatform]', {
      timestamp: new Date().toISOString(),
      event: 'ui.render.failed',
      errorType: error.name,
      componentStack: errorInfo.componentStack,
    })
  }

  render() {
    if (this.state.hasError) {
      return (
        <main role="alert" className="p-6 text-center">
          Die Ansicht konnte nicht angezeigt werden. Bitte lade die Seite neu.
        </main>
      )
    }

    return this.props.children
  }
}
