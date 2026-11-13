import { useState } from 'react'
import { useClaims } from '@/features/claims/hooks/useClaims'
import { useAuth } from '@/features/auth/useAuth'
import { ReportClaimDialog } from './ReportClaimDialog'
import { Button } from '@/components/ui/button'
import { ClaimStats } from './ClaimStats'
import { ClaimTable } from './ClaimTable'

interface ClaimFormProps {
  onLoginRequired: () => void
}

export function ClaimForm({ onLoginRequired }: ClaimFormProps) {
  const {
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
  } = useClaims()
  const { session } = useAuth()
  const [reportOpen, setReportOpen] = useState(false)

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="mx-auto max-w-7xl">
        {/* Page Header */}
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-gray-900">Claims Management</h1>
            <p className="mt-1 text-sm text-gray-500">Übersicht und Verwaltung der Schadenfälle</p>
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => (session === null ? onLoginRequired() : setReportOpen(true))}
              className="rounded-full border border-blue-600 px-6 py-2 text-sm font-medium text-blue-700 transition hover:bg-blue-50"
            >
              + Schaden melden
            </button>
            <Button
              className="!rounded-full !px-6 py-2"
              type="button"
              onClick={handleLoadClaims}
              disabled={loading}
            >
              {loading ? 'Lade Daten...' : '↻ Daten laden'}
            </Button>
          </div>
        </div>

        {/* Message Banner */}
        {message && (
          <div
            role={message.tone === 'error' ? 'alert' : 'status'}
            className={`mb-6 rounded-lg border px-4 py-3 text-sm ${
              message.tone === 'success'
                ? 'border-green-200 bg-green-50 text-green-700'
                : 'border-red-200 bg-red-50 text-red-700'
            }`}
          >
            {message.text}
          </div>
        )}

        {/* Statistics Component */}
        <ClaimStats
          totalClaims={totalClaims}
          filteredCount={filteredClaims.length}
          totalAmount={totalAmount}
        />

        {/* Main Table Component */}
        <ClaimTable
          claims={claims}
          filteredClaims={filteredClaims}
          loading={loading}
          search={search}
          setSearch={setSearch}
          sortField={sortField}
          sortDirection={sortDirection}
          onSort={handleSort}
          onLoadClaims={handleLoadClaims}
          page={page}
          totalPages={totalPages}
          totalClaims={totalClaims}
          onPageChange={handlePageChange}
          updatingId={updatingId}
          onStatusChange={handleStatusChange}
          onLoginRequired={onLoginRequired}
        />
      </div>
      <ReportClaimDialog
        open={reportOpen}
        onClose={() => setReportOpen(false)}
        onReported={() => {
          setReportOpen(false)
          void handleLoadClaims()
        }}
      />
    </div>
  )
}

export default ClaimForm
