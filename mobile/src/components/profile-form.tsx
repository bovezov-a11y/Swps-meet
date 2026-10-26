import { useState } from 'react';
import { Pressable, View } from 'react-native';

import { ApiError } from '@/lib/api';
import {
  campusLabels,
  degreeLabels,
  genderLabels,
  intentLabels,
  internshipRoleLabels,
} from '@/lib/labels';
import { INTEREST_SUGGESTIONS, LANGUAGE_SUGGESTIONS, PROGRAM_SUGGESTIONS } from '@/lib/suggestions';
import { radius, space, useColors } from '@/lib/theme';
import {
  CAMPUSES,
  DEGREE_LEVELS,
  GENDERS,
  INTENTS,
  INTERNSHIP_ROLES,
  type Intent,
  type MyProfile,
  type ProfileInput,
} from '@/lib/types';

import { TagInput } from './tag-input';
import { Button, Chip, ChipPicker, Field, styles, T } from './ui';

const STUDY_YEARS = ['1', '2', '3', '4', '5'] as const;
const yearLabels = { 1: '1st', 2: '2nd', 3: '3rd', 4: '4th', 5: '5th' } as Record<string, string>;

export function emptyProfile(firstName: string): ProfileInput {
  return {
    firstName,
    birthDate: '',
    campus: 'WARSAW',
    program: '',
    studyYear: 1,
    degreeLevel: 'BACHELOR',
    bio: '',
    interests: [],
    languages: [],
    instagram: '',
    intents: [],
    internshipRole: null,
    industry: '',
    workplace: '',
    gender: null,
    interestedIn: [],
  };
}

export function profileToInput(p: MyProfile): ProfileInput {
  return {
    ...p,
    bio: p.bio ?? '',
    instagram: p.instagram ?? '',
    industry: p.industry ?? '',
    workplace: p.workplace ?? '',
  };
}

function toggle<V>(list: V[], value: V): V[] {
  return list.includes(value) ? list.filter((v) => v !== value) : [...list, value];
}

/** "31.12.2004" or "2004-12-31" → "2004-12-31", or null when it is not a real date. */
export function parseBirthDate(text: string): string | null {
  const trimmed = text.trim();
  const dotted = /^(\d{1,2})[./](\d{1,2})[./](\d{4})$/.exec(trimmed);
  const iso = /^(\d{4})-(\d{2})-(\d{2})$/.exec(trimmed);
  const parts = dotted ? [dotted[3], dotted[2], dotted[1]] : iso ? [iso[1], iso[2], iso[3]] : null;
  if (!parts) return null;
  const [y, m, d] = parts as [string, string, string];
  const value = `${y}-${m.padStart(2, '0')}-${d.padStart(2, '0')}`;
  const date = new Date(`${value}T00:00:00Z`);
  return !Number.isNaN(date.getTime()) && date.toISOString().startsWith(value) ? value : null;
}

function formatBirthDate(iso: string) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(iso);
  return match ? `${match[3]}.${match[2]}.${match[1]}` : iso;
}

type Step = { title: string; subtitle: string };
const STEPS: Step[] = [
  { title: 'About you', subtitle: 'Only your first name is shown to other students.' },
  { title: 'What you study', subtitle: 'So people from your program and campus can find you.' },
  { title: 'What are you looking for?', subtitle: 'Pick as many as you like. You can change this any time.' },
  { title: 'A bit more', subtitle: 'Optional, but profiles with these get more replies.' },
];

/**
 * The whole profile. During onboarding it is shown one step at a time; when editing, all at once. Server
 * validation errors are shown under the matching field.
 */
