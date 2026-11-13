import { useEffect, useRef, useState, type FormEvent } from 'react'
import { createClaim } from '@/api/claimService'
import { describeApiError } from '@/api/apiErrors'
import { fetchPolicies } from '@/api/policyService'
import { useAuth } from '@/features/auth/useAuth'
import type { Claim, ClaimType } from '@/types/claim'
import type { Policy } from '@/types/policy'
import { todayIso } from '@/utils/dates'
import { randomId } from '@/utils/ids'

interface ReportClaimDialogProps {
  open: boolean
  onClose: () => void
  onReported: (claim: Claim) => void
}

const CLAIM_TYPES: { value: ClaimType; label: string }[] = [
  { value: 'AUTO', label: 'Kfz' },
  { value: 'HOME', label: 'Hausrat / Wohngebäude' },
  { value: 'LIABILITY', label: 'Haftpflicht' },
  { value: 'LIFE', label: 'Leben' },
]

export function ReportClaimDialog({ open, onClose, onReported }: ReportClaimDialogProps) {
  const { session } = useAuth()
  const [policies, setPolicies] = useState<Policy[]>([])
  const [policyId, setPolicyId] = useState('')
  const [claimType, setClaimType] = useState<ClaimType>('AUTO')
  const [occurredOn, setOccurredOn] = useState(todayIso())
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  // One key per opened form: a retry after a network error does not create a second claim.
  const idempotencyKey = useRef(randomId())
  const customerNumber = session?.customerNumber ?? null

  useEffect(() => {
    if (!open) {
      return
    }
    idempotencyKey.current = randomId()
    let active = true
    fetchPolicies(0, 100)
      .then((page) => {
        if (!active) return
        // Customers may only report claims for their own policies (checked again by the backend).
        const selectable = page.content.filter(
          (policy) => customerNumber === null || policy.customerNumber === customerNumber,
        )
        setPolicies(selectable)
        setPolicyId(selectable[0] ? String(selectable[0].id) : '')
      })
      .catch((loadError: unknown) => {
        if (active) setError(describeApiError(loadError))
      })
    return () => {
      active = false
    }
  }, [open, customerNumber])

  if (!open) {
    return null
  }

  const selectedPolicy = policies.find((policy) => String(policy.id) === policyId)

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (!selectedPolicy) {
      setError('Bitte eine Police auswählen.')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      const claim = await createClaim(
        {
          policyId: selectedPolicy.id,
          occurredOn,
          customerNumber: selectedPolicy.customerNumber,
          claimType,
          description: description.trim(),
          amount: Number(amount),
        },
        idempotencyKey.current,
      )
      setAmount('')
      setDescription('')
      onReported(claim)
    } catch (submitError) {
      setError(describeApiError(submitError))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/40 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose()
      }}
    >
      <form
        role="dialog"
        aria-modal="true"
        aria-labelledby="report-claim-title"
        onSubmit={(event) => void submit(event)}
        className="w-full max-w-lg space-y-4 rounded-xl bg-white p-6 text-left shadow-xl"
      >
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 id="report-claim-title" className="text-lg font-semibold text-gray-900">
              Schaden melden
            </h2>
            <p className="mt-1 text-sm text-gray-500">
              {customerNumber
                ? `Policen von Kunde ${customerNumber}`
                : 'Erfassung für alle Kunden (Sachbearbeitung)'}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Dialog schließen"
            className="rounded-lg px-2 py-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
          >
            ✕
          </button>
        </div>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-gray-700">Police</span>
          <select
            value={policyId}
            onChange={(event) => setPolicyId(event.target.value)}
            required
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900"
          >
            {policies.length === 0 && <option value="">Keine Police verfügbar</option>}
            {policies.map((policy) => (
              <option key={policy.id} value={policy.id}>
                #{policy.id} · {policy.productName} · {policy.customerName}
              </option>
            ))}
          </select>
        </label>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-gray-700">Schadenart</span>
            <select
              value={claimType}
              onChange={(event) => setClaimType(event.target.value as ClaimType)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900"
            >
              {CLAIM_TYPES.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </label>
          <label className="block text-sm">
            <span className="mb-1 block font-medium text-gray-700">Schadendatum</span>
            <input
              type="date"
              required
              value={occurredOn}
              max={todayIso()}
              onChange={(event) => setOccurredOn(event.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900"
            />
          </label>
        </div>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-gray-700">Betrag in Euro</span>
          <input
            type="number"
            required
            min="0.01"
            step="0.01"
            value={amount}
            onChange={(event) => setAmount(event.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900"
          />
        </label>

        <label className="block text-sm">
          <span className="mb-1 block font-medium text-gray-700">Beschreibung</span>
          <textarea
            value={description}
            maxLength={1000}
            rows={3}
            onChange={(event) => setDescription(event.target.value)}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900"
          />
        </label>

        {error && (
          <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        )}

        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100"
          >
            Abbrechen
          </button>
          <button
            type="submit"
            disabled={submitting || !selectedPolicy}
            className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {submitting ? 'Wird gesendet…' : 'Schaden melden'}
          </button>
        </div>
      </form>
    </div>
  )
}
