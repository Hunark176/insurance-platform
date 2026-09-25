import axios from 'axios'
import type { Claim } from '../types/claim'

const API_BASE_URL = '/api/claims'

export async function fetchClaims(): Promise<Claim[]> {
  try {
    const response = await axios.get<Claim[]>(API_BASE_URL)
    return response.data
  } catch (error) {
    console.error('Fehler beim Abrufen der Claims:', error)
    throw error
  }
}
