import { useState } from 'react';
import { PlugZap } from 'lucide-react';
import { PageHeader, Panel } from '@/components/ui/Panel';
import { EmptyState, ErrorState, LoadingState } from '@/components/ui/States';
import { useCurrentUser } from '@/features/auth/AuthContext';
import { useUserDevices } from '@/features/devices/hooks';
import { DeviceConsumption } from '@/features/monitoring/DeviceConsumption';
import { useOverconsumptionAlerts } from '@/features/notifications/useOverconsumptionAlerts';
import { SupportChat } from '@/features/support/SupportChat';
import { cn } from '@/lib/cn';
import { getErrorMessage } from '@/lib/errors';
import { formatKwh } from '@/lib/format';

export function ClientDashboardPage() {
  const user = useCurrentUser();
  const devices = useUserDevices(user.userId);
  const [selectedId, setSelectedId] = useState<number | null>(null);

  useOverconsumptionAlerts(user.userId);

  const deviceList = devices.data ?? [];
  // The first device is shown until the client picks another one
  const selected = deviceList.find((device) => device.id === selectedId) ?? deviceList[0];

  return (
    <>
      <PageHeader title="My energy" description={`Hourly consumption of the devices assigned to ${user.username}.`} />

      {devices.isPending ? (
        <LoadingState label="Loading your devices" />
      ) : devices.isError ? (
        <Panel>
          <ErrorState message={getErrorMessage(devices.error)} onRetry={() => devices.refetch()} />
        </Panel>
      ) : !selected ? (
        <Panel>
          <EmptyState
            icon={<PlugZap className="size-5" aria-hidden />}
            title="No devices yet"
            description="An administrator assigns devices to your account. Their consumption will appear here."
          />
        </Panel>
      ) : (
        <div className="grid gap-6 lg:grid-cols-[16rem_minmax(0,1fr)]">
          <nav aria-label="My devices">
            <h2 className="mb-2 text-sm font-medium text-muted">Devices ({deviceList.length})</h2>
            <ul className="flex gap-2 overflow-x-auto pb-1 lg:flex-col lg:overflow-visible">
              {deviceList.map((device) => {
                const isSelected = device.id === selected.id;

                return (
                  <li key={device.id} className="shrink-0">
                    <button
                      type="button"
                      aria-current={isSelected ? 'true' : undefined}
                      onClick={() => setSelectedId(device.id)}
                      className={cn(
                        'w-full rounded-md border px-4 py-3 text-left transition-colors',
                        isSelected
                          ? 'border-brand bg-surface shadow-[inset_3px_0_0_var(--color-brand)]'
                          : 'border-line bg-surface hover:border-muted/40',
                      )}
                    >
                      <span className="block font-medium">{device.name}</span>
                      <span className="tabular block text-xs text-muted">
                        Limit {formatKwh(device.maxConsumption)} per hour
                      </span>
                    </button>
                  </li>
                );
              })}
            </ul>
          </nav>

          <Panel>
            {/* The key resets the selected day when another device is chosen */}
            <DeviceConsumption key={selected.id} device={selected} />
          </Panel>
        </div>
      )}

      <SupportChat />
    </>
  );
}