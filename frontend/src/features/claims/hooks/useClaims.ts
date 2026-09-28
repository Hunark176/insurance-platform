import { useCallback, useEffect, useState } from 'react'
import { fetchClaims } from '@/api/claimService.ts'
import type { Claim } from '@/types/claim.ts'

type SortField = 'id' | 'amount'
type SortDirection = 'asc' | 'desc'

export function useClaims() {
  const [claims, setClaims] = useState<Claim[]>([])
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [search, setSearch] = useState('')

  const [sortField, setSortField] = useState<SortField>('id')
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc')

  const handleLoadClaims = useCallback(async () => {
    try {
      setLoading(true)
      setMessage('')
      const data = await fetchClaims()
      setClaims(data)
      setMessage('Daten erfolgreich geladen.')
    } catch (error) {
      console.error(error)
      setMessage('Keine Verbindung zum Server möglich.')
    } finally {
      setLoading(false)
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

  const filteredClaims = claims
    .filter((claim) => {
      const searchValue = search.toLowerCase()

      return (
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
    handleLoadClaims,
    handleSort,
  }
}
