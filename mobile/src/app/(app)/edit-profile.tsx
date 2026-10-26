import { router } from 'expo-router';

import { ProfileForm, profileToInput } from '@/components/profile-form';
import { Loading, Screen } from '@/components/ui';
import { useMyProfile, useSaveProfile } from '@/lib/queries';

export default function EditProfileScreen() {
  const profile = useMyProfile();
  const save = useSaveProfile();
  if (!profile.data) return <Loading />;
  return (
    <Screen edges={['bottom']}>
      <ProfileForm
        initial={profileToInput(profile.data)}
        stepped={false}
        submitLabel="Save"
        saving={save.isPending}
        error={save.error}
        onSubmit={(input) => save.mutate(input, { onSuccess: () => router.back() })}
      />
    </Screen>
  );
}
