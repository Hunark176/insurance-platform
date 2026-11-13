import { useState } from 'react'
import { useAuth } from '@/features/auth/useAuth'
import type { Policy } from '@/types/policy'
import { todayIso } from '@/utils/dates'

interface PolicyActionsProps {
  policy: Policy
  cancelling: boolean
  /** Resolves to true when the cancellation succeeded. */
  onCancel: (id: number, effectiveOn: string) => Promise<boolean>
  onLoginRequired: () => void
}

export function PolicyActions({
  policy,
  cancelling,
  onCancel,
  onLoginRequired,
}: PolicyActionsProps) {
  const { session, hasAnyRole } = useAuth()
  const [confirming, setConfirming] = useState(false)
  const [effectiveOn, setEffectiveOn] = useState(todayIso())

  if (policy.status !== 'ACTIVE') {
    return policy.cancelledOn ? (
      <span className="text-xs text-gray-500">gedeckt bis {policy.cancelledOn}</span>
    ) : (
      <span className="text-xs text-gray-400">–</span>
    )
  }

  if (session === null) {
    return (
      <button
        type="button"
        onClick={onLoginRequired}
        className="rounded-lg border border-gray-300 px-3 py-1.5 text-xs font-medium text-gray-700 transition hover:bg-gray-100"
      >
        Stornieren
      </button>
    )
  }

  if (!hasAnyRole('CLERK', 'ADMIN')) {
    return <span className="text-xs text-gray-400">Nur Sachbearbeiter</span>
  }

  if (!confirming) {
    return (
      <button
        type="button"
        onClick={() => setConfirming(true)}
        className="rounded-lg border border-red-300 px-3 py-1.5 text-xs font-medium text-red-700 transition hover:bg-red-50"
      >
        Stornieren
      </button>
    )
  }

  return (
    <form
      className="flex flex-col items-end gap-2"
      onSubmit={(event) => {
        event.preventDefault()
        void onCancel(policy.id, effectiveOn).then((ok) => {
          if (ok) setConfirming(false)
        })
      }}
    >
      <label className="text-xs text-gray-600">
        Letzter gedeckter Tag
        <input
          type="date"
          required
          value={effectiveOn}
          min={todayIso()}
          max={policy.validTo}
          onChange={(event) => setEffectiveOn(event.target.value)}
          className="ml-2 rounded-md border border-gray-300 px-2 py-1 text-xs"
        />
      </label>
      <div className="flex gap-2">
        <button
          type="button"
          onClick={() => setConfirming(false)}
          className="rounded-lg px-3 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-100"
        >
          Abbrechen
        </button>
        <button
          type="submit"
          disabled={cancelling}
          className="rounded-lg bg-red-600 px-3 py-1.5 text-xs font-medium text-white transition hover:bg-red-700 disabled:opacity-60"
        >
          {cancelling ? '…' : 'Storno bestätigen'}
        </button>
      </div>
    </form>
  )
}
