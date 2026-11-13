import { Button } from '@/components/ui/button'
import { PolicyStats } from './PolicyStats'
import { PolicyTable } from './PolicyTable'
import { usePolicies } from '@/features/policies/hooks/usePolicies'

interface PolicyManagementProps {
  onLoginRequired: () => void
}

export function PolicyManagement({ onLoginRequired }: PolicyManagementProps) {
  const {
    policies,
    filteredPolicies,
    message,
    loading,
    search,
    setSearch,
    sortField,
    sortDirection,
    page,
    totalPages,
    totalPolicies,
    cancellingId,
    handleLoadPolicies,
    handleSort,
    handlePageChange,
    handleCancel,
  } = usePolicies()

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="mx-auto max-w-7xl">
        <div className="mb-8 flex flex-col gap-4 text-left sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-gray-900">Policenverwaltung</h1>
            <p className="mt-1 text-sm text-gray-500">
              Übersicht und Verwaltung der Versicherungspolicen
            </p>
          </div>
          <Button
            className="!rounded-full !px-6 py-2"
            type="button"
            onClick={handleLoadPolicies}
            disabled={loading}
          >
            {loading ? 'Lade Daten...' : '↻ Daten laden'}
          </Button>
        </div>

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

        <PolicyStats
          totalPolicies={totalPolicies}
          activePolicies={policies.filter((policy) => policy.status === 'ACTIVE').length}
          filteredCount={filteredPolicies.length}
        />
        <PolicyTable
          policies={policies}
          filteredPolicies={filteredPolicies}
          loading={loading}
          search={search}
          setSearch={setSearch}
          sortField={sortField}
          sortDirection={sortDirection}
          onSort={handleSort}
          onLoadPolicies={handleLoadPolicies}
          page={page}
          totalPages={totalPages}
          totalPolicies={totalPolicies}
          onPageChange={handlePageChange}
          cancellingId={cancellingId}
          onCancel={handleCancel}
          onLoginRequired={onLoginRequired}
        />
      </div>
    </div>
  )
}
