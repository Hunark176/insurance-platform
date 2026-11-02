import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

type RequestConfigWithId = InternalAxiosRequestConfig & { requestId?: string }

const apiClient = axios.create({ baseURL: '/' })

function createRequestId(): string {
  if (typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (character) => {
    const random = Math.floor(Math.random() * 16)
    return (character === 'x' ? random : (random & 0x3) | 0x8).toString(16)
  })
}

function logApiFailure(error: AxiosError): void {
  const config = error.config as RequestConfigWithId | undefined
  const requestId = error.response?.headers['x-request-id'] ?? config?.requestId
  const details = {
    timestamp: new Date().toISOString(),
    event: 'api.request.failed',
    requestId,
    method: config?.method?.toUpperCase(),
    path: config?.url,
    status: error.response?.status,
    code: error.code,
  }

  if (error.response && error.response.status < 500) {
    console.warn('[InsurancePlatform]', details)
  } else {
    console.error('[InsurancePlatform]', details)
  }
}

apiClient.interceptors.request.use((config) => {
  const requestConfig = config as RequestConfigWithId
  requestConfig.requestId = createRequestId()
  config.headers.set('X-Request-Id', requestConfig.requestId)

  if (import.meta.env.DEV) {
    console.debug('[InsurancePlatform]', {
      timestamp: new Date().toISOString(),
      event: 'api.request.started',
      requestId: requestConfig.requestId,
      method: config.method?.toUpperCase(),
      path: config.url,
    })
  }

  return config
})

apiClient.interceptors.response.use(
  (response) => {
    if (import.meta.env.DEV) {
      const config = response.config as RequestConfigWithId
      console.debug('[InsurancePlatform]', {
        timestamp: new Date().toISOString(),
        event: 'api.request.completed',
        requestId: response.headers['x-request-id'] ?? config.requestId,
        method: config.method?.toUpperCase(),
        path: config.url,
        status: response.status,
      })
    }
    return response
  },
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      logApiFailure(error)
    } else {
      console.error('[InsurancePlatform]', {
        timestamp: new Date().toISOString(),
        event: 'api.request.failed',
        errorType: error instanceof Error ? error.name : 'UnknownError',
      })
    }
    return Promise.reject(error)
  },
)

export default apiClient
