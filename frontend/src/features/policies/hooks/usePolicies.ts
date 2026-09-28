import { useCallback, useEffect, useState } from 'react'
import { fetchPolicies } from '@/api/policyService'
import type { Policy } from '@/types/policy'

type SortField = 'id' | 'premium'
type SortDirection = 'asc' | 'desc'

export function usePolicies() {
  const [policies, setPolicies] = useState<Policy[]>([])
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [search, setSearch] = useState('')
  const [sortField, setSortField] = useState<SortField>('id')
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc')

  const handleLoadPolicies = useCallback(async () => {
    try {
      setLoading(true)
      setMessage('')
      setPolicies(await fetchPolicies())
      setMessage('Policen erfolgreich geladen.')
    } catch (error) {
      console.error(error)
      setMessage('Policen konnten nicht geladen werden.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void handleLoadPolicies()
    }, 0)
    return () => window.clearTimeout(timer)
  }, [handleLoadPolicies])

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection((direction) => (direction === 'asc' ? 'desc' : 'asc'))
      return
    }
    setSortField(field)
    setSortDirection('asc')
  }

  const searchValue = search.toLowerCase()
  const filteredPolicies = policies
    .filter(
      (policy) =>
        policy.id.toString().includes(searchValue) ||
        policy.customerId.toString().includes(searchValue) ||
        policy.productId.toString().includes(searchValue) ||
        policy.status.toLowerCase().includes(searchValue),
    )
    .sort((a, b) => {
      const difference = a[sortField] - b[sortField]
      return sortDirection === 'asc' ? difference : -difference
    })

  const totalCoverage = filteredPolicies.reduce((sum, policy) => sum + policy.coverageLimit, 0)

  return {
    policies,
    filteredPolicies,
    message,
    loading,
    search,
    setSearch,
    sortField,
    sortDirection,
    totalCoverage,
    handleLoadPolicies,
    handleSort,
  }
}
