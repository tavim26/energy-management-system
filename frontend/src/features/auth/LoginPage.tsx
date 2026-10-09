import { useState, type FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router';
import { ErrorMessage } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { TextField } from '@/components/ui/Field';
import { getErrorMessage } from '@/lib/errors';
import { homePathFor, useAuth } from './AuthContext';
import { AuthLayout } from './AuthLayout';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // The registration page passes the new username, so the user only types the password
  const registeredUsername = (location.state as { username?: string } | null)?.username ?? '';

  const [username, setUsername] = useState(registeredUsername);
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const user = await login({ username: username.trim(), password });
      navigate(homePathFor(user.role), { replace: true });
    } catch (err) {
      setError(getErrorMessage(err, 'Login failed. Please try again.'));
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout title="Log in" description="Use the account created for you or the one you registered.">
      <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
        {error && <ErrorMessage message={error} />}

        <TextField
          label="Username"
          name="username"
          autoComplete="username"
          autoFocus={!registeredUsername}
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          required
        />
        <TextField
          label="Password"
          name="password"
          type="password"
          autoComplete="current-password"
          autoFocus={Boolean(registeredUsername)}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />

        <Button type="submit" loading={submitting} disabled={!username.trim() || !password} className="mt-2">
          Log in
        </Button>
      </form>

      <p className="mt-6 text-sm text-muted">
        New here?{' '}
        <Link to="/register" className="font-medium text-brand hover:underline">
          Create an account
        </Link>
      </p>
    </AuthLayout>
  );
}