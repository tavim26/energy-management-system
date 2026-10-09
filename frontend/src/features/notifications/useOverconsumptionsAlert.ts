import { useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import { useQueryClient } from '@tanstack/react-query';
import SockJS from 'sockjs-client';
import { toast } from 'sonner';
import { API_URL } from '@/config';
import { consumptionKey } from '@/features/monitoring/hooks';
import { formatHour, formatKwh } from '@/lib/format';

interface OverconsumptionAlert {
  type: 'OVERCONSUMPTION';
  deviceId: number;
  deviceName: string | null;
  consumption: number;
  limit: number;
  // Start of the hour, e.g. 2026-10-05T14:00
  timestamp: string | null;
}

const RECONNECT_DELAY_MS = 5000;

// Listens on the WebSocket topic of the logged-in client and shows each alert as a notification
export function useOverconsumptionAlerts(userId: number) {
  const queryClient = useQueryClient();

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_URL}/ws`),
      reconnectDelay: RECONNECT_DELAY_MS,
      onConnect: () => {
        client.subscribe(`/topic/notifications/${userId}`, (message) => {
          const alert = JSON.parse(message.body) as OverconsumptionAlert;
          const deviceName = alert.deviceName ?? `Device ${alert.deviceId}`;
          const period = alert.timestamp ? describeHour(alert.timestamp) : 'this hour';

          toast.warning(`${deviceName} is over its hourly limit`, {
            description: `${formatKwh(alert.consumption)} used ${period}. The limit is ${formatKwh(alert.limit)}.`,
            duration: 10_000,
          });

          // The chart of this device is out of date now
          void queryClient.invalidateQueries({ queryKey: consumptionKey(alert.deviceId) });
        });
      },
    });

    client.activate();

    return () => {
      void client.deactivate();
    };
  }, [userId, queryClient]);
}

function describeHour(timestamp: string): string {
  const hour = Number(timestamp.slice(11, 13));
  return `between ${formatHour(hour)} and ${formatHour((hour + 1) % 24)}`;
}