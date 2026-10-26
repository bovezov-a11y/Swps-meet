import { View } from 'react-native';

import { Button, EmptyState, Loading, Screen, T } from '@/components/ui';
import { useBlocked, useUnblock } from '@/lib/queries';
import { space } from '@/lib/theme';

export default function BlockedScreen() {
  const blocked = useBlocked();
  const unblock = useUnblock();
  if (blocked.isPending) return <Loading />;
  return (
    <Screen edges={['bottom']}>
      {(blocked.data ?? []).length === 0 ? (
        <EmptyState title="Nobody blocked" text="Students you block or report appear here." />
      ) : (
        blocked.data!.map((user) => (
          <View key={user.userId} style={{ flexDirection: 'row', alignItems: 'center', gap: space.md }}>
            <T style={{ flex: 1, fontWeight: '600' }}>{user.firstName}</T>
            <Button
              title="Unblock"
              variant="secondary"
              loading={unblock.isPending && unblock.variables === user.userId}
              onPress={() => unblock.mutate(user.userId)}
            />
          </View>
        ))
      )}
    </Screen>
  );
}
