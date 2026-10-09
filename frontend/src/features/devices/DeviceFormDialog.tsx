import { useState, type FormEvent } from 'react';
import { toast } from 'sonner';
import { ErrorMessage } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Dialog, DialogActions } from '@/components/ui/Dialog';
import { TextField } from '@/components/ui/Field';
import { getErrorMessage } from '@/lib/errors';
import { useCreateDevice, useUpdateDevice } from './hooks';
import type { Device } from './types';

interface DeviceFormDialogProps {
  open: boolean;
  // Editing when a device is given, creating otherwise
  device: Device | null;
  onClose: () => void;
}

export function DeviceFormDialog({ open, device, onClose }: DeviceFormDialogProps) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={device ? `Edit ${device.name}` : 'Add device'}
      description="The hourly limit decides when the owner receives an overconsumption alert."
    >
      <DeviceForm device={device} onDone={onClose} />
    </Dialog>
  );
}

function DeviceForm({ device, onDone }: { device: Device | null; onDone: () => void }) {
  const createDevice = useCreateDevice();
  const updateDevice = useUpdateDevice();

  const [name, setName] = useState(device?.name ?? '');
  const [limit, setLimit] = useState(device ? String(device.maxConsumption) : '');
  const [error, setError] = useState<string | null>(null);

  const saving = createDevice.isPending || updateDevice.isPending;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    const maxConsumption = Number(limit);

    if (!Number.isFinite(maxConsumption) || maxConsumption <= 0) {
      setError('The hourly limit must be a number greater than 0.');
      return;
    }

    const request = { name: name.trim(), maxConsumption };

    try {
      if (device) {
        await updateDevice.mutateAsync({ id: device.id, request });
        toast.success('Changes saved');
      } else {
        await createDevice.mutateAsync(request);
        toast.success(`Device ${request.name} added`);
      }
      onDone();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      {error && <ErrorMessage message={error} />}

      <TextField
        label="Name"
        autoFocus
        placeholder="e.g. Kitchen fridge"
        value={name}
        onChange={(event) => setName(event.target.value)}
        required
        maxLength={150}
      />
      <TextField
        label="Hourly limit (kWh)"
        type="number"
        inputMode="decimal"
        min="0.01"
        step="0.01"
        placeholder="e.g. 1.5"
        value={limit}
        onChange={(event) => setLimit(event.target.value)}
        required
      />

      <DialogActions>
        <Button variant="secondary" onClick={onDone}>
          Cancel
        </Button>
        <Button type="submit" loading={saving} disabled={!name.trim() || !limit}>
          {device ? 'Save changes' : 'Add device'}
        </Button>
      </DialogActions>
    </form>
  );
}