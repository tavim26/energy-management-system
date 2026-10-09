import type { ReactNode } from 'react';
import { cn } from '@/lib/cn';

type Tone = 'neutral' | 'brand' | 'energy' | 'danger';

const tones: Record<Tone, string> = {
  neutral: 'bg-paper text-muted ring-line',
  brand: 'bg-brand-soft text-brand-strong ring-brand/20',
  energy: 'bg-energy-soft text-[#7a5600] ring-energy/30',
  danger: 'bg-danger-soft text-danger ring-danger/20',
};

export function Badge({ tone = 'neutral', children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span className={cn('inline-flex items-center rounded px-2 py-0.5 text-xs font-medium ring-1 ring-inset', tones[tone])}>
      {children}
    </span>
  );
}