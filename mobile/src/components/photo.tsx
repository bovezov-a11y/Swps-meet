import { Image } from 'expo-image';
import { type ImageStyle, StyleSheet, Text, View, type ViewStyle } from 'react-native';

import { authHeaders, photoUrl } from '@/lib/api';
import { useColors } from '@/lib/theme';

/** A profile photo, loaded with the session token, or the first letter of the name when there is none. */
export function Photo({
  photoId,
  name,
  size,
  style,
  rounded = true,
}: {
  photoId?: string;
  name: string;
  size?: number;
  style?: ImageStyle;
  rounded?: boolean;
}) {
  const c = useColors();
  const box: ImageStyle = size ? { width: size, height: size } : {};
  const corner = rounded ? { borderRadius: size ? size / 2 : 18 } : {};
  if (!photoId) {
    return (
      <View style={[styles.placeholder, box as ViewStyle, corner, { backgroundColor: c.border }, style as ViewStyle]}>
        <Text style={{ color: c.textMuted, fontSize: size ? size / 2.6 : 48, fontWeight: '700' }}>
          {name.charAt(0).toUpperCase()}
        </Text>
      </View>
    );
  }
  return (
    <Image
      source={{ uri: photoUrl(photoId), headers: authHeaders() }}
      style={[box, corner, { backgroundColor: c.border }, style]}
      contentFit="cover"
      transition={150}
      accessibilityLabel={`Photo of ${name}`}
    />
  );
}

const styles = StyleSheet.create({
  placeholder: { alignItems: 'center', justifyContent: 'center' },
});
