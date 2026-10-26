// Mirrors the backend DTOs. Keep in sync with backend/src/main/java/com/meetswps.

export const INTENTS = ['INTERNSHIP', 'FRIENDS', 'SOCIAL', 'DATING'] as const;
export type Intent = (typeof INTENTS)[number];

export const CAMPUSES = ['WARSAW', 'WROCLAW', 'POZNAN', 'SOPOT', 'KATOWICE'] as const;
export type Campus = (typeof CAMPUSES)[number];

export const DEGREE_LEVELS = ['BACHELOR', 'MASTER', 'UNIFORM_MASTER', 'PHD'] as const;
export type DegreeLevel = (typeof DEGREE_LEVELS)[number];

export const GENDERS = ['WOMAN', 'MAN', 'NON_BINARY'] as const;
export type Gender = (typeof GENDERS)[number];

export const INTERNSHIP_ROLES = ['LOOKING', 'OFFERING'] as const;
export type InternshipRole = (typeof INTERNSHIP_ROLES)[number];

export const REPORT_REASONS = [
  'FAKE_PROFILE',
  'HARASSMENT',
  'INAPPROPRIATE_PHOTOS',
  'SPAM',
  'UNDERAGE',
  'OTHER',
] as const;
export type ReportReason = (typeof REPORT_REASONS)[number];

export type User = {
  id: string;
  email: string;
  firstName: string;
  role: 'STUDENT' | 'MODERATOR' | 'ADMIN';
  createdAt: string;
};

export type AuthResponse = { token: string; expiresAt: string; user: User };

export type Account = { user: User; hasProfile: boolean };

export type Photo = { id: string; position: number };

export type ProfileInput = {
  firstName: string;
  birthDate: string;
  campus: Campus;
  program: string;
  studyYear: number;
  degreeLevel: DegreeLevel;
  bio: string;
  interests: string[];
  languages: string[];
  instagram: string;
  intents: Intent[];
  internshipRole: InternshipRole | null;
  industry: string;
  workplace: string;
  gender: Gender | null;
  interestedIn: Gender[];
};

export type MyProfile = Omit<ProfileInput, 'bio' | 'instagram' | 'industry' | 'workplace'> & {
  userId: string;
  age: number;
  bio: string | null;
  instagram: string | null;
  industry: string | null;
  workplace: string | null;
  hidden: boolean;
  photos: Photo[];
};

export type ProfileCard = {
  userId: string;
  firstName: string;
  age: number;
  campus: Campus;
  program: string;
  studyYear: number;
  degreeLevel: DegreeLevel;
  bio: string | null;
  interests: string[];
  languages: string[];
  photoIds: string[];
  sharedIntents: Intent[];
  internshipRole: InternshipRole | null;
  industry: string | null;
  workplace: string | null;
  likedIn: Intent[];
  matchId: string | null;
  instagram: string | null;
};

export type LikeResult = { matched: boolean; matchId: string | null };

export type Message = { id: string; mine: boolean; body: string; createdAt: string };

export type MatchSummary = {
  id: string;
  intent: Intent;
  createdAt: string;
  person: ProfileCard;
  lastMessage: Message | null;
};

export type BlockedUser = { userId: string; firstName: string; blockedAt: string };
