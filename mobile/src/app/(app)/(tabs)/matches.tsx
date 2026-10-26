import { Link } from 'expo-router';
import { FlatList, Pressable, RefreshControl, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Photo } from '@/components/photo';
import { EmptyState, Loading, T } from '@/components/ui';
import { intentLabels } from '@/lib/labels';
import { useMatches } from '@/lib/queries';
import { space, useColors } from '@/lib/theme';

export default function MatchesScreen() {
  const c = useColors();
  const matches = useMatches();

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: c.background }} edges={['top']}>
      <FlatList
        data={matches.data ?? []}
        keyExtractor={(match) => match.id}
        contentContainerStyle={{ padding: space.md, gap: space.sm, flexGrow: 1 }}
        ListHeaderComponent={<T variant="title" style={{ marginBottom: space.sm }}>Matches</T>}
        refreshControl={<RefreshControl refreshing={matches.isRefetching} onRefresh={() => void matches.refetch()} />}
        ListEmptyComponent={
          matches.isPending ? (
            <Loading />
          ) : (
            <EmptyState
              title="No matches yet"
              text="When someone you want to meet wants to meet you too, they show up here and you can chat."
            />
          )
        }
        renderItem={({ item }) => (
          <Link href={{ pathname: '/chat/[id]', params: { id: item.id } }} asChild>
            <Pressable style={{ flexDirection: 'row', gap: space.md, alignItems: 'center', paddingVertical: space.sm }}>
              <Photo photoId={item.person.photoIds[0]} name={item.person.firstName} size={56} />
              <View style={{ flex: 1, gap: 2 }}>
                <T style={{ fontWeight: '700' }}>
                  {item.person.firstName} · {intentLabels[item.intent].emoji}
                </T>
                <T variant="muted" numberOfLines={1}>
                  {item.lastMessage
                    ? `${item.lastMessage.mine ? 'You: ' : ''}${item.lastMessage.body}`
                    : 'Say hi 👋'}
                </T>
              </View>
            </Pressable>
          </Link>
        )}
      />
    </SafeAreaView>
  );
}
