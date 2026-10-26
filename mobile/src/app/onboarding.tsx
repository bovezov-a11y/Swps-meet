import { emptyProfile, ProfileForm } from '@/components/profile-form';
import { Button, Screen } from '@/components/ui';
import { useSession } from '@/lib/auth';
import { useSaveProfile } from '@/lib/queries';

/** Saving the profile flips the account to "has profile", and the root layout then opens the app. */
export default function OnboardingScreen() {
  const { account, signOut } = useSession();
  const save = useSaveProfile();
  return (
    <Screen>
      <ProfileForm
        initial={emptyProfile(account?.user.firstName ?? '')}
        stepped
        submitLabel="Finish"
        saving={save.isPending}
        error={save.error}
        onSubmit={(input) => save.mutate(input)}
      />
      <Button title="Log out" variant="ghost" onPress={() => void signOut()} />
    </Screen>
  );
}
