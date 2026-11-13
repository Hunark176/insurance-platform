import axios from 'axios'

/** Translates an API failure into a short German message for the user. */
export function describeApiError(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return 'Unerwarteter Fehler.'
  }
  const status = error.response?.status
  const detail = (error.response?.data as { detail?: unknown } | undefined)?.detail

  switch (status) {
    case undefined:
      return 'Keine Verbindung zum Server möglich.'
    case 401:
      return 'Bitte zuerst anmelden.'
    case 403:
      return 'Für diese Aktion fehlt die Berechtigung (Rolle Sachbearbeiter oder Administrator nötig).'
    case 409:
      return 'Der Datensatz wurde inzwischen geändert. Bitte neu laden.'
    case 429:
      return 'Zu viele Anfragen in kurzer Zeit. Bitte etwa eine Minute warten.'
    default:
      return typeof detail === 'string' && detail.length > 0
        ? detail
        : `Die Anfrage ist fehlgeschlagen (HTTP ${status}).`
  }
}
