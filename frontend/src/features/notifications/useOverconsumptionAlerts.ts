import { useEffect, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { toast } from 'sonner';
import { API_URL } from '@/config';
import { formatHour, formatKwh } from '@/lib/format';

export interface OverconsumptionAlert {
  type: 'OVERCONSUMPTION';
  deviceId: number;
  deviceName: string | null;
  consumption: number;
  limit: number;
  // Start of the hour, e.g. 2026-10-05T14:00
  timestamp: string | null;
}

export interface ReceivedAlert extends OverconsumptionAlert {
  id: number;
  receivedAt: Date;
}

const RECONNECT_DELAY_MS = 5000;
const MAX_KEPT_ALERTS = 20;

// Listens on the WebSocket topic of the logged-in client, shows each alert as a notification
// and returns the alerts received since the page was opened, newest first
export function useOverconsumptionAlerts(userId: number): ReceivedAlert[] {
  const [alerts, setAlerts] = useState<ReceivedAlert[]>([]);

  useEffect(() => {
    let nextId = 0;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_URL}/ws`),
      reconnectDelay: RECONNECT_DELAY_MS,
      onConnect: () => {
        client.subscribe(`/topic/notifications/${userId}`, (message) => {
          const alert = JSON.parse(message.body) as OverconsumptionAlert;

          toast.warning(`${deviceLabel(alert)} is over its hourly limit`, {
            description: describeAlert(alert),
            duration: 10_000,
          });

          nextId += 1;
          const received: ReceivedAlert = { ...alert, id: nextId, receivedAt: new Date() };
          setAlerts((current) => [received, ...current].slice(0, MAX_KEPT_ALERTS));
        });
      },
    });

    client.activate();

    return () => {
      void client.deactivate();
    };
  }, [userId]);

  return alerts;
}

export function deviceLabel(alert: OverconsumptionAlert): string {
  return alert.deviceName ?? `Device ${alert.deviceId}`;
}

export function describeAlert(alert: OverconsumptionAlert): string {
  const period = alert.timestamp ? describeHour(alert.timestamp) : 'this hour';
  return `${formatKwh(alert.consumption)} used ${period}. The limit is ${formatKwh(alert.limit)}.`;
}

function describeHour(timestamp: string): string {
  const hour = Number(timestamp.slice(11, 13));
  return `between ${formatHour(hour)} and ${formatHour((hour + 1) % 24)}`;
}