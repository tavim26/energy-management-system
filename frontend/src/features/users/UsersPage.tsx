import { useMemo, useState } from 'react';
import { Pencil, Plus, Trash2, Users } from 'lucide-react';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/Badge';
import { Button, IconButton } from '@/components/ui/Button';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { PageHeader, Panel } from '@/components/ui/Panel';
import { EmptyState, ErrorState, LoadingState } from '@/components/ui/States';
import { Table, Td, Th } from '@/components/ui/Table';
import { useCurrentUser } from '@/features/auth/AuthContext';
import { useDevices } from '@/features/devices/hooks';
import { getErrorMessage } from '@/lib/errors';
import { useDeleteUser, useUsers } from './hooks';
import { UserFormDialog } from './UserFormDialog';
import type { User } from './types';

export function UsersPage() {
  const currentUser = useCurrentUser();
  const users = useUsers();
  const devices = useDevices();
  const deleteUser = useDeleteUser();

  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<User | null>(null);
  const [deleting, setDeleting] = useState<User | null>(null);

  const deviceCounts = useMemo(() => {
    const counts = new Map<number, number>();

    for (const device of devices.data ?? []) {
      if (device.userId !== null) {
        counts.set(device.userId, (counts.get(device.userId) ?? 0) + 1);
      }
    }

    return counts;
  }, [devices.data]);

  function openCreate() {
    setEditing(null);
    setFormOpen(true);
  }

  function openEdit(user: User) {
    setEditing(user);
    setFormOpen(true);
  }

  async function handleDelete() {
    if (!deleting) return;

    try {
      await deleteUser.mutateAsync(deleting.id);
      toast.success(`${deleting.username ?? 'User'} deleted`);
      setDeleting(null);
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  }

  return (
    <>
      <PageHeader
        title="Users"
        description="Everyone who can log in, and the devices assigned to them."
        actions={
          <Button icon={<Plus className="size-4" aria-hidden />} onClick={openCreate}>
            Add user
          </Button>
        }
      />

      <Panel>
        {users.isPending ? (
          <LoadingState label="Loading users" />
        ) : users.isError ? (
          <ErrorState message={getErrorMessage(users.error)} onRetry={() => users.refetch()} />
        ) : users.data.length === 0 ? (
          <EmptyState
            icon={<Users className="size-5" aria-hidden />}
            title="No users yet"
            description="Add a client account, or let clients register themselves."
          />
        ) : (
          <Table>
            <thead>
              <tr>
                <Th>User</Th>
                <Th>Role</Th>
                <Th>Address</Th>
                <Th className="text-right">Devices</Th>
                <Th className="w-0">
                  <span className="sr-only">Actions</span>
                </Th>
              </tr>
            </thead>
            <tbody>
              {users.data.map((user) => {
                const isCurrentUser = user.id === currentUser.userId;

                return (
                  <tr key={user.id}>
                    <Td>
                      <div className="font-medium">
                        {user.username ?? <span className="text-muted">Account missing</span>}
                        {isCurrentUser && <span className="ml-2 text-xs font-normal text-muted">(you)</span>}
                      </div>
                      {user.fullName && <div className="text-xs text-muted">{user.fullName}</div>}
                    </Td>
                    <Td>
                      {user.role === 'ADMIN' && <Badge tone="brand">Administrator</Badge>}
                      {user.role === 'CLIENT' && <Badge>Client</Badge>}
                    </Td>
                    <Td className="text-muted">{user.address || '—'}</Td>
                    <Td className="tabular text-right">
                      {user.role === 'CLIENT' ? (deviceCounts.get(user.id) ?? 0) : '—'}
                    </Td>
                    <Td>
                      <div className="flex justify-end gap-1">
                        <IconButton label={`Edit ${user.username ?? 'user'}`} onClick={() => openEdit(user)}>
                          <Pencil className="size-4" aria-hidden />
                        </IconButton>
                        <IconButton
                          label={isCurrentUser ? 'You cannot delete your own account' : `Delete ${user.username ?? 'user'}`}
                          tone="danger"
                          disabled={isCurrentUser}
                          onClick={() => setDeleting(user)}
                        >
                          <Trash2 className="size-4" aria-hidden />
                        </IconButton>
                      </div>
                    </Td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
        )}
      </Panel>

      <UserFormDialog open={formOpen} user={editing} onClose={() => setFormOpen(false)} />
      <ConfirmDialog
        open={deleting !== null}
        title={`Delete ${deleting?.username ?? 'this user'}?`}
        description="They will no longer be able to log in, and their devices become unassigned."
        confirmLabel="Delete user"
        loading={deleteUser.isPending}
        onConfirm={handleDelete}
        onClose={() => setDeleting(null)}
      />
    </>
  );
}