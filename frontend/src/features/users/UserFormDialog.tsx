import { useState, type FormEvent } from 'react';
import { toast } from 'sonner';
import { ErrorMessage } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Dialog, DialogActions } from '@/components/ui/Dialog';
import { SelectField, TextField } from '@/components/ui/Field';
import type { Role } from '@/features/auth/types';
import { getErrorMessage } from '@/lib/errors';
import { useCreateUser, useUpdateUser } from './hooks';
import type { User } from './types';

interface UserFormDialogProps {
  open: boolean;
  // Editing when a user is given, creating otherwise
  user: User | null;
  onClose: () => void;
}

export function UserFormDialog({ open, user, onClose }: UserFormDialogProps) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      title={user ? `Edit ${user.username ?? 'user'}` : 'Add user'}
      description={user ? 'Username and role cannot be changed.' : 'The user can log in right away with these credentials.'}
    >
      {user ? <EditUserForm user={user} onDone={onClose} /> : <CreateUserForm onDone={onClose} />}
    </Dialog>
  );
}

function CreateUserForm({ onDone }: { onDone: () => void }) {
  const createUser = useCreateUser();
  const [form, setForm] = useState({ username: '', password: '', role: 'CLIENT' as Role, fullName: '', address: '' });
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    try {
      await createUser.mutateAsync({
        username: form.username.trim(),
        password: form.password,
        role: form.role,
        fullName: form.fullName.trim() || undefined,
        address: form.address.trim() || undefined,
      });
      toast.success(`User ${form.username.trim()} added`);
      onDone();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      {error && <ErrorMessage message={error} />}

      <div className="grid gap-4 sm:grid-cols-2">
        <TextField
          label="Username"
          autoFocus
          autoComplete="off"
          value={form.username}
          onChange={(event) => setForm({ ...form, username: event.target.value })}
          required
          minLength={3}
        />
        <SelectField
          label="Role"
          value={form.role}
          onChange={(event) => setForm({ ...form, role: event.target.value as Role })}
        >
          <option value="CLIENT">Client</option>
          <option value="ADMIN">Administrator</option>
        </SelectField>
      </div>
      <TextField
        label="Password"
        type="password"
        autoComplete="new-password"
        hint="At least 6 characters"
        value={form.password}
        onChange={(event) => setForm({ ...form, password: event.target.value })}
        required
        minLength={6}
      />
      <TextField
        label="Full name"
        value={form.fullName}
        onChange={(event) => setForm({ ...form, fullName: event.target.value })}
      />
      <TextField
        label="Address"
        value={form.address}
        onChange={(event) => setForm({ ...form, address: event.target.value })}
      />

      <DialogActions>
        <Button variant="secondary" onClick={onDone}>
          Cancel
        </Button>
        <Button type="submit" loading={createUser.isPending}>
          Add user
        </Button>
      </DialogActions>
    </form>
  );
}

function EditUserForm({ user, onDone }: { user: User; onDone: () => void }) {
  const updateUser = useUpdateUser();
  const [form, setForm] = useState({ fullName: user.fullName ?? '', address: user.address ?? '' });
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    try {
      await updateUser.mutateAsync({ id: user.id, request: form });
      toast.success('Changes saved');
      onDone();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      {error && <ErrorMessage message={error} />}

      <TextField
        label="Full name"
        autoFocus
        value={form.fullName}
        onChange={(event) => setForm({ ...form, fullName: event.target.value })}
      />
      <TextField
        label="Address"
        value={form.address}
        onChange={(event) => setForm({ ...form, address: event.target.value })}
      />

      <DialogActions>
        <Button variant="secondary" onClick={onDone}>
          Cancel
        </Button>
        <Button type="submit" loading={updateUser.isPending}>
          Save changes
        </Button>
      </DialogActions>
    </form>
  );
}