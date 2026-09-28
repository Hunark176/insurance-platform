import { Button } from '@/components/ui/button'
import { formatCurrency } from '@/utils/formatters'
import type { Policy } from '@/types/policy'

interface PolicyTableProps {
  policies: Policy[]
  filteredPolicies: Policy[]
  loading: boolean
  search: string
  setSearch: (value: string) => void
  sortField: 'id' | 'premium'
  sortDirection: 'asc' | 'desc'
  onSort: (field: 'id' | 'premium') => void
  onLoadPolicies: () => void
}

const formatDate = (date: string) =>
  new Intl.DateTimeFormat('de-DE', { dateStyle: 'medium', timeZone: 'UTC' }).format(
    new Date(`${date}T00:00:00Z`),
  )

export function PolicyTable({
  policies,
  filteredPolicies,
  loading,
  search,
  setSearch,
  sortField,
  sortDirection,
  onSort,
  onLoadPolicies,
}: PolicyTableProps) {
  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white text-left shadow-sm">
      <div className="flex flex-col gap-4 border-b border-gray-200 px-6 py-5 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h2 className="text-lg font-semibold text-gray-900">Versicherungspolicen</h2>
          <p className="mt-1 text-sm text-gray-500">
            {filteredPolicies.length} von {policies.length} Policen
          </p>
        </div>
        <input
          type="search"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Policen suchen (ID, Kunde, Produkt, Status)..."
          aria-label="Policen durchsuchen"
          className="w-full rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 lg:w-96"
        />
      </div>

      {policies.length === 0 && !loading && (
        <div className="px-6 py-16 text-center">
          <h3 className="text-lg font-semibold text-gray-900">Noch keine Policen geladen</h3>
          <p className="mx-auto mt-2 max-w-md text-sm text-gray-500">
            Lade die Versicherungspolicen aus dem Backend, um sie hier anzuzeigen.
          </p>
          <div className="mt-6">
            <Button type="button" onClick={onLoadPolicies}>
              Policen laden
            </Button>
          </div>
        </div>
      )}

      {loading && (
        <div className="px-6 py-16 text-center text-sm text-gray-500">Policen werden geladen...</div>
      )}

      {policies.length > 0 && !loading && (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50">
              <tr>
                {(['id', 'premium'] as const).map((field) => (
                  <th key={field} className="px-6 py-4 text-left">
                    <button
                      type="button"
                      onClick={() => onSort(field)}
                      className="text-xs font-semibold uppercase tracking-wider text-gray-500 hover:text-gray-900"
                    >
                      {field === 'id' ? 'Policen-ID' : 'Prämie'}
                      {' '}{sortField === field ? (sortDirection === 'asc' ? '↑' : '↓') : '↕'}
                    </button>
                  </th>
                ))}
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                  Kunde / Produkt
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                  Laufzeit
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                  Deckungssumme
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                  Status
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredPolicies.map((policy) => (
                <tr key={policy.id} className="transition-colors hover:bg-gray-50">
                  <td className="whitespace-nowrap px-6 py-4 font-semibold text-gray-900">
                    #{policy.id}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-gray-700">
                    {formatCurrency(policy.premium)}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-gray-700">
                    Kunde #{policy.customerId} / Produkt #{policy.productId}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-gray-700">
                    {formatDate(policy.validFrom)} – {formatDate(policy.validTo)}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-gray-700">
                    {formatCurrency(policy.coverageLimit)}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4">
                    <span
                      className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
                        policy.status === 'ACTIVE'
                          ? 'bg-green-50 text-green-700 ring-1 ring-inset ring-green-200'
                          : 'bg-gray-100 text-gray-700 ring-1 ring-inset ring-gray-300'
                      }`}
                    >
                      {policy.status === 'ACTIVE' ? 'Aktiv' : 'Gekündigt'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {filteredPolicies.length === 0 && (
            <div className="px-6 py-12 text-center text-sm text-gray-500">
              Keine Policen für diese Suche gefunden.
            </div>
          )}
        </div>
      )}
    </div>
  )
}
