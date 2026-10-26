import { useState } from 'react';
import { FlatList, RefreshControl, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { PersonCard } from '@/components/person-card';
import { Button, Chip, ChipPicker, EmptyState, Field, Loading, styles, T } from '@/components/ui';
import { campusLabels, intentLabels } from '@/lib/labels';
import { type DiscoverFilters, useDiscover, useMyProfile } from '@/lib/queries';
import { space, useColors } from '@/lib/theme';
import { CAMPUSES, type Intent } from '@/lib/types';

export default function DiscoverScreen() {
  const c = useColors();
  const profile = useMyProfile();
  const intents = profile.data?.intents ?? [];
  const [chosen, setChosen] = useState<Intent | undefined>();
  const intent = chosen && intents.includes(chosen) ? chosen : intents[0];
  const [filters, setFilters] = useState<DiscoverFilters>({});
  const [showFilters, setShowFilters] = useState(false);
  const people = useDiscover(intent, filters);
  const filterCount = [filters.campus, filters.program, filters.interest].filter(Boolean).length;

  const header = (
    <View style={{ gap: space.md, paddingBottom: space.md }}>
      <T variant="title">Discover</T>
      <View style={styles.wrap}>
        {intents.map((i) => (
          <Chip
            key={i}
            label={`${intentLabels[i].emoji} ${intentLabels[i].title}`}
            selected={i === intent}
            onPress={() => setChosen(i)}
          />
        ))}
      </View>
      {intent ? <T variant="muted">{intentLabels[intent].description}</T> : null}
      {profile.data && profile.data.photos.length === 0 ? (
        <T variant="small" style={{ color: c.primary }}>
          Tip: add a photo in the Me tab. People are much more likely to say hi.
        </T>
      ) : null}
      <Button
        title={showFilters ? 'Hide filters' : filterCount > 0 ? `Filters (${filterCount})` : 'Filters'}
        variant="secondary"
        onPress={() => setShowFilters(!showFilters)}
      />
      {showFilters ? (
        <View style={{ gap: space.md }}>
          <ChipPicker
            label="Campus"
            options={CAMPUSES}
            labels={campusLabels}
            value={filters.campus ?? null}
            onChange={(campus) => setFilters({ ...filters, campus: filters.campus === campus ? undefined : campus })}
          />
          <Field
            label="Program"
            value={filters.program ?? ''}
            onChangeText={(program) => setFilters({ ...filters, program })}
            placeholder="e.g. Psychology"
          />
          <Field
            label="Interest"
            value={filters.interest ?? ''}
            onChangeText={(interest) => setFilters({ ...filters, interest })}
            placeholder="e.g. Climbing"
          />
          {filterCount > 0 ? <Button title="Clear filters" variant="ghost" onPress={() => setFilters({})} /> : null}
        </View>
      ) : null}
    </View>
  );

  if (profile.isPending) return <Loading />;

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: c.background }} edges={['top']}>
      <FlatList
        data={people.data ?? []}
        keyExtractor={(person) => person.userId}
        renderItem={({ item }) => <PersonCard person={item} intent={intent!} />}
        ListHeaderComponent={header}
        ItemSeparatorComponent={() => <View style={{ height: space.md }} />}
        contentContainerStyle={{ padding: space.md, paddingBottom: space.xl * 3 }}
        refreshControl={
          <RefreshControl refreshing={people.isRefetching} onRefresh={() => void people.refetch()} />
        }
        ListEmptyComponent={
          people.isPending ? (
            <Loading />
          ) : people.isError ? (
            <EmptyState title="Something went wrong" text={people.error.message} />
          ) : (
            <EmptyState
              title="No one here yet"
              text={
                filterCount > 0
                  ? 'Nobody matches these filters. Try clearing some.'
                  : 'Be the first! Invite friends from SWPS and pull down to refresh.'
              }
            />
          )
        }
      />
    </SafeAreaView>
  );
}
