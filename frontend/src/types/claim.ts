export type ClaimStatus = 'RECEIVED' | 'IN_REVIEW' | 'APPROVED' | 'REJECTED'

export interface Claim {
  id: number
  policyId: number
  customerNumber: string
  customerName: string
  claimType: string
  amount: number
  status: ClaimStatus
  occurredOn: string
}

export interface ClaimPage {
  content: Claim[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
