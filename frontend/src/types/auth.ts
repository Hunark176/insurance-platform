export type Role = 'CUSTOMER' | 'CLERK' | 'ADMIN'

export interface Session {
  username: string
  roles: Role[]
  /** Base64-encoded `username:password` for the HTTP Basic header. */
  token: string
}

export interface CurrentUser {
  username: string
  roles: Role[]
}

export interface DemoAccount {
  username: string
  password: string
  role: Role
}
