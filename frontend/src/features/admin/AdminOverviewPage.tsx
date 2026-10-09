import { useState } from 'react';
import { Link } from 'react-router';
import { CircleCheck, Link2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { PageHeader, Panel } from '@/components/ui/Panel';
import { EmptyState, ErrorState, LoadingState } from '@/components/ui/States';
import { useCurrentUser } from '@/features/auth/AuthContext';
import { AssignDeviceDialog } from '@/features/devices/AssignDeviceDialog';
import { useDevices } from '@/features/devices/hooks';
import type { Device } from '@/features/devices/types';
import { useUsers } from '@/features/users/hooks';
import { getErrorMessage } from '@/lib/errors';
import { formatKwh } from '@/lib/format';

export function AdminOverviewPage() {
  const user = useCurrentUser();
  const devices = useDevices();
  const users = useUsers();
  const [assigning, setAssigning] = useState<Device | null>(null);

  const clientCount = (users.data ?? []).filter((account) => account.role === 'CLIENT').length;
  const unassigned = (devices.data ?? []).filter((device) => device.userId === null);

  return (
    <>
      <PageHeader title="Overview" description={`Logged in as ${user.username}.`} />

      <dl className="mb-8 grid grid-cols-3 divide-x divide-line rounded-lg border border-line bg-surface">
        <Figure label="Clients" value={users.data ? clientCount : null} to="/admin/users" />
        <Figure label="Devices" value={devices.data ? devices.data.length : null} to="/admin/devices" />
        <Figure label="Without an owner" value={devices.data ? unassigned.length : null} to="/admin/devices" />
      </dl>

      <Panel>
        <div className="border-b border-line px-6 py-4">
          <h2 className="font-semibold">Devices waiting for an owner</h2>
          <p className="text-sm text-muted">Clients only see the consumption of devices assigned to them.</p>
        </div>

        {devices.isPending ? (
          <LoadingState label="Loading devices" />
        ) : devices.isError ? (
          <ErrorState message={getErrorMessage(devices.error)} onRetry={() => devices.refetch()} />
        ) : unassigned.length === 0 ? (
          <EmptyState
            icon={<CircleCheck className="size-5" aria-hidden />}
            title="Every device has an owner"
            description="New devices appear here until you assign them to a client."
          />
        ) : (
          <ul className="divide-y divide-line">
            {unassigned.map((device) => (
              <li key={device.id} className="flex items-center justify-between gap-4 px-6 py-3">
                <div>
                  <p className="font-medium">{device.name}</p>
                  <p className="tabular text-xs text-muted">Limit {formatKwh(device.maxConsumption)} per hour</p>
                </div>
                <Button
                  variant="secondary"
                  size="sm"
                  icon={<Link2 className="size-4" aria-hidden />}
                  onClick={() => setAssigning(device)}
                >
                  Assign
                </Button>
              </li>
            ))}
          </ul>
        )}
      </Panel>

      <AssignDeviceDialog device={assigning} onClose={() => setAssigning(null)} />
    </>
  );
}

function Figure({ label, value, to }: { label: string; value: number | null; to: string }) {
  return (
    <Link to={to} className="group px-4 py-4 hover:bg-paper sm:px-6 sm:py-5">
      <dt className="text-sm text-muted group-hover:text-ink">{label}</dt>
      <dd className="tabular mt-1 font-display text-3xl font-semibold">{value ?? '–'}</dd>
    </Link>
  );
}