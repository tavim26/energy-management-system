import type { ReactNode, TdHTMLAttributes, ThHTMLAttributes } from 'react';
import { cn } from '@/lib/cn';

// The wrapper scrolls horizontally on narrow screens instead of squeezing the columns
export function Table({ children }: { children: ReactNode }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-sm">{children}</table>
    </div>
  );
}

export function Th({ className, ...props }: ThHTMLAttributes<HTMLTableCellElement>) {
  return (
    <th
      scope="col"
      className={cn('border-b border-line px-4 py-3 text-xs font-medium text-muted first:pl-6 last:pr-6', className)}
      {...props}
    />
  );
}

export function Td({ className, ...props }: TdHTMLAttributes<HTMLTableCellElement>) {
  return (
    <td
      className={cn('border-b border-line px-4 py-3 align-middle first:pl-6 last:pr-6 [tr:last-child_&]:border-b-0', className)}
      {...props}
    />
  );
}