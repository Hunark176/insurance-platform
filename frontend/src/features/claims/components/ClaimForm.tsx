import { useClaims } from '@/features/claims/hooks/useClaims'
import { Button } from '@/components/ui/button'
import { ClaimStats } from './ClaimStats'
import { ClaimTable } from './ClaimTable'

export function ClaimForm() {
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
  } = useClaims()

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="mx-auto max-w-7xl">
        {/* Page Header */}
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-gray-900">Claims Management</h1>
            <p className="mt-1 text-sm text-gray-500">Übersicht und Verwaltung der Schadenfälle</p>
          </div>

          <Button
            className="!rounded-full !px-6 py-2"
            type="button"
            onClick={handleLoadClaims}
            disabled={loading}
          >
            {loading ? 'Lade Daten...' : '↻ Daten laden'}
          </Button>
        </div>

        {/* Message Banner */}
        {message && (
          <div
            className={`mb-6 rounded-lg border px-4 py-3 text-sm ${
              message.includes('erfolgreich')
                ? 'border-green-200 bg-green-50 text-green-700'
                : 'border-red-200 bg-red-50 text-red-700'
            }`}
          >
            {message}
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
        />
      </div>
    </div>
  )
}

export default ClaimForm
