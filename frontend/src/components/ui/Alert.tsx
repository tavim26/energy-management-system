import { CircleAlert } from 'lucide-react';

// Inline error shown inside forms and panels
export function ErrorMessage({ message }: { message: string }) {
  return (
    <div role="alert" className="flex items-start gap-2 rounded-md bg-danger-soft px-3 py-2.5 text-sm text-danger">
      <CircleAlert className="mt-0.5 size-4 shrink-0" aria-hidden />
      <span>{message}</span>
    </div>
  );
}