import { router, Stack, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';
import { Alert, FlatList, KeyboardAvoidingView, Platform, Pressable, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Photo } from '@/components/photo';
import { confirmBlock, openReport } from '@/components/safety-actions';
import { Button, EmptyState, Loading, T } from '@/components/ui';
import { ApiError } from '@/lib/api';
import { useBlock, useMatches, useMessages, useSendMessage, useUnmatch } from '@/lib/queries';
import { radius, space, useColors } from '@/lib/theme';

export default function ChatScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const c = useColors();
  const matches = useMatches();
  const messages = useMessages(id);
  const send = useSendMessage(id);
  const unmatch = useUnmatch();
  const block = useBlock();
  const [draft, setDraft] = useState('');
  const match = matches.data?.find((m) => m.id === id);

  if (matches.isPending || messages.isPending) return <Loading />;
  if (!match || messages.isError) {
    return <EmptyState title="Chat not available" text="This match has ended." />;
  }
  const person = match.person;

  function showMenu() {
    Alert.alert(person.firstName, undefined, [
      {
        text: 'Unmatch',
        style: 'destructive',
        onPress: () => unmatch.mutate(id, { onSuccess: () => router.back() }),
      },
      { text: 'Report', onPress: () => openReport(person.userId, person.firstName) },
      { text: 'Block', onPress: () => confirmBlock(person.firstName, () => block.mutateAsync(person.userId)) },
      { text: 'Cancel', style: 'cancel' },
    ]);
  }

  function submit() {
    const body = draft.trim();
    if (!body) return;
    send.mutate(body, { onSuccess: () => setDraft('') });
  }

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: c.background }} edges={['bottom']}>
      <Stack.Screen
        options={{
          headerTitle: () => (
            <Pressable
              onPress={() => router.push({ pathname: '/person/[id]', params: { id: person.userId } })}
              style={{ flexDirection: 'row', alignItems: 'center', gap: space.sm }}>
              <Photo photoId={person.photoIds[0]} name={person.firstName} size={32} />
              <T style={{ fontWeight: '700' }}>{person.firstName}</T>
            </Pressable>
          ),
          headerRight: () => <Button title="•••" variant="ghost" onPress={showMenu} style={{ minHeight: 36 }} />,
        }}
      />
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        keyboardVerticalOffset={100}>
        <FlatList
          data={[...(messages.data ?? [])].reverse()}
          inverted
          keyExtractor={(message) => message.id}
          contentContainerStyle={{ padding: space.md, gap: space.sm, flexGrow: 1 }}
          ListEmptyComponent={
            <View style={{ transform: [{ scaleY: -1 }] }}>
              <EmptyState
                title={`You and ${person.firstName} matched`}
                text="Say hi! Keep it friendly, and meet in public places the first time."
              />
            </View>
          }
          renderItem={({ item }) => (
            <View
              style={{
                alignSelf: item.mine ? 'flex-end' : 'flex-start',
                maxWidth: '80%',
                backgroundColor: item.mine ? c.primary : c.surface,
                borderRadius: radius.lg,
                paddingHorizontal: space.md,
                paddingVertical: space.sm,
              }}>
              <T style={{ color: item.mine ? c.primaryText : c.text }}>{item.body}</T>
            </View>
          )}
        />
        {send.error instanceof ApiError ? (
          <T variant="error" style={{ paddingHorizontal: space.md }}>
            {send.error.summary}
          </T>
        ) : null}
        <View style={{ flexDirection: 'row', gap: space.sm, padding: space.sm, alignItems: 'flex-end' }}>
          <TextInput
            value={draft}
            onChangeText={setDraft}
            placeholder="Message"
            placeholderTextColor={c.textMuted}
            multiline
            maxLength={2000}
            style={{
              flex: 1,
              color: c.text,
              backgroundColor: c.surface,
              borderRadius: radius.lg,
              paddingHorizontal: space.md,
              paddingVertical: 10,
              fontSize: 16,
              maxHeight: 120,
            }}
          />
          <Button title="Send" onPress={submit} loading={send.isPending} disabled={!draft.trim()} />
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
