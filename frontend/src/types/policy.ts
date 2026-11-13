export type PolicyStatus = 'ACTIVE' | 'CANCELLED' | 'EXPIRED'

export interface Policy {
  id: number
  customerId: number
  customerNumber: string
  customerName: string
  productId: number
  productName: string
  validFrom: string
  validTo: string
  coverageLimit: number
  premium: number
  status: PolicyStatus
  /** Last covered day of a cancelled policy. */
  cancelledOn: string | null
  /** Maximum paid for all losses of one policy year, if the product defines one. */
  annualAggregateLimit: number | null
}

export interface PolicyPage {
  content: Policy[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
