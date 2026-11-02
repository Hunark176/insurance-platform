import apiClient from './apiClient'
import type { Policy } from '../types/policy'

const API_BASE_URL = '/api/policies'

export async function fetchPolicies(): Promise<Policy[]> {
  const response = await apiClient.get<Policy[]>(API_BASE_URL)
  return response.data
}
