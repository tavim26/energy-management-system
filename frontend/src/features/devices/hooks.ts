import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { devicesApi } from './api';
import type { DeviceRequest } from './types';

// Every device query starts with this key, so one invalidation refreshes all of them
const DEVICES_KEY = ['devices'];

export function useDevices() {
  return useQuery({ queryKey: DEVICES_KEY, queryFn: devicesApi.list });
}

export function useUserDevices(userId: number) {
  return useQuery({
    queryKey: [...DEVICES_KEY, 'user', userId],
    queryFn: () => devicesApi.listForUser(userId),
  });
}

function useDeviceMutation<TVariables>(mutationFn: (variables: TVariables) => Promise<unknown>) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: DEVICES_KEY }),
  });
}

export function useCreateDevice() {
  return useDeviceMutation((request: DeviceRequest) => devicesApi.create(request));
}

export function useUpdateDevice() {
  return useDeviceMutation(({ id, request }: { id: number; request: DeviceRequest }) => devicesApi.update(id, request));
}

export function useDeleteDevice() {
  return useDeviceMutation((id: number) => devicesApi.remove(id));
}

export function useAssignDevice() {
  return useDeviceMutation(({ deviceId, userId }: { deviceId: number; userId: number }) =>
    devicesApi.assign(deviceId, userId),
  );
}

export function useUnassignDevice() {
  return useDeviceMutation((deviceId: number) => devicesApi.unassign(deviceId));
}