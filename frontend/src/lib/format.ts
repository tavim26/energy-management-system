export function formatKwh(value: number, digits = 2): string {
  return `${value.toFixed(digits)} kWh`;
}

export function formatHour(hour: number): string {
  return `${String(hour).padStart(2, '0')}:00`;
}