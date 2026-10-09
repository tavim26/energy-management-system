import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { usersApi } from './api';
import type { CreateUserRequest, UpdateUserRequest } from './types';

const USERS_KEY = ['users'];

export function useUsers() {
  return useQuery({ queryKey: USERS_KEY, queryFn: usersApi.list });
}

export function useCreateUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateUserRequest) => usersApi.create(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: USERS_KEY }),
  });
}

export function useUpdateUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateUserRequest }) => usersApi.update(id, request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: USERS_KEY }),
  });
}

export function useDeleteUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => usersApi.remove(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: USERS_KEY });
      // The devices of a deleted user become unassigned
      await queryClient.invalidateQueries({ queryKey: ['devices'] });
    },
  });
}