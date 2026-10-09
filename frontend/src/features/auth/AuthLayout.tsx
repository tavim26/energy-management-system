import type { ReactNode } from 'react';
import { Logo } from '@/components/layout/Logo';

// Heights of the bars in the decorative chart: a typical day with an evening peak
const SAMPLE_DAY = [22, 18, 16, 15, 15, 18, 34, 48, 40, 30, 28, 27, 31, 29, 27, 30, 41, 62, 78, 86, 70, 52, 38, 28];
const SAMPLE_LIMIT = 75;

interface AuthLayoutProps {
  title: string;
  description: string;
  children: ReactNode;
}

export function AuthLayout({ title, description, children }: AuthLayoutProps) {
  return (
    <div className="grid min-h-screen lg:grid-cols-[minmax(0,5fr)_minmax(0,6fr)]">
      <aside className="hidden flex-col justify-between bg-ink p-12 text-white lg:flex">
        <Logo />

        <div>
          <p className="max-w-sm font-display text-4xl font-semibold leading-tight">
            See how much energy every device uses, hour by hour.
          </p>

          <svg viewBox="0 0 240 100" className="mt-10 w-full max-w-md" aria-hidden>
            {SAMPLE_DAY.map((value, hour) => (
              <rect
                key={hour}
                x={hour * 10 + 1}
                y={100 - value}
                width={7}
                height={value}
                rx={1}
                fill={value > SAMPLE_LIMIT ? '#e0644f' : '#e9a300'}
              />
            ))}
            <line x1="0" x2="240" y1={100 - SAMPLE_LIMIT} y2={100 - SAMPLE_LIMIT} stroke="#e0644f" strokeDasharray="3 3" />
          </svg>
          <p className="mt-4 max-w-sm text-sm text-white/70">
            Alerts arrive in real time when a device goes over its hourly limit.
          </p>
        </div>

        <p className="text-xs text-white/50">Energy Management System</p>
      </aside>

      <main className="flex items-center justify-center px-4 py-12 sm:px-8">
        <div className="w-full max-w-sm">
          <div className="mb-10 lg:hidden">
            <Logo />
          </div>
          <h1 className="font-display text-3xl font-semibold tracking-tight">{title}</h1>
          <p className="mt-1.5 text-sm text-muted">{description}</p>
          <div className="mt-8">{children}</div>
        </div>
      </main>
    </div>
  );
}