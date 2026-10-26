import type { ReactNode } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  type TextInputProps,
  type TextProps,
  View,
  type ViewStyle,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { radius, space, useColors } from '@/lib/theme';

type TextVariant = 'title' | 'heading' | 'body' | 'muted' | 'small' | 'error';

export function T({ variant = 'body', style, ...rest }: TextProps & { variant?: TextVariant }) {
  const c = useColors();
  const color = variant === 'muted' || variant === 'small' ? c.textMuted : variant === 'error' ? c.danger : c.text;
  return <Text style={[{ color }, textStyles[variant], style]} {...rest} />;
}

const textStyles = StyleSheet.create({
  title: { fontSize: 30, fontWeight: '700', lineHeight: 36 },
  heading: { fontSize: 19, fontWeight: '700', lineHeight: 25 },
  body: { fontSize: 16, lineHeight: 23 },
  muted: { fontSize: 15, lineHeight: 21 },
  small: { fontSize: 13, lineHeight: 18 },
  error: { fontSize: 14, lineHeight: 20, fontWeight: '600' },
});

/** A safe-area page that scrolls, with consistent padding. */
export function Screen({
  children,
  scroll = true,
  edges = ['top', 'bottom'],
}: {
  children: ReactNode;
  scroll?: boolean;
  edges?: ('top' | 'bottom')[];
}) {
  const c = useColors();
  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: c.background }} edges={edges}>
      {scroll ? (
        <ScrollView contentContainerStyle={styles.screen} keyboardShouldPersistTaps="handled">
          {children}
        </ScrollView>
      ) : (
        <View style={[styles.screen, { flex: 1 }]}>{children}</View>
      )}
    </SafeAreaView>
  );
}

export function Button({
  title,
  onPress,
  variant = 'primary',
  loading = false,
  disabled = false,
  style,
}: {
  title: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  loading?: boolean;
  disabled?: boolean;
  style?: ViewStyle;
}) {
  const c = useColors();
  const background =
    variant === 'primary' ? c.primary : variant === 'danger' ? c.danger : variant === 'secondary' ? c.surface : 'transparent';
  const color = variant === 'primary' || variant === 'danger' ? c.primaryText : variant === 'ghost' ? c.primary : c.text;
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      disabled={disabled || loading}
      style={({ pressed }) => [
        styles.button,
        { backgroundColor: background, opacity: disabled ? 0.45 : pressed ? 0.8 : 1 },
        style,
      ]}>
      {loading ? <ActivityIndicator color={color} /> : <Text style={[styles.buttonText, { color }]}>{title}</Text>}
    </Pressable>
  );
}

export function Field({
  label,
  hint,
  error,
  ...input
}: TextInputProps & { label: string; hint?: string; error?: string }) {
  const c = useColors();
  return (
    <View style={{ gap: space.xs }}>
      <T variant="small" style={{ fontWeight: '600', color: c.text }}>
        {label}
      </T>
      <TextInput
        placeholderTextColor={c.textMuted}
        style={[
          styles.input,
          { backgroundColor: c.surface, borderColor: error ? c.danger : c.border, color: c.text },
          input.multiline && { minHeight: 96, textAlignVertical: 'top' },
        ]}
        {...input}
      />
      {error ? <T variant="error">{error}</T> : hint ? <T variant="small">{hint}</T> : null}
    </View>
  );
}

export function Chip({ label, selected, onPress }: { label: string; selected: boolean; onPress?: () => void }) {
  const c = useColors();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ selected }}
      onPress={onPress}
      disabled={!onPress}
      style={[
        styles.chip,
        { backgroundColor: selected ? c.primary : c.surface, borderColor: selected ? c.primary : c.border },
      ]}>
      <Text style={{ color: selected ? c.primaryText : c.text, fontWeight: '600', fontSize: 14 }}>{label}</Text>
    </Pressable>
  );
}

/** Pick one value (or several with `multiple`) from a small set, shown as chips. */
export function ChipPicker<V extends string>({
  label,
  options,
  labels,
  value,
  onChange,
  error,
}: {
  label?: string;
  options: readonly V[];
  labels: Record<V, string>;
  value: V | V[] | null;
  onChange: (value: V) => void;
  error?: string;
}) {
  const selected = (option: V) => (Array.isArray(value) ? value.includes(option) : value === option);
  return (
    <View style={{ gap: space.sm }}>
      {label ? (
        <T variant="small" style={{ fontWeight: '600' }}>
          {label}
        </T>
      ) : null}
      <View style={styles.wrap}>
        {options.map((option) => (
          <Chip key={option} label={labels[option]} selected={selected(option)} onPress={() => onChange(option)} />
        ))}
      </View>
      {error ? <T variant="error">{error}</T> : null}
    </View>
  );
}

export function Card({ children, style }: { children: ReactNode; style?: ViewStyle }) {
  const c = useColors();
  return <View style={[styles.card, { backgroundColor: c.surface, borderColor: c.border }, style]}>{children}</View>;
}

export function Loading() {
  const c = useColors();
  return (
    <View style={styles.center}>
      <ActivityIndicator color={c.primary} size="large" />
    </View>
  );
}

export function EmptyState({ title, text, action }: { title: string; text: string; action?: ReactNode }) {
  return (
    <View style={[styles.center, { gap: space.sm, paddingVertical: space.xl }]}>
      <T variant="heading" style={{ textAlign: 'center' }}>
        {title}
      </T>
      <T variant="muted" style={{ textAlign: 'center' }}>
        {text}
      </T>
      {action}
    </View>
  );
}

export const styles = StyleSheet.create({
  screen: { padding: space.md, gap: space.md, flexGrow: 1 },
  button: {
    minHeight: 50,
    borderRadius: radius.md,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: space.lg,
  },
  buttonText: { fontSize: 16, fontWeight: '700' },
  input: { borderWidth: 1, borderRadius: radius.md, paddingHorizontal: space.md, paddingVertical: 12, fontSize: 16 },
  chip: { borderWidth: 1, borderRadius: radius.pill, paddingHorizontal: 14, paddingVertical: 8 },
  wrap: { flexDirection: 'row', flexWrap: 'wrap', gap: space.sm },
  card: { borderWidth: 1, borderRadius: radius.lg, padding: space.md, gap: space.sm },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: space.lg },
});
