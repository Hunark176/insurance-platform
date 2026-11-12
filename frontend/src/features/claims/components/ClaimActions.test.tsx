import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { authStore, toBasicToken } from '@/features/auth/authStore'
import type { Role } from '@/types/auth'
import type { Claim, ClaimStatus } from '@/types/claim'
import { ClaimActions } from './ClaimActions'

function claim(status: ClaimStatus): Claim {
  return {
    id: 7,
    policyId: 1,
    customerNumber: 'DEMO-1001',
    customerName: 'Anna Schneider',
    claimType: 'AUTO',
    amount: 250,
    status,
    occurredOn: '2026-11-01',
  }
}

function loginAs(role: Role) {
  const username = role.toLowerCase()
  authStore.setSession({ username, roles: [role], token: toBasicToken(username, username) })
}

function renderActions(status: ClaimStatus) {
  const onStatusChange = vi.fn()
  const onLoginRequired = vi.fn()
  render(
    <ClaimActions
      claim={claim(status)}
      updating={false}
      onStatusChange={onStatusChange}
      onLoginRequired={onLoginRequired}
    />,
  )
  return { onStatusChange, onLoginRequired }
}

describe('ClaimActions', () => {
  beforeEach(() => authStore.setSession(null))

  it('asks anonymous visitors to log in instead of changing the status', async () => {
    const { onStatusChange, onLoginRequired } = renderActions('RECEIVED')

    await userEvent.click(screen.getByRole('button', { name: 'Prüfung starten' }))

    expect(onLoginRequired).toHaveBeenCalledOnce()
    expect(onStatusChange).not.toHaveBeenCalled()
    expect(screen.getByText('Anmeldung erforderlich')).toBeInTheDocument()
  })

  it('disables the actions for customers', () => {
    loginAs('CUSTOMER')
    renderActions('IN_REVIEW')

    expect(screen.getByRole('button', { name: 'Genehmigen' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Ablehnen' })).toBeDisabled()
    expect(screen.getByText('Nur Sachbearbeiter')).toBeInTheDocument()
  })

  it('lets clerks move a claim along its lifecycle', async () => {
    loginAs('CLERK')
    const { onStatusChange } = renderActions('IN_REVIEW')

    await userEvent.click(screen.getByRole('button', { name: 'Genehmigen' }))

    expect(onStatusChange).toHaveBeenCalledWith(7, 'APPROVED')
  })

  it('offers only the transitions the backend allows', () => {
    loginAs('ADMIN')
    renderActions('RECEIVED')

    expect(screen.getByRole('button', { name: 'Prüfung starten' })).toBeEnabled()
    expect(screen.queryByRole('button', { name: 'Genehmigen' })).not.toBeInTheDocument()
  })

  it('shows closed claims without actions', () => {
    loginAs('CLERK')
    renderActions('APPROVED')

    expect(screen.queryByRole('button')).not.toBeInTheDocument()
    expect(screen.getByText('Abgeschlossen')).toBeInTheDocument()
  })
})
