import apiClient from './apiClient'
import type { Claim, ClaimPage, ClaimStatus, CreateClaimRequest } from '../types/claim'

const API_BASE_URL = '/api/claims'

export async function fetchClaims(page = 0, size = 20, policyId?: number): Promise<ClaimPage> {
  const response = await apiClient.get<ClaimPage>(API_BASE_URL, {
    params: { page, size, ...(policyId === undefined ? {} : { policyId }) },
  })
  return response.data
}

/**
 * Reports a claim. The idempotency key makes a retry after a timeout safe: the backend
 * returns the first response instead of creating a second claim.
 */
export async function createClaim(
  request: CreateClaimRequest,
  idempotencyKey: string,
): Promise<Claim> {
  const response = await apiClient.post<Claim>(API_BASE_URL, request, {
    headers: { 'Idempotency-Key': idempotencyKey },
  })
  return response.data
}

export async function updateClaimStatus(id: number, newStatus: ClaimStatus): Promise<Claim> {
  const response = await apiClient.patch<Claim>(`${API_BASE_URL}/${id}/status`, null, {
    params: { newStatus },
  })
  return response.data
}
