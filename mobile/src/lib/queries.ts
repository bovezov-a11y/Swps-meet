import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Platform } from 'react-native';

import { api } from './api';
import { saveDataExport } from './data-export';
import type {
  Account,
  BlockedUser,
  Campus,
  Intent,
  LikeResult,
  MatchSummary,
  Message,
  MyProfile,
  Photo,
  ProfileCard,
  ProfileInput,
  ReportReason,
} from './types';

export type DiscoverFilters = { campus?: Campus; program?: string; interest?: string };

export function useMyProfile() {
  return useQuery({ queryKey: ['profile'], queryFn: () => api<MyProfile>('/api/profile') });
}

export function useSaveProfile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: ProfileInput) => api<MyProfile>('/api/profile', { method: 'PUT', body: input }),
    onSuccess: (profile) => {
      queryClient.setQueryData(['profile'], profile);
      queryClient.setQueryData<Account>(['account'], (account) =>
        account ? { user: { ...account.user, firstName: profile.firstName }, hasProfile: true } : account,
      );
      void queryClient.invalidateQueries({ queryKey: ['discover'] });
    },
  });
}

export function useDiscover(intent: Intent | undefined, filters: DiscoverFilters) {
  const params = new URLSearchParams();
  if (intent) params.set('intent', intent);
  if (filters.campus) params.set('campus', filters.campus);
  if (filters.program?.trim()) params.set('program', filters.program.trim());
  if (filters.interest?.trim()) params.set('interest', filters.interest.trim());
  return useQuery({
    queryKey: ['discover', intent, filters],
    queryFn: () => api<ProfileCard[]>(`/api/discover?${params}`),
    enabled: intent !== undefined,
  });
}

export function usePerson(userId: string) {
  return useQuery({ queryKey: ['person', userId], queryFn: () => api<ProfileCard>(`/api/profiles/${userId}`) });
}

/** Everything that shows another student's like or match state. */
function invalidatePeople(queryClient: ReturnType<typeof useQueryClient>) {
  void queryClient.invalidateQueries({ queryKey: ['discover'] });
  void queryClient.invalidateQueries({ queryKey: ['person'] });
  void queryClient.invalidateQueries({ queryKey: ['matches'] });
}

export function useLike() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ userId, intent, liked }: { userId: string; intent: Intent; liked: boolean }) =>
      liked
        ? api<LikeResult>(`/api/likes/${userId}?intent=${intent}`, { method: 'PUT' })
        : api<void>(`/api/likes/${userId}?intent=${intent}`, { method: 'DELETE' }).then(
            (): LikeResult => ({ matched: false, matchId: null }),
          ),
    onSuccess: () => invalidatePeople(queryClient),
  });
}

export function useMatches() {
  return useQuery({
    queryKey: ['matches'],
    queryFn: () => api<MatchSummary[]>('/api/matches'),
    refetchInterval: 15_000,
  });
}

export function useUnmatch() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (matchId: string) => api<void>(`/api/matches/${matchId}`, { method: 'DELETE' }),
    onSuccess: () => invalidatePeople(queryClient),
  });
}

/** Polls every few seconds while the chat is open. */
export function useMessages(matchId: string) {
  return useQuery({
    queryKey: ['messages', matchId],
    queryFn: () => api<Message[]>(`/api/matches/${matchId}/messages`),
    refetchInterval: 4_000,
  });
}

export function useSendMessage(matchId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: string) =>
      api<Message>(`/api/matches/${matchId}/messages`, { method: 'POST', body: { body } }),
    onSuccess: (message) => {
      queryClient.setQueryData<Message[]>(['messages', matchId], (messages) => [...(messages ?? []), message]);
      void queryClient.invalidateQueries({ queryKey: ['matches'] });
    },
  });
}

export function useBlock() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (userId: string) => api<void>(`/api/blocks/${userId}`, { method: 'PUT' }),
    onSuccess: () => {
      invalidatePeople(queryClient);
      void queryClient.invalidateQueries({ queryKey: ['blocks'] });
    },
  });
}

export function useBlocked() {
  return useQuery({ queryKey: ['blocks'], queryFn: () => api<BlockedUser[]>('/api/blocks') });
}

export function useUnblock() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (userId: string) => api<void>(`/api/blocks/${userId}`, { method: 'DELETE' }),
    onSuccess: () => {
      invalidatePeople(queryClient);
      void queryClient.invalidateQueries({ queryKey: ['blocks'] });
    },
  });
}

export function useReport() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: { userId: string; reason: ReportReason; details: string }) =>
      api<void>('/api/reports', { method: 'POST', body: input }),
    onSuccess: () => invalidatePeople(queryClient),
  });
}

export type PickedImage = { uri: string; mimeType?: string | null; fileName?: string | null; file?: File };

export function useUploadPhoto() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (image: PickedImage) => {
      const form = new FormData();
      if (Platform.OS === 'web' && image.file) {
        form.append('file', image.file);
      } else {
        // React Native's FormData takes a { uri, name, type } object for files.
        form.append('file', {
          uri: image.uri,
          name: image.fileName ?? 'photo.jpg',
          type: image.mimeType ?? 'image/jpeg',
        } as unknown as Blob);
      }
      return api<Photo>('/api/profile/photos', { method: 'POST', form });
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['profile'] }),
  });
}

export function useDeletePhoto() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (photoId: string) => api<void>(`/api/profile/photos/${photoId}`, { method: 'DELETE' }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['profile'] }),
  });
}

export function useMakeMainPhoto() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (photoIds: string[]) =>
      api<Photo[]>('/api/profile/photos/order', { method: 'PUT', body: { photoIds } }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['profile'] }),
  });
}

/** A copy of everything stored about the student (GDPR right of access), saved as a JSON file. */
export function useExportData() {
  return useMutation({
    mutationFn: async () => {
      const data = await api<unknown>('/api/account/export');
      await saveDataExport(JSON.stringify(data, null, 2));
    },
  });
}

export function useDeleteAccount() {
  return useMutation({
    mutationFn: (password: string) => api<void>('/api/account', { method: 'DELETE', body: { password } }),
  });
}
