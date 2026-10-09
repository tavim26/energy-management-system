import { BellRing, PlugZap, TriangleAlert } from 'lucide-react';
import { PageHeader, Panel } from '@/components/ui/Panel';
import { EmptyState, ErrorState, LoadingState } from '@/components/ui/States';
import { useCurrentUser } from '@/features/auth/AuthContext';
import { useUserDevices } from '@/features/devices/hooks';
import {
  describeAlert,
  deviceLabel,
  useOverconsumptionAlerts,
  type ReceivedAlert,
} from '@/features/notifications/useOverconsumptionAlerts';
import { SupportChat } from '@/features/support/SupportChat';
import { getErrorMessage } from '@/lib/errors';
import { formatKwh } from '@/lib/format';

export function ClientDashboardPage() {
  const user = useCurrentUser();
  const devices = useUserDevices(user.userId);
  const alerts = useOverconsumptionAlerts(user.userId);

  // Devices with at least one alert in this session are highlighted in the list
  const alertedDeviceIds = new Set(alerts.map((alert) => alert.deviceId));

  return (
    <>
      <PageHeader
        title="My energy"
        description="Your devices and their hourly limits. You get an alert as soon as a device goes over its limit."
      />

      <div className="grid gap-6 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)]">
        <Panel>
          <div className="border-b border-line px-6 py-4">
            <h2 className="font-semibold">Devices</h2>
          </div>

          {devices.isPending ? (
            <LoadingState label="Loading your devices" />
          ) : devices.isError ? (
            <ErrorState message={getErrorMessage(devices.error)} onRetry={() => devices.refetch()} />
          ) : devices.data.length === 0 ? (
            <EmptyState
              icon={<PlugZap className="size-5" aria-hidden />}
              title="No devices yet"
              description="An administrator assigns devices to your account. They will appear here."
            />
          ) : (
            <ul className="divide-y divide-line">
              {devices.data.map((device) => (
                <li key={device.id} className="flex items-center justify-between gap-4 px-6 py-4">
                  <div className="flex items-center gap-3">
                    <span className="flex size-9 items-center justify-center rounded-md bg-energy-soft text-[#7a5600]">
                      <PlugZap className="size-4" aria-hidden />
                    </span>
                    <div>
                      <p className="font-medium">{device.name}</p>
                      <p className="tabular text-xs text-muted">
                        Limit {formatKwh(device.maxConsumption)} per hour
                      </p>
                    </div>
                  </div>
                  {alertedDeviceIds.has(device.id) && (
                    <span className="flex items-center gap-1 text-xs font-medium text-danger">
                      <TriangleAlert className="size-3.5" aria-hidden />
                      Over limit
                    </span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </Panel>

        <Panel>
          <div className="border-b border-line px-6 py-4">
            <h2 className="font-semibold">Alerts</h2>
            <p className="text-sm text-muted">Received while this page is open</p>
          </div>

          {alerts.length === 0 ? (
            <EmptyState
              icon={<BellRing className="size-5" aria-hidden />}
              title="No alerts"
              description="When a device uses more energy in an hour than its limit, the alert appears here and as a notification."
            />
          ) : (
            <ul className="divide-y divide-line" aria-live="polite">
              {alerts.map((alert) => (
                <AlertItem key={alert.id} alert={alert} />
              ))}
            </ul>
          )}
        </Panel>
      </div>

      <SupportChat />
    </>
  );
}

function AlertItem({ alert }: { alert: ReceivedAlert }) {
  return (
    <li className="flex gap-3 px-6 py-4">
      <TriangleAlert className="mt-0.5 size-4 shrink-0 text-danger" aria-hidden />
      <div>
        <p className="text-sm font-medium">{deviceLabel(alert)} went over its hourly limit</p>
        <p className="tabular text-sm text-muted">{describeAlert(alert)}</p>
        <p className="mt-1 text-xs text-muted">
          Received at {alert.receivedAt.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
        </p>
      </div>
    </li>
  );
}