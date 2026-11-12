import apiClient from './apiClient'
import type { CurrentUser, DemoAccount } from '../types/auth'

export async function fetchCurrentUser(token: string): Promise<CurrentUser> {
  const response = await apiClient.get<CurrentUser>('/api/auth/me', {
    headers: { Authorization: `Basic ${token}` },
  })
  return response.data
}

export async function fetchDemoAccounts(): Promise<DemoAccount[]> {
  const response = await apiClient.get<DemoAccount[]>('/api/auth/demo-accounts')
  return response.data
}
