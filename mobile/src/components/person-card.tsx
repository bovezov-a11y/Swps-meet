import { Link } from 'expo-router';
import { Pressable, View } from 'react-native';

import { campusLabels, studyLine } from '@/lib/labels';
import { radius, space, useColors } from '@/lib/theme';
import type { Intent, ProfileCard } from '@/lib/types';

import { Photo } from './photo';
import { Chip, styles, T } from './ui';

/** One student in a discover list. Opens their profile, where the student can say they want to meet. */
export function PersonCard({ person, intent }: { person: ProfileCard; intent: Intent }) {
  const c = useColors();
  const liked = person.likedIn.includes(intent);
  return (
    <Link href={{ pathname: '/person/[id]', params: { id: person.userId, intent } }} asChild>
      <Pressable
        style={{ backgroundColor: c.surface, borderColor: c.border, borderWidth: 1, borderRadius: radius.lg }}>
        <Photo
          photoId={person.photoIds[0]}
          name={person.firstName}
          rounded={false}
          style={{ width: '100%', aspectRatio: 4 / 3, borderTopLeftRadius: radius.lg, borderTopRightRadius: radius.lg }}
        />
        <View style={{ padding: space.md, gap: space.xs }}>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
            <T variant="heading">
              {person.firstName}, {person.age}
            </T>
            {person.matchId ? (
              <T style={{ color: c.success, fontWeight: '700' }}>Matched</T>
            ) : liked ? (
              <T style={{ color: c.primary, fontWeight: '700' }}>Request sent</T>
            ) : null}
          </View>
          <T variant="muted">{studyLine(person)}</T>
          <T variant="small">📍 {campusLabels[person.campus]}</T>
          {person.industry ? (
            <T variant="small">
              💼 {person.internshipRole === 'OFFERING' ? 'Can help with' : 'Looking for'}: {person.industry}
            </T>
          ) : null}
          {person.interests.length > 0 ? (
            <View style={[styles.wrap, { marginTop: space.xs }]}>
              {person.interests.slice(0, 4).map((interest) => (
                <Chip key={interest} label={interest} selected={false} />
              ))}
            </View>
          ) : null}
        </View>
      </Pressable>
    </Link>
  );
}
