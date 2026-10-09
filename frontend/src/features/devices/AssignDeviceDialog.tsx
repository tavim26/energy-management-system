import { useState, type FormEvent } from 'react';
import { toast } from 'sonner';
import { ErrorMessage } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Dialog, DialogActions } from '@/components/ui/Dialog';
import { SelectField } from '@/components/ui/Field';
import { useUsers } from '@/features/users/hooks';
import { getErrorMessage } from '@/lib/errors';
import { useAssignDevice } from './hooks';
import type { Device } from './types';

interface AssignDeviceDialogProps {
  device: Device | null;
  onClose: () => void;
}

export function AssignDeviceDialog({ device, onClose }: AssignDeviceDialogProps) {
  return (
    <Dialog
      open={device !== null}
      onClose={onClose}
      title={device ? `Assign ${device.name}` : 'Assign device'}
      description="The client sees the consumption of this device and receives its alerts."
    >
      {device && <AssignForm device={device} onDone={onClose} />}
    </Dialog>
  );
}

function AssignForm({ device, onDone }: { device: Device; onDone: () => void }) {
  const users = useUsers();
  const assignDevice = useAssignDevice();
  const [userId, setUserId] = useState('');
  const [error, setError] = useState<string | null>(null);

  // Only clients can own devices
  const clients = (users.data ?? []).filter((user) => user.role === 'CLIENT');

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    try {
      await assignDevice.mutateAsync({ deviceId: device.id, userId: Number(userId) });
      const client = clients.find((user) => user.id === Number(userId));
      toast.success(`${device.name} assigned to ${client?.username ?? 'the client'}`);
      onDone();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      {error && <ErrorMessage message={error} />}
      {users.isError && <ErrorMessage message={getErrorMessage(users.error, 'The client list could not be loaded.')} />}

      <SelectField
        label="Client"
        autoFocus
        value={userId}
        onChange={(event) => setUserId(event.target.value)}
        disabled={users.isPending}
        hint={!users.isPending && clients.length === 0 ? 'There are no client accounts yet.' : undefined}
        required
      >
        <option value="" disabled>
          {users.isPending ? 'Loading clients…' : 'Choose a client'}
        </option>
        {clients.map((client) => (
          <option key={client.id} value={client.id}>
            {client.username}
            {client.fullName ? ` (${client.fullName})` : ''}
          </option>
        ))}
      </SelectField>

      <DialogActions>
        <Button variant="secondary" onClick={onDone}>
          Cancel
        </Button>
        <Button type="submit" loading={assignDevice.isPending} disabled={!userId}>
          Assign device
        </Button>
      </DialogActions>
    </form>
  );
}