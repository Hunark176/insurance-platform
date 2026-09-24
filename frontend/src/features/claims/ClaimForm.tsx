import { useState } from 'react';
import { fetchClaims } from '@/api/claimService';
import type { Claim } from '@/types/claim';
import { Button } from '@/components/ui/button';

type SortField = 'id' | 'amount';
type SortDirection = 'asc' | 'desc';

export function ClaimForm() {
    const [claims, setClaims] = useState<Claim[]>([]);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);

    const [search, setSearch] = useState('');

    const [sortField, setSortField] = useState<SortField>('id');
    const [sortDirection, setSortDirection] =
        useState<SortDirection>('asc');

    const handleLoadClaims = async () => {
        try {
            setLoading(true);
            setMessage('');

            const data = await fetchClaims();

            setClaims(data);
            setMessage('Daten erfolgreich geladen.');
        } catch (error) {
            console.error(error);
            setMessage('Keine Verbindung zum Server möglich.');
        } finally {
            setLoading(false);
        }
    };

    const handleSort = (field: SortField) => {
        if (sortField === field) {
            setSortDirection(
                sortDirection === 'asc' ? 'desc' : 'asc'
            );
        } else {
            setSortField(field);
            setSortDirection('asc');
        }
    };

    const filteredClaims = claims
        .filter((claim) => {
            const searchValue = search.toLowerCase();

            return (
                claim.customerNumber
                    .toLowerCase()
                    .includes(searchValue) ||
                claim.claimType
                    .toLowerCase()
                    .includes(searchValue) ||
                claim.id.toString().includes(searchValue)
            );
        })
        .sort((a, b) => {
            const valueA = a[sortField];
            const valueB = b[sortField];

            if (valueA < valueB) {
                return sortDirection === 'asc' ? -1 : 1;
            }

            if (valueA > valueB) {
                return sortDirection === 'asc' ? 1 : -1;
            }

            return 0;
        });

    const totalAmount = filteredClaims.reduce(
        (sum, claim) => sum + claim.amount,
        0
    );

    const formatCurrency = (amount: number) => {
        return amount.toLocaleString('de-DE', {
            style: 'currency',
            currency: 'EUR',
        });
    };

    return (
        <div className="min-h-screen bg-gray-50 p-6">

            <div className="mx-auto max-w-7xl">

                {/* =========================
                    PAGE HEADER
                   ========================= */}

                <div className="mb-8">

                    <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

                        <div>
                            <h1 className="text-2xl font-bold tracking-tight text-gray-900">
                                Claims Management
                            </h1>

                            <p className="mt-1 text-sm text-gray-500">
                                Übersicht und Verwaltung der Schadenfälle
                            </p>
                        </div>

                        <Button
                            className="!rounded-full !px-6 py-2"
                            type="button"
                            onClick={handleLoadClaims}
                            disabled={loading}
                        >
                            {loading
                                ? 'Lade Daten...'
                                : '↻  Daten laden'}
                        </Button>

                    </div>

                </div>


                {/* =========================
                    MESSAGE
                   ========================= */}

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


                {/* =========================
                    STATISTICS
                   ========================= */}

                <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">

                    {/* Anzahl */}
                    <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">

                        <p className="text-sm font-medium text-gray-500">
                            Anzahl Claims
                        </p>

                        <p className="mt-2 text-2xl font-bold text-gray-900">
                            {claims.length}
                        </p>

                    </div>


                    {/* Angezeigt */}
                    <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">

                        <p className="text-sm font-medium text-gray-500">
                            Angezeigt
                        </p>

                        <p className="mt-2 text-2xl font-bold text-gray-900">
                            {filteredClaims.length}
                        </p>

                    </div>


                    {/* Summe */}
                    <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">

                        <p className="text-sm font-medium text-gray-500">
                            Gesamtschaden
                        </p>

                        <p className="mt-2 text-2xl font-bold text-gray-900">
                            {formatCurrency(totalAmount)}
                        </p>

                    </div>

                </div>


                {/* =========================
                    MAIN CARD
                   ========================= */}

                <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">


                    {/* =========================
                        CARD HEADER
                       ========================= */}

                    <div className="border-b border-gray-200 px-6 py-5">

                        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">

                            <div>
                                <h2 className="text-lg font-semibold text-gray-900">
                                    Schadenfälle
                                </h2>

                                <p className="mt-1 text-sm text-gray-500">
                                    {filteredClaims.length} von{' '}
                                    {claims.length} Claims
                                </p>
                            </div>


                            {/* Suche */}

                            <div className="relative w-full lg:w-80">

                                <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                                    🔎
                                </span>

                                <input
                                    type="text"
                                    value={search}
                                    onChange={(event) =>
                                        setSearch(event.target.value)
                                    }
                                    placeholder="Claims durchsuchen..."
                                    className="w-full rounded-lg border border-gray-300 bg-white py-2.5 pl-10 pr-4 text-sm text-gray-900 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                                />

                            </div>

                        </div>

                    </div>


                    {/* =========================
                        EMPTY STATE
                       ========================= */}

                    {claims.length === 0 && !loading && (

                        <div className="px-6 py-16 text-center">

                            <div className="mb-4 text-5xl">
                                📋
                            </div>

                            <h3 className="text-lg font-semibold text-gray-900">
                                Noch keine Claims geladen
                            </h3>

                            <p className="mx-auto mt-2 max-w-md text-sm text-gray-500">
                                Lade die Schadenfälle aus dem Spring-Boot-
                                Backend, um sie hier anzuzeigen.
                            </p>

                            <div className="mt-6">

                                <Button
                                    type="button"
                                    onClick={handleLoadClaims}
                                >
                                    Claims laden
                                </Button>

                            </div>

                        </div>

                    )}


                    {/* =========================
                        LOADING
                       ========================= */}

                    {loading && (

                        <div className="px-6 py-16 text-center">

                            <div className="mx-auto mb-4 h-8 w-8 animate-spin rounded-full border-4 border-gray-200 border-t-blue-600" />

                            <p className="text-sm text-gray-500">
                                Claims werden geladen...
                            </p>

                        </div>

                    )}


                    {/* =========================
                        TABLE
                       ========================= */}

                    {claims.length > 0 && !loading && (

                        <div className="overflow-x-auto">

                            <table className="w-full text-left text-sm">

                                {/* HEADER */}

                                <thead className="bg-gray-50">

                                <tr>

                                    {/* ID */}

                                    <th className="px-6 py-4">

                                        <button
                                            type="button"
                                            onClick={() =>
                                                handleSort('id')
                                            }
                                            className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-gray-500 hover:text-gray-900"
                                        >
                                            ID

                                            <span>
                                                    {sortField === 'id'
                                                        ? sortDirection === 'asc'
                                                            ? '↑'
                                                            : '↓'
                                                        : '↕'}
                                                </span>

                                        </button>

                                    </th>


                                    {/* CUSTOMER */}

                                    <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-gray-500">
                                        Kundennummer
                                    </th>


                                    {/* TYPE */}

                                    <th className="px-6 py-4 text-xs font-semibold uppercase tracking-wider text-gray-500">
                                        Claim-Typ
                                    </th>


                                    {/* AMOUNT */}

                                    <th className="px-6 py-4">

                                        <button
                                            type="button"
                                            onClick={() =>
                                                handleSort('amount')
                                            }
                                            className="ml-auto flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-gray-500 hover:text-gray-900"
                                        >
                                            Betrag

                                            <span>
                                                    {sortField === 'amount'
                                                        ? sortDirection === 'asc'
                                                            ? '↑'
                                                            : '↓'
                                                        : '↕'}
                                                </span>

                                        </button>

                                    </th>


                                    {/* ACTION */}

                                    <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-gray-500">
                                        Aktion
                                    </th>

                                </tr>

                                </thead>


                                {/* BODY */}

                                <tbody className="divide-y divide-gray-200">

                                {filteredClaims.map((claim) => (

                                    <tr
                                        key={claim.id}
                                        className="group transition-colors hover:bg-gray-50"
                                    >

                                        {/* ID */}

                                        <td className="whitespace-nowrap px-6 py-4">

                                                <span className="font-semibold text-gray-900">
                                                    #{claim.id}
                                                </span>

                                        </td>


                                        {/* CUSTOMER */}

                                        <td className="whitespace-nowrap px-6 py-4">

                                                <span className="font-medium text-gray-700">
                                                    {claim.customerNumber}
                                                </span>

                                        </td>


                                        {/* TYPE */}

                                        <td className="px-6 py-4">

                                                <span className="inline-flex items-center rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700 ring-1 ring-inset ring-blue-200">
                                                    {claim.claimType}
                                                </span>

                                        </td>


                                        {/* AMOUNT */}

                                        <td className="whitespace-nowrap px-6 py-4 text-right">

                                                <span className="font-semibold text-gray-900">
                                                    {formatCurrency(
                                                        claim.amount
                                                    )}
                                                </span>

                                        </td>


                                        {/* ACTION */}

                                        <td className="whitespace-nowrap px-6 py-4 text-right">

                                            <button
                                                type="button"
                                                onClick={() =>
                                                    console.log(
                                                        'Claim:',
                                                        claim
                                                    )
                                                }
                                                className="rounded-lg px-3 py-2 text-sm font-medium text-blue-600 transition hover:bg-blue-50 hover:text-blue-700"
                                            >
                                                Details →
                                            </button>

                                        </td>

                                    </tr>

                                ))}

                                </tbody>

                            </table>


                            {/* Keine Suchergebnisse */}

                            {filteredClaims.length === 0 && (

                                <div className="px-6 py-12 text-center">

                                    <p className="text-sm font-medium text-gray-700">
                                        Keine Claims gefunden
                                    </p>

                                    <p className="mt-1 text-sm text-gray-500">
                                        Ändere deinen Suchbegriff.
                                    </p>

                                </div>

                            )}

                        </div>

                    )}


                    {/* =========================
                        FOOTER
                       ========================= */}

                    {claims.length > 0 && !loading && (

                        <div className="flex flex-col gap-3 border-t border-gray-200 bg-gray-50 px-6 py-4 text-sm text-gray-500 sm:flex-row sm:items-center sm:justify-between">

                            <span>
                                {filteredClaims.length} Datensätze angezeigt
                            </span>

                            <span>
                                Sortiert nach{' '}
                                <strong className="font-medium text-gray-700">
                                    {sortField === 'id'
                                        ? 'ID'
                                        : 'Betrag'}
                                </strong>{' '}
                                (
                                {sortDirection === 'asc'
                                    ? 'aufsteigend'
                                    : 'absteigend'}
                                )
                            </span>

                        </div>

                    )}

                </div>

            </div>

        </div>
    );
}

export default ClaimForm;