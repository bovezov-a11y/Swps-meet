import { useQuery, useQueryClient } from '@tanstack/react-query';
import { createContext, type ReactNode, use, useCallback, useEffect, useMemo, useState } from 'react';

import { api, setApiToken } from './api';
import { getItem, setItem } from './storage';
import type { Account, AuthResponse } from './types';

const TOKEN_KEY = 'meetswps.token';

type Session = {
  /** True until the stored token has been read and, if present, the account loaded. */
  loading: boolean;
  token: string | null;
  account: Account | undefined;
  signIn: (auth: AuthResponse) => Promise<void>;
  signOut: () => Promise<void>;
};

const SessionContext = createContext<Session | null>(null);

export function SessionProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [token, setToken] = useState<string | null>(null);
  const [restored, setRestored] = useState(false);

  const signOut = useCallback(async () => {
    setApiToken(null);
    setToken(null);
    queryClient.clear();
    await setItem(TOKEN_KEY, null);
  }, [queryClient]);

  useEffect(() => {
    getItem(TOKEN_KEY)
      .then((stored) => {
        setApiToken(stored, () => void signOut());
        setToken(stored);
      })
      .finally(() => setRestored(true));
  }, [signOut]);

  const account = useQuery({
    queryKey: ['account'],
    queryFn: () => api<Account>('/api/account'),
    enabled: token !== null,
  });

  const signIn = useCallback(
    async (auth: AuthResponse) => {
      await setItem(TOKEN_KEY, auth.token);
      setApiToken(auth.token, () => void signOut());
      queryClient.clear();
      // Load the account before switching screens, so the navigator goes straight to onboarding or the app.
      await queryClient.prefetchQuery({ queryKey: ['account'], queryFn: () => api<Account>('/api/account') });
      setToken(auth.token);
    },
    [queryClient, signOut],
  );

  const value = useMemo<Session>(
    () => ({
      loading: !restored || (token !== null && account.isPending),
      token,
      account: account.data,
      signIn,
      signOut,
    }),
    [restored, token, account.isPending, account.data, signIn, signOut],
  );

  return <SessionContext value={value}>{children}</SessionContext>;
}

export function useSession() {
  const session = use(SessionContext);
  if (!session) throw new Error('useSession must be used inside SessionProvider');
  return session;
}
