import { useEffect, useId, useRef, type MouseEvent, type ReactNode } from 'react';
import { X } from 'lucide-react';
import { IconButton } from './Button';

interface DialogProps {
  open: boolean;
  onClose: () => void;
  title: string;
  description?: string;
  children: ReactNode;
}

// Built on the native <dialog> element, which handles focus, the Escape key and the backdrop
export function Dialog({ open, onClose, title, description, children }: DialogProps) {
  const ref = useRef<HTMLDialogElement>(null);
  const titleId = useId();

  useEffect(() => {
    const dialog = ref.current;

    if (!dialog) {
      return;
    }

    if (open && !dialog.open) {
      dialog.showModal();
    } else if (!open && dialog.open) {
      dialog.close();
    }
  }, [open]);

  // A click on the backdrop targets the <dialog> element itself, not its content
  function handleClick(event: MouseEvent<HTMLDialogElement>) {
    if (event.target === event.currentTarget) {
      onClose();
    }
  }

  return (
    <dialog
      ref={ref}
      onClose={onClose}
      onClick={handleClick}
      aria-labelledby={titleId}
      className="m-auto w-[calc(100%-2rem)] max-w-md rounded-lg bg-surface p-0 text-ink shadow-xl backdrop:bg-ink/40"
    >
      {open && (
        <div className="p-6">
          <div className="mb-5 flex items-start justify-between gap-4">
            <div>
              <h2 id={titleId} className="text-lg font-semibold">
                {title}
              </h2>
              {description && <p className="mt-1 text-sm text-muted">{description}</p>}
            </div>
            <IconButton label="Close" onClick={onClose} className="-mr-2 -mt-1">
              <X className="size-4" aria-hidden />
            </IconButton>
          </div>
          {children}
        </div>
      )}
    </dialog>
  );
}

export function DialogActions({ children }: { children: ReactNode }) {
  return <div className="mt-6 flex justify-end gap-2">{children}</div>;
}