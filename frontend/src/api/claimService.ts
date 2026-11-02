import apiClient from './apiClient'
import type { ClaimPage } from '../types/claim'

const API_BASE_URL = '/api/claims'

export async function fetchClaims(page = 0, size = 20, policyId?: number): Promise<ClaimPage> {
  const response = await apiClient.get<ClaimPage>(API_BASE_URL, {
    params: { page, size, ...(policyId === undefined ? {} : { policyId }) },
  })
  return response.data
}
