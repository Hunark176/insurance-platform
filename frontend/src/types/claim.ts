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

export type ClaimType = 'AUTO' | 'HOME' | 'LIFE' | 'LIABILITY'

export interface CreateClaimRequest {
  policyId: number
  occurredOn: string
  customerNumber: string
  claimType: ClaimType
  description: string
  amount: number
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
