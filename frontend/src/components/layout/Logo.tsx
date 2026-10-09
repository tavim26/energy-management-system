import { Zap } from 'lucide-react';

export function Logo() {
  return (
    <div className="flex items-center gap-2.5">
      <span className="flex size-8 items-center justify-center rounded-md bg-brand">
        <Zap className="size-4 fill-energy text-energy" aria-hidden />
      </span>
      <span className="font-display text-lg font-semibold leading-none tracking-tight">Energy Management</span>
    </div>
  );
}