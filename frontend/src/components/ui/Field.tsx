import { useId, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes } from 'react';
import { cn } from '@/lib/cn';

const controlClasses =
  'h-10 w-full rounded-md border border-line bg-surface px-3 text-sm text-ink placeholder:text-muted/70 ' +
  'focus:border-brand focus:outline-none focus:ring-2 focus:ring-brand/20 disabled:bg-paper disabled:text-muted';

interface FieldShellProps {
  id: string;
  label: string;
  hint?: string;
  children: ReactNode;
}

function FieldShell({ id, label, hint, children }: FieldShellProps) {
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-sm font-medium text-ink">
        {label}
      </label>
      {children}
      {hint && (
        <p id={`${id}-hint`} className="text-xs text-muted">
          {hint}
        </p>
      )}
    </div>
  );
}

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  hint?: string;
}

export function TextField({ label, hint, id, className, ...props }: TextFieldProps) {
  const generatedId = useId();
  const inputId = id ?? generatedId;

  return (
    <FieldShell id={inputId} label={label} hint={hint}>
      <input
        id={inputId}
        className={cn(controlClasses, className)}
        aria-describedby={hint ? `${inputId}-hint` : undefined}
        {...props}
      />
    </FieldShell>
  );
}

interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  hint?: string;
  children: ReactNode;
}

export function SelectField({ label, hint, id, className, children, ...props }: SelectFieldProps) {
  const generatedId = useId();
  const selectId = id ?? generatedId;

  return (
    <FieldShell id={selectId} label={label} hint={hint}>
      <select
        id={selectId}
        className={cn(controlClasses, 'pr-8', className)}
        aria-describedby={hint ? `${selectId}-hint` : undefined}
        {...props}
      >
        {children}
      </select>
    </FieldShell>
  );
}