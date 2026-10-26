import * as ImagePicker from 'expo-image-picker';
import { router } from 'expo-router';
import { useState } from 'react';
import { Alert, Pressable, View } from 'react-native';

import { Photo } from '@/components/photo';
import { Button, Card, Field, Loading, Screen, T } from '@/components/ui';
import { ApiError } from '@/lib/api';
import { useSession } from '@/lib/auth';
import { campusLabels, intentLabels, studyLine } from '@/lib/labels';
import {
  useDeleteAccount,
  useDeletePhoto,
  useExportData,
  useMakeMainPhoto,
  useMyProfile,
  useUploadPhoto,
} from '@/lib/queries';
import { radius, space, useColors } from '@/lib/theme';

const MAX_PHOTOS = 3;

export default function MeScreen() {
  const c = useColors();
  const { signOut } = useSession();
  const profile = useMyProfile();
  const upload = useUploadPhoto();
  const deletePhoto = useDeletePhoto();
  const makeMain = useMakeMainPhoto();
  const deleteAccount = useDeleteAccount();
  const exportData = useExportData();
  const [deleting, setDeleting] = useState(false);
  const [password, setPassword] = useState('');

  if (profile.isPending || !profile.data) return <Loading />;
  const p = profile.data;

  async function addPhoto() {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
    });
    if (!result.canceled) upload.mutate(result.assets[0]);
  }

  function photoMenu(photoId: string, index: number) {
    const ids = p.photos.map((photo) => photo.id);
    Alert.alert('Photo', undefined, [
      ...(index > 0
        ? [{ text: 'Make main photo', onPress: () => makeMain.mutate([photoId, ...ids.filter((id) => id !== photoId)]) }]
        : []),
      { text: 'Delete', style: 'destructive' as const, onPress: () => deletePhoto.mutate(photoId) },
      { text: 'Cancel', style: 'cancel' as const },
    ]);
  }

  return (
    <Screen edges={['top']}>
      <T variant="title">Me</T>
      {p.hidden ? (
        <Card style={{ borderColor: c.danger }}>
          <T variant="error">Your profile is hidden while moderators review reports about it.</T>
        </Card>
      ) : null}

      <View style={{ flexDirection: 'row', gap: space.sm }}>
        {Array.from({ length: MAX_PHOTOS }, (_, index) => {
          const photo = p.photos[index];
          return (
            <Pressable
              key={photo?.id ?? `empty-${index}`}
              onPress={() => (photo ? photoMenu(photo.id, index) : void addPhoto())}
              style={{
                flex: 1,
                aspectRatio: 3 / 4,
                borderRadius: radius.md,
                borderWidth: photo ? 0 : 2,
                borderStyle: 'dashed',
                borderColor: c.border,
                alignItems: 'center',
                justifyContent: 'center',
                overflow: 'hidden',
              }}>
              {photo ? (
                <Photo photoId={photo.id} name={p.firstName} rounded={false} style={{ width: '100%', height: '100%' }} />
              ) : (
                <T variant="heading" style={{ color: c.textMuted }}>
                  {upload.isPending && index === p.photos.length ? '…' : '+'}
                </T>
              )}
            </Pressable>
          );
        })}
      </View>
      <T variant="small">Tap a photo to make it your main photo or delete it.</T>
      {upload.error instanceof ApiError ? <T variant="error">{upload.error.summary}</T> : null}

      <View style={{ gap: space.xs }}>
        <T variant="heading">
          {p.firstName}, {p.age}
        </T>
        <T variant="muted">{studyLine(p)}</T>
        <T variant="small">📍 {campusLabels[p.campus]}</T>
        <T variant="small">
          Looking for: {p.intents.map((i) => `${intentLabels[i].emoji} ${intentLabels[i].title}`).join(', ')}
        </T>
      </View>

      <Button title="Edit profile" variant="secondary" onPress={() => router.push('/edit-profile')} />
      <Button title="Blocked students" variant="secondary" onPress={() => router.push('/blocked')} />
      <Button title="Privacy policy" variant="secondary" onPress={() => router.push('/privacy')} />
      <Button
        title="Download my data"
        variant="secondary"
        loading={exportData.isPending}
        onPress={() => exportData.mutate()}
      />
      {exportData.error instanceof ApiError ? <T variant="error">{exportData.error.summary}</T> : null}
      <Button title="Log out" variant="secondary" onPress={() => void signOut()} />

      {deleting ? (
        <Card>
          <T variant="heading">Delete account</T>
          <T variant="muted">
            This permanently deletes your profile, photos, matches and messages. Enter your password to confirm.
          </T>
          <Field label="Password" value={password} onChangeText={setPassword} secureTextEntry />
          {deleteAccount.error instanceof ApiError ? (
            <T variant="error">{deleteAccount.error.summary}</T>
          ) : null}
          <Button
            title="Delete my account"
            variant="danger"
            disabled={!password}
            loading={deleteAccount.isPending}
            onPress={() => deleteAccount.mutate(password, { onSuccess: () => void signOut() })}
          />
          <Button title="Cancel" variant="ghost" onPress={() => setDeleting(false)} />
        </Card>
      ) : (
        <Button title="Delete account" variant="ghost" onPress={() => setDeleting(true)} />
      )}
    </Screen>
  );
}
