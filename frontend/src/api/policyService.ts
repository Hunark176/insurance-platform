import apiClient from './apiClient'
import type { Policy, PolicyPage } from '../types/policy'

const API_BASE_URL = '/api/policies'

export async function fetchPolicies(page = 0, size = 100): Promise<PolicyPage> {
  const response = await apiClient.get<PolicyPage>(API_BASE_URL, { params: { page, size } })
  return response.data
}

/** Cancels a policy; losses up to and including `effectiveOn` stay covered. */
export async function cancelPolicy(id: number, effectiveOn: string): Promise<Policy> {
  const response = await apiClient.post<Policy>(`${API_BASE_URL}/${id}/cancel`, null, {
    params: { effectiveOn },
  })
  return response.data
}
