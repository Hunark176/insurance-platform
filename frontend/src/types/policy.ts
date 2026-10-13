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
  status: 'ACTIVE' | 'CANCELLED'
}
