import type { Role } from '@/types/auth'

export const ROLE_LABELS: Record<Role, string> = {
  CUSTOMER: 'Kunde',
  CLERK: 'Sachbearbeiter',
  ADMIN: 'Administrator',
}

export const ROLE_DESCRIPTIONS: Record<Role, string> = {
  CUSTOMER: 'Darf Schäden melden, aber keine Schäden bearbeiten.',
  CLERK: 'Prüft, genehmigt und lehnt Schäden ab.',
  ADMIN: 'Alle Rechte inklusive Auszahlungen und Audit-Trail.',
}
