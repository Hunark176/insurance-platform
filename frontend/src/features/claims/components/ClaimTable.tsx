import { Button } from '@/components/ui/button'
import { formatCurrency } from '@/utils/formatters'
import type { Claim } from '@/types/claim'

interface ClaimTableProps {
  claims: Claim[]
  filteredClaims: Claim[]
  loading: boolean
  search: string
  setSearch: (value: string) => void
  sortField: 'id' | 'amount'
  sortDirection: 'asc' | 'desc'
  onSort: (field: 'id' | 'amount') => void
  onLoadClaims: () => void
  page: number
  totalPages: number
  totalClaims: number
  onPageChange: (page: number) => void
}

export function ClaimTable({
  claims,
  filteredClaims,
  loading,
  search,
  setSearch,
  sortField,
  sortDirection,
  onSort,
  onLoadClaims,
  page,
  totalPages,
  totalClaims,
  onPageChange,
}: ClaimTableProps) {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
      {/* Card Header & Search */}
      <div className="border-b border-gray-200 px-6 py-5">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Schadenfälle</h2>
            <p className="mt-1 text-sm text-gray-500">
              {filteredClaims.length} von {claims.length} Claims auf dieser Seite · {totalClaims} insgesamt
            </p>
          </div>

          <div className="relative w-full lg:w-80">
            <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
              🔎
            </span>
            <input
              type="text"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Diese Seite durchsuchen..."
              className="w-full rounded-lg border border-gray-300 bg-white py-2.5 pl-10 pr-4 text-sm text-gray-900 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            />
          </div>
        </div>
      </div>

      {/* Empty State */}
      {claims.length === 0 && !loading && totalClaims === 0 && (
        <div className="px-6 py-16 text-center">
          <div className="mb-4 text-5xl">📋</div>
          <h3 className="text-lg font-semibold text-gray-900">Keine Claims geladen</h3>
          <p className="mx-auto mt-2 max-w-md text-sm text-gray-500">
            Für diese Übersicht wurden keine Schadenfälle zurückgegeben.
          </p>
          <div className="mt-6">
            <Button type="button" onClick={onLoadClaims}>
              Claims laden
            </Button>
          </div>
        </div>
      )}

      {claims.length === 0 && !loading && totalClaims > 0 && (
        <div className="px-6 py-12 text-center">
          <p className="text-sm font-medium text-gray-700">Keine Claims auf dieser Seite</p>
        </div>
      )}

      {/* Loading State */}
      {loading && (
        <div className="px-6 py-16 text-center">
          <div className="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-blue-600" />
          <p className="text-sm text-gray-500">Claims werden geladen...</p>
        </div>
      )}

      {/* Table Data */}
      {claims.length > 0 && !loading && (
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-4">
                  <button
                    type="button"
                    onClick={() => onSort('id')}
                    className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-gray-500 hover:text-gray-900"
                  >
                    ID
                    <span>{sortField === 'id' ? (sortDirection === 'asc' ? '↑' : '↓') : '↕'}</span>
                  </button>
                </th>
                <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-gray-500">
                  Kundennummer
                </th>
                <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-gray-500">
                  Claim-Typ
                </th>
                <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-gray-500">
                  Schadendatum
                </th>
                <th className="px-6 py-4">
                  <button
                    type="button"
                    onClick={() => onSort('amount')}
                    className="ml-auto flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-gray-500 hover:text-gray-900"
                  >
                    Betrag
                    <span>
                      {sortField === 'amount' ? (sortDirection === 'asc' ? '↑' : '↓') : '↕'}
                    </span>
                  </button>
                </th>
                <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-gray-500">
                  Aktion
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredClaims.map((claim) => (
                <tr key={claim.id} className="group transition-colors hover:bg-gray-50">
                  <td className="whitespace-nowrap px-6 py-4">
                    <span className="font-semibold text-gray-900">#{claim.id}</span>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4">
                    <span className="font-medium text-gray-700">{claim.customerNumber}</span>
                  </td>
                  <td className="px-6 py-4">
                    <span className="inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700 ring-1 ring-inset ring-blue-200">
                      {claim.claimType}
                    </span>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-gray-700">
                    {new Intl.DateTimeFormat('de-DE', { dateStyle: 'medium', timeZone: 'UTC' }).format(
                      new Date(`${claim.occurredOn}T00:00:00Z`),
                    )}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-right">
                    <span className="font-semibold text-gray-900">
                      {formatCurrency(claim.amount)}
                    </span>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-right">
                    <button
                      type="button"
                      onClick={() => console.log('Claim:', claim)}
                      className="rounded-lg px-3 py-2 text-sm font-medium text-blue-600 transition hover:bg-blue-50 hover:text-blue-700"
                    >
                      Details →
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {filteredClaims.length === 0 && (
            <div className="px-6 py-12 text-center">
              <p className="text-sm font-medium text-gray-700">Keine Claims gefunden</p>
              <p className="mt-1 text-sm text-gray-500">Ändere deinen Suchbegriff.</p>
            </div>
          )}
        </div>
      )}

      {/* Footer */}
      {totalClaims > 0 && !loading && (
        <div className="flex flex-col gap-3 border-t border-gray-200 bg-gray-50 px-6 py-4 text-sm text-gray-500 sm:flex-row sm:items-center sm:justify-between">
          <span>{filteredClaims.length} Datensätze auf dieser Seite angezeigt</span>
          <div className="flex flex-wrap items-center justify-between gap-3">
            {claims.length > 0 && (
              <span>
                Sortiert nach{' '}
                <strong className="font-medium text-gray-700">
                  {sortField === 'id' ? 'ID' : 'Betrag'}
                </strong>{' '}
                ({sortDirection === 'asc' ? 'aufsteigend' : 'absteigend'})
              </span>
            )}
            <div className="flex items-center gap-2">
              <button
                type="button"
                disabled={page === 0}
                onClick={() => onPageChange(page - 1)}
                className="rounded-lg border border-gray-300 px-3 py-2 font-medium text-gray-700 transition hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Zurück
              </button>
              <span>
                Seite {page + 1} von {totalPages}
              </span>
              <button
                type="button"
                disabled={page + 1 >= totalPages}
                onClick={() => onPageChange(page + 1)}
                className="rounded-lg border border-gray-300 px-3 py-2 font-medium text-gray-700 transition hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Weiter
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
