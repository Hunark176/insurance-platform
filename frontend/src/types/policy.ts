import type { components } from '@/api/generated/schema'

// Generated from the backend OpenAPI contract (npm run generate:api-types); do not redefine fields here.
export type Policy = components['schemas']['PolicyResponse']
export type PolicyPage = components['schemas']['PageResponsePolicyResponse']
export type PolicyStatus = Policy['status']
