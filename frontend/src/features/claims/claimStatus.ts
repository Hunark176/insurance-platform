import type { ClaimStatus } from '@/types/claim'

export const CLAIM_STATUS_LABELS: Record<ClaimStatus, string> = {
  RECEIVED: 'Eingegangen',
  IN_REVIEW: 'In Prüfung',
  APPROVED: 'Genehmigt',
  REJECTED: 'Abgelehnt',
}

export const CLAIM_STATUS_STYLES: Record<ClaimStatus, string> = {
  RECEIVED: 'bg-gray-50 text-gray-700 ring-gray-200',
  IN_REVIEW: 'bg-amber-50 text-amber-700 ring-amber-200',
  APPROVED: 'bg-green-50 text-green-700 ring-green-200',
  REJECTED: 'bg-red-50 text-red-700 ring-red-200',
}

export interface ClaimTransition {
  target: ClaimStatus
  label: string
  tone: 'neutral' | 'positive' | 'negative'
}

/** Mirrors the backend lifecycle: RECEIVED → IN_REVIEW → APPROVED | REJECTED. */
export const CLAIM_TRANSITIONS: Record<ClaimStatus, ClaimTransition[]> = {
  RECEIVED: [{ target: 'IN_REVIEW', label: 'Prüfung starten', tone: 'neutral' }],
  IN_REVIEW: [
    { target: 'APPROVED', label: 'Genehmigen', tone: 'positive' },
    { target: 'REJECTED', label: 'Ablehnen', tone: 'negative' },
  ],
  APPROVED: [],
  REJECTED: [],
}
