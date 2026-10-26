import { View } from 'react-native';

import { Screen, T } from '@/components/ui';
import { PRIVACY_POLICY, PRIVACY_POLICY_VERSION } from '@/content/privacy-policy';
import { space } from '@/lib/theme';

export function PrivacyPolicy() {
  return (
    <Screen edges={['bottom']}>
      <T variant="small">Version {PRIVACY_POLICY_VERSION}</T>
      {PRIVACY_POLICY.map((section) => (
        <View key={section.heading} style={{ gap: space.xs }}>
          <T variant="heading">{section.heading}</T>
          <T>{section.text}</T>
        </View>
      ))}
    </Screen>
  );
}
