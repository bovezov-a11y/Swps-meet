import { router, Stack, useLocalSearchParams } from 'expo-router';
import { Linking, ScrollView, useWindowDimensions, View } from 'react-native';

import { Photo } from '@/components/photo';
import { confirmBlock, openReport } from '@/components/safety-actions';
import { Button, Card, Chip, EmptyState, Loading, Screen, styles, T } from '@/components/ui';
import { ApiError } from '@/lib/api';
import { campusLabels, intentLabels, studyLine } from '@/lib/labels';
import { useBlock, useLike, usePerson } from '@/lib/queries';
import { radius, space } from '@/lib/theme';
import type { Intent } from '@/lib/types';

export default function PersonScreen() {
  const { id, intent: from } = useLocalSearchParams<{ id: string; intent?: Intent }>();
  const { width } = useWindowDimensions();
  const person = usePerson(id);
  const like = useLike();
  const block = useBlock();

  if (person.isPending) return <Loading />;
  if (person.isError) {
    return (
      <Screen edges={['bottom']}>
        <EmptyState title="Profile not available" text="This student is no longer visible to you." />
      </Screen>
    );
  }
  const p = person.data;
  // The section the student came from goes first.
  const sections = [...p.sharedIntents].sort((a, b) => Number(b === from) - Number(a === from));

  return (
    <Screen edges={['bottom']}>
      <Stack.Screen options={{ title: p.firstName }} />
      {p.photoIds.length > 0 ? (
        <ScrollView horizontal pagingEnabled showsHorizontalScrollIndicator={false} style={{ marginHorizontal: -space.md }}>
          {p.photoIds.map((photoId) => (
            <Photo key={photoId} photoId={photoId} name={p.firstName} rounded={false} style={{ width, aspectRatio: 1 }} />
          ))}
        </ScrollView>
      ) : (
        <Photo name={p.firstName} rounded={false} style={{ width: '100%', aspectRatio: 2, borderRadius: radius.lg }} />
      )}

      <View style={{ gap: space.xs }}>
        <T variant="title">
          {p.firstName}, {p.age}
        </T>
        <T variant="muted">{studyLine(p)}</T>
        <T variant="small">📍 {campusLabels[p.campus]}</T>
      </View>

      {p.matchId ? (
        <Card>
          <T variant="heading">🎉 You matched!</T>
          <Button title="Send a message" onPress={() => router.push({ pathname: '/chat/[id]', params: { id: p.matchId! } })} />
          {p.instagram ? (
            <Button
              title={`Instagram: @${p.instagram}`}
              variant="secondary"
              onPress={() => void Linking.openURL(`https://instagram.com/${p.instagram}`)}
            />
          ) : null}
        </Card>
      ) : (
        <Card>
          <T variant="heading">Want to meet?</T>
          <T variant="small">
            {p.firstName} only finds out if they want to meet you too. Then you can chat and see each other’s
            Instagram.
          </T>
          {sections.map((intent) => {
            const liked = p.likedIn.includes(intent);
            return (
              <Button
                key={intent}
                title={liked ? `✓ Sent · ${intentLabels[intent].title}` : `${intentLabels[intent].emoji} ${intentLabels[intent].title}`}
                variant={liked ? 'secondary' : 'primary'}
                loading={like.isPending && like.variables?.intent === intent}
                onPress={() => like.mutate({ userId: p.userId, intent, liked: !liked })}
              />
            );
          })}
          {like.data?.matched ? <T style={{ fontWeight: '700' }}>It’s a match! 🎉</T> : null}
          {like.error instanceof ApiError ? <T variant="error">{like.error.summary}</T> : null}
        </Card>
      )}

      {p.industry ? (
        <View style={{ gap: space.xs }}>
          <T variant="heading">💼 Internships</T>
          <T>
            {p.internshipRole === 'OFFERING' ? 'Works in' : 'Looking for an internship in'} {p.industry}
            {p.workplace ? ` at ${p.workplace}` : ''}
          </T>
        </View>
      ) : null}
      {p.bio ? (
        <View style={{ gap: space.xs }}>
          <T variant="heading">About</T>
          <T>{p.bio}</T>
        </View>
      ) : null}
      {p.interests.length > 0 ? (
        <View style={{ gap: space.sm }}>
          <T variant="heading">Interests</T>
          <View style={styles.wrap}>
            {p.interests.map((i) => (
              <Chip key={i} label={i} selected={false} />
            ))}
          </View>
        </View>
      ) : null}
      {p.languages.length > 0 ? (
        <View style={{ gap: space.xs }}>
          <T variant="heading">Languages</T>
          <T>{p.languages.join(', ')}</T>
        </View>
      ) : null}

      <View style={{ gap: space.xs, marginTop: space.lg }}>
        <Button title="Report" variant="ghost" onPress={() => openReport(p.userId, p.firstName)} />
        <Button
          title="Block"
          variant="ghost"
          onPress={() => confirmBlock(p.firstName, () => block.mutateAsync(p.userId))}
        />
      </View>
    </Screen>
  );
}
