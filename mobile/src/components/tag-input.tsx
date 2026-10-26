import { useState } from 'react';
import { View } from 'react-native';

import { space } from '@/lib/theme';

import { Chip, Field, styles, T } from './ui';

/** Free-text tags such as interests: type one, press enter, tap a tag to remove it. */
export function TagInput({
  label,
  values,
  onChange,
  suggestions,
  max,
  placeholder,
}: {
  label: string;
  values: string[];
  onChange: (values: string[]) => void;
  suggestions: string[];
  max: number;
  placeholder: string;
}) {
  const [draft, setDraft] = useState('');
  const has = (tag: string) => values.some((v) => v.toLowerCase() === tag.toLowerCase());
  const add = (tag: string) => {
    const clean = tag.trim();
    if (clean && !has(clean) && values.length < max) onChange([...values, clean]);
    setDraft('');
  };
  const remove = (tag: string) => onChange(values.filter((v) => v !== tag));

  return (
    <View style={{ gap: space.sm }}>
      <Field
        label={`${label} (${values.length}/${max})`}
        value={draft}
        onChangeText={setDraft}
        onSubmitEditing={() => add(draft)}
        submitBehavior="submit"
        returnKeyType="done"
        placeholder={placeholder}
        maxLength={40}
      />
      {values.length > 0 ? (
        <View style={styles.wrap}>
          {values.map((tag) => (
            <Chip key={tag} label={`${tag}  ✕`} selected onPress={() => remove(tag)} />
          ))}
        </View>
      ) : null}
      <View style={styles.wrap}>
        {suggestions
          .filter((s) => !has(s))
          .slice(0, 10)
          .map((s) => (
            <Chip key={s} label={`+ ${s}`} selected={false} onPress={() => add(s)} />
          ))}
      </View>
      {values.length >= max ? <T variant="small">You have added the maximum.</T> : null}
    </View>
  );
}
