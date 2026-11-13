import type { components } from '@/api/generated/schema'

/** Roles of the backend (`Roles`); the API describes them only as strings. */
export type Role = 'CUSTOMER' | 'CLERK' | 'ADMIN'

export interface Session {
  username: string
  roles: Role[]
  /** Base64-encoded `username:password` for the HTTP Basic header. */
  token: string
  /** Customer represented by a customer login; null for staff. */
  customerNumber?: string | null
}

// Generated from the backend OpenAPI contract (npm run generate:api-types).
export type CurrentUser = components['schemas']['CurrentUser']

export type DemoAccount = Omit<components['schemas']['DemoAccount'], 'role'> & { role: Role }
