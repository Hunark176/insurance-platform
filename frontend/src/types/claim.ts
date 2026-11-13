import type { components } from '@/api/generated/schema'

// Generated from the backend OpenAPI contract (npm run generate:api-types); do not redefine fields here.
export type Claim = components['schemas']['ClaimResponse']
export type ClaimPage = components['schemas']['ClaimPageResponse']
export type ClaimStatus = Claim['status']
export type CreateClaimRequest = components['schemas']['CreateClaimRequest']
export type ClaimType = CreateClaimRequest['claimType']
