import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

// White surface that groups related content on a page
export function Panel({ className, children }: { className?: string; children: ReactNode }) {
  return <section className={cn('rounded-lg border border-line bg-surface', className)}>{children}</section>;
}

interface PageHeaderProps {
  title: string;
  description?: string;
  actions?: ReactNode;
}

export function PageHeader({ title, description, actions }: PageHeaderProps) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="font-display text-3xl font-semibold tracking-tight">{title}</h1>
        {description && <p className="mt-1 text-sm text-muted">{description}</p>}
      </div>
      {actions && <div className="flex gap-2">{actions}</div>}
    </div>
  );
}