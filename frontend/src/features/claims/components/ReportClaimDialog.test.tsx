import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { authStore, toBasicToken } from '@/features/auth/authStore'
import type { Policy, PolicyPage } from '@/types/policy'
import { ReportClaimDialog } from './ReportClaimDialog'

const { fetchPolicies, createClaim } = vi.hoisted(() => ({
  fetchPolicies: vi.fn(),
  createClaim: vi.fn(),
}))
vi.mock('@/api/policyService', () => ({ fetchPolicies }))
vi.mock('@/api/claimService', () => ({ createClaim }))

function policy(id: number, customerNumber: string, customerName: string): Policy {
  return {
    id,
    customerId: id,
    customerNumber,
    customerName,
    productId: 1,
    productName: 'Kfz-Versicherung Komfort',
    validFrom: '2026-01-01',
    validTo: '2026-12-31',
    coverageLimit: 120000,
    premium: 684,
    status: 'ACTIVE',
    cancelledOn: null,
    annualAggregateLimit: null,
  }
}

const page: PolicyPage = {
  content: [policy(1, 'DEMO-1001', 'Anna Schneider'), policy(2, 'DEMO-1002', 'Ben Wagner')],
  page: 0,
  size: 100,
  totalElements: 2,
  totalPages: 1,
  first: true,
  last: true,
}

describe('ReportClaimDialog', () => {
  beforeEach(() => {
    fetchPolicies.mockReset().mockResolvedValue(page)
    createClaim.mockReset()
  })

  it('offers customers only their own policies', async () => {
    authStore.setSession({
      username: 'customer',
      roles: ['CUSTOMER'],
      token: toBasicToken('customer', 'customer'),
      customerNumber: 'DEMO-1001',
    })
    render(<ReportClaimDialog open onClose={vi.fn()} onReported={vi.fn()} />)

    await waitFor(() => expect(screen.getAllByRole('option', { name: /#1/ })).toHaveLength(1))
    expect(screen.queryByRole('option', { name: /Ben Wagner/ })).not.toBeInTheDocument()
  })

  it('sends the claim with the same idempotency key on a retry', async () => {
    authStore.setSession({
      username: 'clerk',
      roles: ['CLERK'],
      token: toBasicToken('clerk', 'clerk'),
    })
    createClaim.mockRejectedValueOnce(new Error('network down')).mockResolvedValueOnce({ id: 99 })
    const onReported = vi.fn()
    render(<ReportClaimDialog open onClose={vi.fn()} onReported={onReported} />)
    await screen.findByRole('option', { name: /Ben Wagner/ })

    await userEvent.type(screen.getByLabelText('Betrag in Euro'), '250')
    await userEvent.click(screen.getByRole('button', { name: 'Schaden melden' }))
    await screen.findByRole('alert')
    await userEvent.click(screen.getByRole('button', { name: 'Schaden melden' }))

    await waitFor(() => expect(onReported).toHaveBeenCalledWith({ id: 99 }))
    expect(createClaim).toHaveBeenCalledTimes(2)
    const [firstRequest, firstKey] = createClaim.mock.calls[0]
    const [, secondKey] = createClaim.mock.calls[1]
    expect(firstRequest).toMatchObject({ policyId: 1, customerNumber: 'DEMO-1001', amount: 250 })
    expect(secondKey).toBe(firstKey)
  })
})
