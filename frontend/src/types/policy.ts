export interface Policy {
  id: number
  customerId: number
  productId: number
  validFrom: string
  validTo: string
  coverageLimit: number
  premium: number
  status: 'ACTIVE' | 'CANCELLED'
}
