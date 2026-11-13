import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { authStore, toBasicToken } from '@/features/auth/authStore'
import type { Role } from '@/types/auth'
import type { Policy } from '@/types/policy'
import { PolicyActions } from './PolicyActions'

function policy(overrides: Partial<Policy> = {}): Policy {
  return {
    id: 12,
    customerId: 1,
    customerNumber: 'DEMO-1001',
    customerName: 'Anna Schneider',
    productId: 2,
    productName: 'Private Haftpflichtversicherung',
    validFrom: '2026-01-01',
    validTo: '2099-12-31',
    coverageLimit: 10000000,
    premium: 68,
    status: 'ACTIVE',
    cancelledOn: null,
    annualAggregateLimit: 20000000,
    ...overrides,
  }
}

function loginAs(role: Role) {
  const username = role.toLowerCase()
  authStore.setSession({ username, roles: [role], token: toBasicToken(username, username) })
}

function renderActions(target: Policy = policy()) {
  const onCancel = vi.fn().mockResolvedValue(true)
  const onLoginRequired = vi.fn()
  render(
    <PolicyActions
      policy={target}
      cancelling={false}
      onCancel={onCancel}
      onLoginRequired={onLoginRequired}
    />,
  )
  return { onCancel, onLoginRequired }
}

describe('PolicyActions', () => {
  beforeEach(() => authStore.setSession(null))

  it('asks anonymous visitors to log in', async () => {
    const { onCancel, onLoginRequired } = renderActions()

    await userEvent.click(screen.getByRole('button', { name: 'Stornieren' }))

    expect(onLoginRequired).toHaveBeenCalledOnce()
    expect(onCancel).not.toHaveBeenCalled()
  })

  it('does not offer cancellation to customers', () => {
    loginAs('CUSTOMER')
    renderActions()

    expect(screen.queryByRole('button', { name: 'Stornieren' })).not.toBeInTheDocument()
    expect(screen.getByText('Nur Sachbearbeiter')).toBeInTheDocument()
  })

  it('lets clerks cancel with an effective date after confirming', async () => {
    loginAs('CLERK')
    const { onCancel } = renderActions()

    await userEvent.click(screen.getByRole('button', { name: 'Stornieren' }))
    const dateInput = screen.getByLabelText('Letzter gedeckter Tag')
    await userEvent.clear(dateInput)
    await userEvent.type(dateInput, '2099-06-30')
    await userEvent.click(screen.getByRole('button', { name: 'Storno bestätigen' }))

    expect(onCancel).toHaveBeenCalledWith(12, '2099-06-30')
  })

  it('shows the last covered day of a cancelled policy', () => {
    loginAs('CLERK')
    renderActions(policy({ status: 'CANCELLED', cancelledOn: '2026-11-13' }))

    expect(screen.getByText('gedeckt bis 2026-11-13')).toBeInTheDocument()
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })
})
