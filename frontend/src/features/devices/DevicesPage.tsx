import { useMemo, useState } from 'react';
import { Link2, Pencil, PlugZap, Plus, Trash2, Unlink } from 'lucide-react';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/Badge';
import { Button, IconButton } from '@/components/ui/Button';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { PageHeader, Panel } from '@/components/ui/Panel';
import { EmptyState, ErrorState, LoadingState } from '@/components/ui/States';
import { Table, Td, Th } from '@/components/ui/Table';
import { useUsers } from '@/features/users/hooks';
import { cn } from '@/lib/cn';
import { getErrorMessage } from '@/lib/errors';
import { formatKwh } from '@/lib/format';
import { AssignDeviceDialog } from './AssignDeviceDialog';
import { DeviceFormDialog } from './DeviceFormDialog';
import { useDeleteDevice, useDevices, useUnassignDevice } from './hooks';
import type { Device } from './types';

type Filter = 'all' | 'assigned' | 'unassigned';

const FILTERS: Array<{ value: Filter; label: string }> = [
  { value: 'all', label: 'All' },
  { value: 'assigned', label: 'Assigned' },
  { value: 'unassigned', label: 'Unassigned' },
];

export function DevicesPage() {
  const devices = useDevices();
  const users = useUsers();
  const deleteDevice = useDeleteDevice();
  const unassignDevice = useUnassignDevice();

  const [filter, setFilter] = useState<Filter>('all');
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<Device | null>(null);
  const [assigning, setAssigning] = useState<Device | null>(null);
  const [deleting, setDeleting] = useState<Device | null>(null);

  const usernames = useMemo(
    () => new Map((users.data ?? []).map((user) => [user.id, user.username ?? `User ${user.id}`])),
    [users.data],
  );

  const visibleDevices = (devices.data ?? []).filter((device) => {
    if (filter === 'assigned') return device.userId !== null;
    if (filter === 'unassigned') return device.userId === null;
    return true;
  });

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(device: Device) {
    setEditing(device);
    setFormOpen(true);
  }

  async function handleUnassign(device: Device) {
    try {
      await unassignDevice.mutateAsync(device.id);
      toast.success(`${device.name} unassigned`);
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  }

  async function handleDelete() {
    if (!deleting) return;

    try {
      await deleteDevice.mutateAsync(deleting.id);
      toast.success(`${deleting.name} deleted`);
      setDeleting(null);
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  }

  return (
    <>
      <PageHeader
        title="Devices"
        description="Smart meters and the clients they belong to."
        actions={
          <Button icon={<Plus className="size-4" aria-hidden />} onClick={openCreate}>
            Add device
          </Button>
        }
      />

      <Panel>
        <div className="flex gap-1 border-b border-line px-4 py-3 sm:px-6" role="group" aria-label="Filter devices">
          {FILTERS.map((option) => (
            <button
              key={option.value}
              type="button"
              aria-pressed={filter === option.value}
              onClick={() => setFilter(option.value)}
              className={cn(
                'rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
                filter === option.value ? 'bg-ink text-white' : 'text-muted hover:bg-paper hover:text-ink',
              )}
            >
              {option.label}
            </button>
          ))}
        </div>

        {devices.isPending ? (
          <LoadingState label="Loading devices" />
        ) : devices.isError ? (
          <ErrorState message={getErrorMessage(devices.error)} onRetry={() => devices.refetch()} />
        ) : visibleDevices.length === 0 ? (
          <EmptyState
            icon={<PlugZap className="size-5" aria-hidden />}
            title={filter === 'all' ? 'No devices yet' : `No ${filter} devices`}
            description={
              filter === 'all'
                ? 'Add a device, then assign it to a client so they can follow its consumption.'
                : 'Devices that match this filter will appear here.'
            }
            action={
              filter === 'all' && (
                <Button icon={<Plus className="size-4" aria-hidden />} onClick={openCreate}>
                  Add device
                </Button>
              )
            }
          />
        ) : (
          <Table>
            <thead>
              <tr>
                <Th>Device</Th>
                <Th className="text-right">Hourly limit</Th>
                <Th>Owner</Th>
                <Th className="w-0">
                  <span className="sr-only">Actions</span>
                </Th>
              </tr>
            </thead>
            <tbody>
              {visibleDevices.map((device) => (
                <tr key={device.id}>
                  <Td className="font-medium">{device.name}</Td>
                  <Td className="tabular text-right">{formatKwh(device.maxConsumption)}</Td>
                  <Td>
                    {device.userId === null ? (
                      <Badge tone="energy">Unassigned</Badge>
                    ) : (
                      usernames.get(device.userId) ?? `User ${device.userId}`
                    )}
                  </Td>
                  <Td>
                    <div className="flex justify-end gap-1">
                      {device.userId === null ? (
                        <Button
                          variant="secondary"
                          size="sm"
                          icon={<Link2 className="size-4" aria-hidden />}
                          onClick={() => setAssigning(device)}
                        >
                          Assign
                        </Button>
                      ) : (
                        <Button
                          variant="ghost"
                          size="sm"
                          icon={<Unlink className="size-4" aria-hidden />}
                          disabled={unassignDevice.isPending}
                          onClick={() => handleUnassign(device)}
                        >
                          Unassign
                        </Button>
                      )}
                      <IconButton label={`Edit ${device.name}`} onClick={() => openEdit(device)}>
                        <Pencil className="size-4" aria-hidden />
                      </IconButton>
                      <IconButton label={`Delete ${device.name}`} tone="danger" onClick={() => setDeleting(device)}>
                        <Trash2 className="size-4" aria-hidden />
                      </IconButton>
                    </div>
                  </Td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}
      </Panel>

      <DeviceFormDialog open={formOpen} device={editing} onClose={() => setFormOpen(false)} />
      <AssignDeviceDialog device={assigning} onClose={() => setAssigning(null)} />
      <ConfirmDialog
        open={deleting !== null}
        title={`Delete ${deleting?.name ?? 'device'}?`}
        description="Its consumption history is deleted as well. This cannot be undone."
        confirmLabel="Delete device"
        loading={deleteDevice.isPending}
        onConfirm={handleDelete}
        onClose={() => setDeleting(null)}
      />
    </>
  );
}