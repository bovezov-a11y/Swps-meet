import { useMutation } from '@tanstack/react-query';
import { Link, router } from 'expo-router';
import { useState } from 'react';
import { Pressable, View } from 'react-native';

import { Button, Field, Screen, T } from '@/components/ui';
import { PRIVACY_POLICY_VERSION } from '@/content/privacy-policy';
import { api, ApiError } from '@/lib/api';
import { radius, space, useColors } from '@/lib/theme';

export default function RegisterScreen() {
  const c = useColors();
  const [firstName, setFirstName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [accepted, setAccepted] = useState(false);

  const register = useMutation({
    mutationFn: () =>
      api<{ email: string }>('/api/auth/register', {
        method: 'POST',
        body: { firstName, email, password, privacyPolicyVersion: PRIVACY_POLICY_VERSION },
      }),
    onSuccess: ({ email: confirmed }) => router.push({ pathname: '/verify', params: { email: confirmed } }),
  });
  const errors = register.error instanceof ApiError ? register.error.fieldErrors : {};

  return (
    <Screen>
      <View style={{ gap: space.xs, marginTop: space.xl }}>
        <T variant="title">Create your account</T>
        <T variant="muted">We will email a 6-digit code to your SWPS address.</T>
      </View>
      <Field
        label="First name"
        value={firstName}
        onChangeText={setFirstName}
        autoComplete="given-name"
        error={errors.firstName}
      />
      <Field
        label="SWPS email"
        value={email}
        onChangeText={setEmail}
        placeholder="you@st.swps.edu.pl"
        keyboardType="email-address"
        autoCapitalize="none"
        autoComplete="email"
        error={errors.email}
      />
      <Field
        label="Password"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
        autoComplete="new-password"
        hint="At least 8 characters."
        error={errors.password}
      />
      <Pressable
        accessibilityRole="checkbox"
        accessibilityState={{ checked: accepted }}
        onPress={() => setAccepted(!accepted)}
        style={{ flexDirection: 'row', gap: space.sm, alignItems: 'center' }}>
        <View
          style={{
            width: 24,
            height: 24,
            borderRadius: radius.sm,
            borderWidth: 2,
            borderColor: c.primary,
            backgroundColor: accepted ? c.primary : 'transparent',
            alignItems: 'center',
            justifyContent: 'center',
          }}>
          {accepted ? <T style={{ color: c.primaryText, fontWeight: '700' }}>✓</T> : null}
        </View>
        <T style={{ flex: 1 }}>
          I am 18 or older and accept the{' '}
          <Link href="/privacy" style={{ color: c.primary, fontWeight: '600' }}>
            privacy policy
          </Link>
        </T>
      </Pressable>
      {errors.privacyPolicyVersion ? <T variant="error">{errors.privacyPolicyVersion}</T> : null}
      {register.error instanceof ApiError && Object.keys(errors).length === 0 ? (
        <T variant="error">{register.error.message}</T>
      ) : null}
      <Button
        title="Send me a code"
        onPress={() => register.mutate()}
        loading={register.isPending}
        disabled={!accepted || !email || !password || !firstName}
      />
    </Screen>
  );
}
