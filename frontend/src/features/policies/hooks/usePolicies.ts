import { useCallback, useEffect, useState } from 'react'
import { cancelPolicy, fetchPolicies } from '@/api/policyService'
import { describeApiError } from '@/api/apiErrors'
import type { Policy } from '@/types/policy'

type SortField = 'id' | 'premium'
type SortDirection = 'asc' | 'desc'

export interface PolicyNotice {
  tone: 'success' | 'error'
  text: string
}

/** Page size for the policy overview; the API allows at most 100. */
const PAGE_SIZE = 100

export function usePolicies() {
  const [policies, setPolicies] = useState<Policy[]>([])
  const [message, setMessage] = useState<PolicyNotice | null>(null)
  const [loading, setLoading] = useState(false)
  const [cancellingId, setCancellingId] = useState<number | null>(null)
  const [search, setSearch] = useState('')
  const [sortField, setSortField] = useState<SortField>('id')
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalPolicies, setTotalPolicies] = useState(0)

  const handleLoadPolicies = useCallback(async () => {
    try {
      setLoading(true)
      setMessage(null)
      const data = await fetchPolicies(page, PAGE_SIZE)
      setPolicies(data.content)
      setTotalPages(data.totalPages)
      setTotalPolicies(data.totalElements)
      setMessage({ tone: 'success', text: 'Policen erfolgreich geladen.' })
    } catch (error) {
      setMessage({ tone: 'error', text: describeApiError(error) })
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void handleLoadPolicies()
    }, 0)
    return () => window.clearTimeout(timer)
  }, [handleLoadPolicies])

  const handleCancel = useCallback(async (id: number, effectiveOn: string): Promise<boolean> => {
    setCancellingId(id)
    setMessage(null)
    try {
      const updated = await cancelPolicy(id, effectiveOn)
      setPolicies((current) => current.map((policy) => (policy.id === id ? updated : policy)))
      setMessage({
        tone: 'success',
        text: `Police #${id} storniert. Schäden bis einschließlich ${effectiveOn} bleiben gedeckt; die anteilige Prämie wird gutgeschrieben.`,
      })
      return true
    } catch (error) {
      setMessage({ tone: 'error', text: `Police #${id}: ${describeApiError(error)}` })
      return false
    } finally {
      setCancellingId(null)
    }
  }, [])

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection((direction) => (direction === 'asc' ? 'desc' : 'asc'))
      return
    }
    setSortField(field)
    setSortDirection('asc')
  }

  const handlePageChange = (nextPage: number) => {
    if (nextPage >= 0 && nextPage < totalPages) {
      setPage(nextPage)
    }
  }

  const searchValue = search.toLowerCase()
  const filteredPolicies = policies
    .filter(
      (policy) =>
        policy.id.toString().includes(searchValue) ||
        policy.customerId.toString().includes(searchValue) ||
        policy.customerName.toLowerCase().includes(searchValue) ||
        policy.customerNumber.toLowerCase().includes(searchValue) ||
        policy.productId.toString().includes(searchValue) ||
        policy.productName.toLowerCase().includes(searchValue) ||
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
    page,
    totalPages,
    totalPolicies,
    cancellingId,
    handleLoadPolicies,
    handleSort,
    handlePageChange,
    handleCancel,
  }
}
