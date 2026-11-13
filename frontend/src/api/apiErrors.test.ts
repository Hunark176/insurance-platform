import { AxiosError, AxiosHeaders, type AxiosResponse } from 'axios'
import { describe, expect, it } from 'vitest'
import { describeApiError } from './apiErrors'

function httpError(status: number, data: unknown = {}): AxiosError {
  const config = { headers: new AxiosHeaders() }
  const response = { status, data, statusText: '', headers: {}, config } as AxiosResponse
  return new AxiosError('Request failed', 'ERR_BAD_RESPONSE', config, null, response)
}

describe('describeApiError', () => {
  it('reports a missing server connection', () => {
    expect(describeApiError(new AxiosError('Network Error', 'ERR_NETWORK'))).toBe(
      'Keine Verbindung zum Server möglich.',
    )
  })

  it.each([
    [401, 'Bitte zuerst anmelden.'],
    [
      403,
      'Für diese Aktion fehlt die Berechtigung (Rolle Sachbearbeiter oder Administrator nötig).',
    ],
    [409, 'Der Datensatz wurde inzwischen geändert. Bitte neu laden.'],
  ])('translates HTTP %i', (status, message) => {
    expect(describeApiError(httpError(status))).toBe(message)
  })

  it('shows the ProblemDetail detail for business rule violations', () => {
    const error = httpError(422, { detail: 'Claim amount exceeds the policy coverage limit' })

    expect(describeApiError(error)).toBe('Claim amount exceeds the policy coverage limit')
  })

  it('falls back to the status code when no detail is available', () => {
    expect(describeApiError(httpError(500))).toBe('Die Anfrage ist fehlgeschlagen (HTTP 500).')
  })

  it('handles errors that are not HTTP errors', () => {
    expect(describeApiError(new Error('boom'))).toBe('Unerwarteter Fehler.')
  })
})
