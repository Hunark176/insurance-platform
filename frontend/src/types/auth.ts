export type Role = 'CUSTOMER' | 'CLERK' | 'ADMIN'

export interface Session {
  username: string
  roles: Role[]
  /** Base64-encoded `username:password` for the HTTP Basic header. */
  token: string
  /** Customer represented by a customer login; null for staff. */
  customerNumber?: string | null
}

export interface CurrentUser {
  username: string
  roles: Role[]
  customerNumber?: string | null
}

export interface DemoAccount {
  username: string
  password: string
  role: Role
}