export function ProfileForm({
  initial,
  stepped,
  submitLabel,
  saving,
  error,
  onSubmit,
}: {
  initial: ProfileInput;
  stepped: boolean;
  submitLabel: string;
  saving: boolean;
  error: unknown;
  onSubmit: (input: ProfileInput) => void;
}) {
  const c = useColors();
  const [form, setForm] = useState(initial);
  const [birthText, setBirthText] = useState(formatBirthDate(initial.birthDate));
  const [step, setStep] = useState(0);
  const [localError, setLocalError] = useState<string | null>(null);
  const set = <K extends keyof ProfileInput>(key: K, value: ProfileInput[K]) =>
    setForm((f) => ({ ...f, [key]: value }));

  const serverErrors = error instanceof ApiError ? error.fieldErrors : {};
  const generalError = error instanceof ApiError && Object.keys(serverErrors).length === 0 ? error.message : null;
  const has = (intent: Intent) => form.intents.includes(intent);

  /** Checks what the server would reject anyway, so students are told before moving on. */
  function problemIn(index: number): string | null {
    if (index === 0) {
      if (!form.firstName.trim()) return 'Enter your first name.';
      if (!parseBirthDate(birthText)) return 'Enter your date of birth as DD.MM.YYYY.';
    }
    if (index === 1 && !form.program.trim()) return 'Enter what you study.';
    if (index === 2) {
      if (form.intents.length === 0) return 'Choose at least one thing you are looking for.';
      if (has('INTERNSHIP') && !form.internshipRole) return 'Tell others how you are involved in internships.';
      if (has('DATING') && !form.gender) return 'Choose your gender for the dating section.';
      if (has('DATING') && form.interestedIn.length === 0) return 'Choose who you would like to meet.';
    }
    return null;
  }

  function next() {
    const problem = problemIn(step);
    setLocalError(problem);
    if (!problem) setStep(step + 1);
  }

  function submit() {
    const problem = [0, 1, 2].map(problemIn).find(Boolean) ?? null;
    setLocalError(problem);
    if (problem) return;
    onSubmit({ ...form, birthDate: parseBirthDate(birthText)! });
  }

  const sections = [
    <View key="about" style={{ gap: space.md }}>
      <Field
        label="First name"
        value={form.firstName}
        onChangeText={(v) => set('firstName', v)}
        autoComplete="given-name"
        maxLength={50}
        error={serverErrors.firstName}
      />
      <Field
        label="Date of birth"
        value={birthText}
        onChangeText={setBirthText}
        placeholder="DD.MM.YYYY"
        keyboardType="numbers-and-punctuation"
        hint="Others only see your age. You must be 18 or older."
        error={serverErrors.birthDate}
      />
    </View>,

    <View key="study" style={{ gap: space.md }}>
      <ChipPicker
        label="Campus"
        options={CAMPUSES}
        labels={campusLabels}
        value={form.campus}
        onChange={(v) => set('campus', v)}
      />
      <Field
        label="Program"
        value={form.program}
        onChangeText={(v) => set('program', v)}
        placeholder="e.g. Psychology"
        maxLength={100}
        error={serverErrors.program}
      />
      <View style={styles.wrap}>
        {PROGRAM_SUGGESTIONS.filter((p) => p !== form.program).slice(0, 8).map((p) => (
          <Chip key={p} label={p} selected={false} onPress={() => set('program', p)} />
        ))}
      </View>
      <ChipPicker
        label="Degree"
        options={DEGREE_LEVELS}
        labels={degreeLabels}
        value={form.degreeLevel}
        onChange={(v) => set('degreeLevel', v)}
      />
      <ChipPicker
        label="Year"
        options={STUDY_YEARS}
        labels={yearLabels}
        value={String(form.studyYear) as (typeof STUDY_YEARS)[number]}
        onChange={(v) => set('studyYear', Number(v))}
      />
    </View>,

    <View key="intents" style={{ gap: space.md }}>
      {INTENTS.map((intent) => {
        const selected = has(intent);
        const label = intentLabels[intent];
        return (
          <Pressable
            key={intent}
            accessibilityRole="checkbox"
            accessibilityState={{ checked: selected }}
            onPress={() => set('intents', toggle(form.intents, intent))}
            style={{
              borderWidth: 2,
              borderColor: selected ? c.primary : c.border,
              backgroundColor: c.surface,
              borderRadius: radius.lg,
              padding: space.md,
              gap: space.xs,
            }}>
            <T variant="heading">
              {label.emoji} {label.title} {selected ? '✓' : ''}
            </T>
            <T variant="muted">{label.description}</T>
          </Pressable>
        );
      })}
      {serverErrors.intents ? <T variant="error">{serverErrors.intents}</T> : null}

      {has('INTERNSHIP') ? (
        <View style={{ gap: space.md }}>
          <T variant="heading">💼 Internships</T>
          <ChipPicker
            options={INTERNSHIP_ROLES}
            labels={internshipRoleLabels}
            value={form.internshipRole}
            onChange={(v) => set('internshipRole', v)}
            error={serverErrors.internshipRole}
          />
          <Field
            label="Field or industry"
            value={form.industry}
            onChangeText={(v) => set('industry', v)}
            placeholder="e.g. Marketing, HR, IT"
            maxLength={100}
          />
          {form.internshipRole === 'OFFERING' ? (
            <Field
              label="Where you work (optional)"
              value={form.workplace}
              onChangeText={(v) => set('workplace', v)}
              placeholder="Company name"
              maxLength={100}
            />
          ) : null}
        </View>
      ) : null}

      {has('DATING') ? (
        <View style={{ gap: space.md }}>
          <T variant="heading">💜 Looking for fun</T>
          <T variant="small">
            Only shown in the Looking for fun section, and only to people you could match with.
          </T>
          <ChipPicker
            label="I am a"
            options={GENDERS}
            labels={genderLabels}
            value={form.gender}
            onChange={(v) => set('gender', v)}
            error={serverErrors.gender}
          />
          <ChipPicker
            label="I'd like to meet"
            options={GENDERS}
            labels={genderLabels}
            value={form.interestedIn}
            onChange={(v) => set('interestedIn', toggle(form.interestedIn, v))}
            error={serverErrors.interestedIn}
          />
        </View>
      ) : null}
    </View>,

    <View key="more" style={{ gap: space.lg }}>
      <Field
        label="About me"
        value={form.bio}
        onChangeText={(v) => set('bio', v)}
        placeholder="What do you enjoy? What would you like to do together?"
        multiline
        maxLength={500}
        error={serverErrors.bio}
      />
      <TagInput
        label="Interests"
        values={form.interests}
        onChange={(v) => set('interests', v)}
        suggestions={INTEREST_SUGGESTIONS}
        max={10}
        placeholder="Type an interest and press enter"
      />
      <TagInput
        label="Languages I speak"
        values={form.languages}
        onChange={(v) => set('languages', v)}
        suggestions={LANGUAGE_SUGGESTIONS}
        max={6}
        placeholder="Type a language and press enter"
      />
      <Field
        label="Instagram (optional)"
        value={form.instagram}
        onChangeText={(v) => set('instagram', v)}
        placeholder="@username or profile link"
        autoCapitalize="none"
        autoCorrect={false}
        hint="Only people you match with can see it."
        error={serverErrors.instagram}
      />
    </View>,
  ];

  const errorText = localError ?? generalError ?? (error instanceof ApiError ? error.summary : null);
  const isLast = step === sections.length - 1;

  if (!stepped) {
    return (
      <View style={{ gap: space.xl }}>
        {sections.map((section, index) => (
          <View key={index} style={{ gap: space.md }}>
            <T variant="heading">{STEPS[index].title}</T>
            {section}
          </View>
        ))}
        {errorText ? <T variant="error">{errorText}</T> : null}
        <Button title={submitLabel} onPress={submit} loading={saving} />
      </View>
    );
  }

  return (
    <View style={{ gap: space.lg }}>
      <View style={{ gap: space.xs }}>
        <T variant="small">
          Step {step + 1} of {sections.length}
        </T>
        <T variant="title">{STEPS[step].title}</T>
        <T variant="muted">{STEPS[step].subtitle}</T>
      </View>
      {sections[step]}
      {errorText ? <T variant="error">{errorText}</T> : null}
      <View style={{ gap: space.sm }}>
        <Button title={isLast ? submitLabel : 'Next'} onPress={isLast ? submit : next} loading={saving} />
        {step > 0 ? <Button title="Back" variant="ghost" onPress={() => setStep(step - 1)} /> : null}
      </View>
    </View>
  );
}
