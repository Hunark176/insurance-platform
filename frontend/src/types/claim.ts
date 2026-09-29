export interface Claim {
  id: number
  customerNumber: string
  claimType: string
  amount: number
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
