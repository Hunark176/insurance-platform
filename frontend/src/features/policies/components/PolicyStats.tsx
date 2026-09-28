interface PolicyStatsProps {
  totalPolicies: number
  activePolicies: number
  filteredCount: number
}

export function PolicyStats({ totalPolicies, activePolicies, filteredCount }: PolicyStatsProps) {
  return (
    <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
      <div className="rounded-xl border border-gray-200 bg-white p-5 text-left shadow-sm">
        <p className="text-sm font-medium text-gray-500">Anzahl Policen</p>
        <p className="mt-2 text-2xl font-bold text-gray-900">{totalPolicies}</p>
      </div>
      <div className="rounded-xl border border-gray-200 bg-white p-5 text-left shadow-sm">
        <p className="text-sm font-medium text-gray-500">Aktive Policen</p>
        <p className="mt-2 text-2xl font-bold text-gray-900">{activePolicies}</p>
      </div>
      <div className="rounded-xl border border-gray-200 bg-white p-5 text-left shadow-sm">
        <p className="text-sm font-medium text-gray-500">Suchergebnisse</p>
        <p className="mt-2 text-2xl font-bold text-gray-900">{filteredCount}</p>
      </div>
    </div>
  )
}
