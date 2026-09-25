export const formatCurrency = (amount: number) => {
  return amount.toLocaleString('de-DE', {
    style: 'currency',
    currency: 'EUR',
  })
}
