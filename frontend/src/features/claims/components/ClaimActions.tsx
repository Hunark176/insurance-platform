import { useAuth } from '@/features/auth/useAuth'
import { CLAIM_TRANSITIONS, type ClaimTransition } from '@/features/claims/claimStatus'
import type { Claim, ClaimStatus } from '@/types/claim'

interface ClaimActionsProps {
  claim: Claim
  updating: boolean
  onStatusChange: (id: number, status: ClaimStatus) => void
  onLoginRequired: () => void
}

const TONE_STYLES: Record<ClaimTransition['tone'], string> = {
  neutral: 'border-gray-300 text-gray-700 hover:bg-gray-100',
  positive: 'border-green-300 text-green-700 hover:bg-green-50',
  negative: 'border-red-300 text-red-700 hover:bg-red-50',
}

export function ClaimActions({
  claim,
  updating,
  onStatusChange,
  onLoginRequired,
}: ClaimActionsProps) {
  const { session, hasAnyRole } = useAuth()
  const transitions = CLAIM_TRANSITIONS[claim.status] ?? []

  if (transitions.length === 0) {
    return <span className="text-xs text-gray-400">Abgeschlossen</span>
  }

  const mayEdit = hasAnyRole('CLERK', 'ADMIN')
  const deniedHint = 'Nur Sachbearbeiter oder Administratoren dürfen Claims bearbeiten.'

  return (
    <div className="flex flex-col items-end gap-1">
      <div className="flex justify-end gap-2">
        {transitions.map((transition) => (
          <button
            key={transition.target}
            type="button"
            disabled={updating || (session !== null && !mayEdit)}
            title={session !== null && !mayEdit ? deniedHint : undefined}
            onClick={() =>
              session === null ? onLoginRequired() : onStatusChange(claim.id, transition.target)
            }
            className={`rounded-lg border px-3 py-1.5 text-xs font-medium transition disabled:cursor-not-allowed disabled:opacity-50 ${TONE_STYLES[transition.tone]}`}
          >
            {updating ? '…' : transition.label}
          </button>
        ))}
      </div>
      {session === null && <span className="text-xs text-gray-400">Anmeldung erforderlich</span>}
      {session !== null && !mayEdit && (
        <span className="text-xs text-gray-400">Nur Sachbearbeiter</span>
      )}
    </div>
  )
}
