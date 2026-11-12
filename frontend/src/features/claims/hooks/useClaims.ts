import { useCallback, useEffect, useState } from 'react'
import { fetchClaims, updateClaimStatus } from '@/api/claimService.ts'
import { describeApiError } from '@/api/apiErrors'
import { CLAIM_STATUS_LABELS } from '@/features/claims/claimStatus'
import type { Claim, ClaimStatus } from '@/types/claim.ts'

type SortField = 'id' | 'amount'
type SortDirection = 'asc' | 'desc'

export interface Notice {
  tone: 'success' | 'error'
  text: string
}

export function useClaims() {
  const [claims, setClaims] = useState<Claim[]>([])
  const [message, setMessage] = useState<Notice | null>(null)
  const [loading, setLoading] = useState(false)
  const [updatingId, setUpdatingId] = useState<number | null>(null)
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [totalClaims, setTotalClaims] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const [sortField, setSortField] = useState<SortField>('id')
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc')

  const handleLoadClaims = useCallback(async () => {
    try {
      setLoading(true)
      setMessage(null)
      const data = await fetchClaims(page)
      setClaims(data.content)
      setTotalClaims(data.totalElements)
      setTotalPages(data.totalPages)
      setMessage({ tone: 'success', text: 'Daten erfolgreich geladen.' })
    } catch (error) {
      setMessage({ tone: 'error', text: describeApiError(error) })
    } finally {
      setLoading(false)
    }
  }, [page])

  const handleStatusChange = useCallback(async (id: number, newStatus: ClaimStatus) => {
    setUpdatingId(id)
    setMessage(null)
    try {
      const updated = await updateClaimStatus(id, newStatus)
      setClaims((current) => current.map((claim) => (claim.id === id ? updated : claim)))
      setMessage({
        tone: 'success',
        text:
          newStatus === 'APPROVED'
            ? `Claim #${id} genehmigt – die Auszahlung wird automatisch angelegt.`
            : `Claim #${id}: Status ist jetzt „${CLAIM_STATUS_LABELS[newStatus]}“.`,
      })
    } catch (error) {
      setMessage({ tone: 'error', text: `Claim #${id}: ${describeApiError(error)}` })
    } finally {
      setUpdatingId(null)
    }
  }, [])

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void handleLoadClaims()
    }, 0)
    return () => window.clearTimeout(timer)
  }, [handleLoadClaims])

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection((currentDirection) => (currentDirection === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortField(field)
      setSortDirection('asc')
    }
  }

  const handlePageChange = (nextPage: number) => {
    if (nextPage >= 0 && nextPage < totalPages) {
      setPage(nextPage)
    }
  }

  const filteredClaims = claims
    .filter((claim) => {
      const searchValue = search.toLowerCase()

      return (
        claim.customerName.toLowerCase().includes(searchValue) ||
        claim.customerNumber.toLowerCase().includes(searchValue) ||
        claim.claimType.toLowerCase().includes(searchValue) ||
        claim.id.toString().includes(searchValue)
      )
    })
    .sort((a, b) => {
      const valueA = a[sortField]
      const valueB = b[sortField]

      if (valueA < valueB) {
        return sortDirection === 'asc' ? -1 : 1
      }

      if (valueA > valueB) {
        return sortDirection === 'asc' ? 1 : -1
      }

      return 0
    })

  const totalAmount = filteredClaims.reduce((sum, claim) => sum + claim.amount, 0)

  return {
    claims,
    filteredClaims,
    message,
    loading,
    search,
    setSearch,
    sortField,
    sortDirection,
    totalAmount,
    page,
    totalClaims,
    totalPages,
    handleLoadClaims,
    handleSort,
    handlePageChange,
    handleStatusChange,
    updatingId,
  }
}
