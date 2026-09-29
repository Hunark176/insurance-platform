import axios from 'axios'
import type { ClaimPage } from '../types/claim'

const API_BASE_URL = '/api/claims'

export async function fetchClaims(page = 0, size = 20, policyId?: number): Promise<ClaimPage> {
  try {
    const response = await axios.get<ClaimPage>(API_BASE_URL, {
      params: { page, size, ...(policyId === undefined ? {} : { policyId }) },
    })
    return response.data
  } catch (error) {
    console.error('Fehler beim Abrufen der Claims:', error)
    throw error
  }
}
