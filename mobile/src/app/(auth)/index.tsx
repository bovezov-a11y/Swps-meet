import { router } from 'expo-router';
import { View } from 'react-native';

import { Button, Screen, T } from '@/components/ui';
import { intentLabels } from '@/lib/labels';
import { space } from '@/lib/theme';
import { INTENTS } from '@/lib/types';

export default function WelcomeScreen() {
  return (
    <Screen>
      <View style={{ flex: 1, justifyContent: 'center', gap: space.lg }}>
        <View style={{ gap: space.sm }}>
          <T variant="title">Meet SWPS</T>
          <T variant="muted">
            Meet other SWPS students. Only people with a verified SWPS email can join.
          </T>
        </View>
        <View style={{ gap: space.sm }}>
          {INTENTS.map((intent) => (
            <T key={intent}>
              {intentLabels[intent].emoji} {intentLabels[intent].title}
            </T>
          ))}
        </View>
      </View>
      <View style={{ gap: space.sm }}>
        <Button title="Create an account" onPress={() => router.push('/register')} />
        <Button title="I already have an account" variant="secondary" onPress={() => router.push('/login')} />
        <T variant="small" style={{ textAlign: 'center' }}>
          Unofficial student project, not affiliated with SWPS University.
        </T>
      </View>
    </Screen>
  );
}
