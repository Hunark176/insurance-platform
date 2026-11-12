import apiClient from './apiClient'
import type { Claim, ClaimPage, ClaimStatus } from '../types/claim'

const API_BASE_URL = '/api/claims'

export async function fetchClaims(page = 0, size = 20, policyId?: number): Promise<ClaimPage> {
  const response = await apiClient.get<ClaimPage>(API_BASE_URL, {
    params: { page, size, ...(policyId === undefined ? {} : { policyId }) },
  })
  return response.data
}

export async function updateClaimStatus(id: number, newStatus: ClaimStatus): Promise<Claim> {
  const response = await apiClient.patch<Claim>(`${API_BASE_URL}/${id}/status`, null, {
    params: { newStatus },
  })
  return response.data
}
