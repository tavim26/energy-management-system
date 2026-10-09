import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router';
import { toast } from 'sonner';
import { ErrorMessage } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { TextField } from '@/components/ui/Field';
import { getErrorMessage } from '@/lib/errors';
import { authApi } from './api';
import { AuthLayout } from './AuthLayout';

const MIN_USERNAME_LENGTH = 3;
const MIN_PASSWORD_LENGTH = 6;

export function RegisterPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({ username: '', password: '', fullName: '', address: '' });
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function update(field: keyof typeof form, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    const username = form.username.trim();

    if (username.length < MIN_USERNAME_LENGTH) {
      setError(`Username must have at least ${MIN_USERNAME_LENGTH} characters.`);
      return;
    }

    if (form.password.length < MIN_PASSWORD_LENGTH) {
      setError(`Password must have at least ${MIN_PASSWORD_LENGTH} characters.`);
      return;
    }

    setSubmitting(true);

    try {
      await authApi.register({
        username,
        password: form.password,
        fullName: form.fullName.trim() || undefined,
        address: form.address.trim() || undefined,
      });

      toast.success('Account created. You can log in now.');
      navigate('/login', { state: { username } });
    } catch (err) {
      setError(getErrorMessage(err, 'Registration failed. Please try again.'));
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout title="Create an account" description="Client accounts can see the devices an administrator assigns to them.">
      <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
        {error && <ErrorMessage message={error} />}

        <TextField
          label="Username"
          autoComplete="username"
          autoFocus
          value={form.username}
          onChange={(event) => update('username', event.target.value)}
          hint={`At least ${MIN_USERNAME_LENGTH} characters`}
          required
        />
        <TextField
          label="Password"
          type="password"
          autoComplete="new-password"
          value={form.password}
          onChange={(event) => update('password', event.target.value)}
          hint={`At least ${MIN_PASSWORD_LENGTH} characters`}
          required
        />
        <TextField
          label="Full name"
          autoComplete="name"
          value={form.fullName}
          onChange={(event) => update('fullName', event.target.value)}
        />
        <TextField
          label="Address"
          autoComplete="street-address"
          value={form.address}
          onChange={(event) => update('address', event.target.value)}
        />

        <Button type="submit" loading={submitting} className="mt-2">
          Create account
        </Button>
      </form>

      <p className="mt-6 text-sm text-muted">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-brand hover:underline">
          Log in
        </Link>
      </p>
    </AuthLayout>
  );
}