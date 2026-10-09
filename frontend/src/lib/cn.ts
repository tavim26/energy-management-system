// Joins class names, skipping the ones that are false, null or undefined
export function cn(...classes: Array<string | false | null | undefined>): string {
  return classes.filter(Boolean).join(' ');
}