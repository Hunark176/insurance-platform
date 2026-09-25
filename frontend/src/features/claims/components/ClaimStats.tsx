import { formatCurrency } from '@/utils/formatters'

interface ClaimStatsProps {
  totalClaims: number
  filteredCount: number
  totalAmount: number
}

export function ClaimStats({ totalClaims, filteredCount, totalAmount }: ClaimStatsProps) {
  return (
    <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
      <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
        <p className="text-sm font-medium text-gray-500">Anzahl Claims</p>
        <p className="mt-2 text-2xl font-bold text-gray-900">{totalClaims}</p>
      </div>

      <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
        <p className="text-sm font-medium text-gray-500">Angezeigt</p>
        <p className="mt-2 text-2xl font-bold text-gray-900">{filteredCount}</p>
      </div>

      <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
        <p className="text-sm font-medium text-gray-500">Gesamtschaden</p>
        <p className="mt-2 text-2xl font-bold text-gray-900">{formatCurrency(totalAmount)}</p>
      </div>
    </div>
  )
}
