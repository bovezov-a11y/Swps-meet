import type { Campus, DegreeLevel, Gender, Intent, InternshipRole, ReportReason } from './types';

export const intentLabels: Record<Intent, { title: string; emoji: string; description: string }> = {
  INTERNSHIP: {
    title: 'Internships',
    emoji: '💼',
    description: 'Find someone who works where you want to intern, or help a fellow student get in.',
  },
  FRIENDS: { title: 'Friends', emoji: '🤝', description: 'Meet people to study, hang out and grab coffee with.' },
  SOCIAL: { title: 'Socialising', emoji: '🎉', description: 'Parties, events, sports and going out together.' },
  DATING: {
    title: 'Looking for fun',
    emoji: '💜',
    description: 'Only other students in this section can see you here.',
  },
};

export const campusLabels: Record<Campus, string> = {
  WARSAW: 'Warsaw',
  WROCLAW: 'Wrocław',
  POZNAN: 'Poznań',
  SOPOT: 'Sopot',
  KATOWICE: 'Katowice',
};

export const degreeLabels: Record<DegreeLevel, string> = {
  BACHELOR: "Bachelor's",
  MASTER: "Master's",
  UNIFORM_MASTER: 'Uniform master’s',
  PHD: 'PhD',
};

export const genderLabels: Record<Gender, string> = {
  WOMAN: 'Woman',
  MAN: 'Man',
  NON_BINARY: 'Non-binary',
};

export const internshipRoleLabels: Record<InternshipRole, string> = {
  LOOKING: "I'm looking for an internship",
  OFFERING: 'I work somewhere and can help',
};

export const reportReasonLabels: Record<ReportReason, string> = {
  FAKE_PROFILE: 'Fake profile or not a student',
  HARASSMENT: 'Harassment or threats',
  INAPPROPRIATE_PHOTOS: 'Inappropriate photos',
  SPAM: 'Spam or selling something',
  UNDERAGE: 'Looks under 18',
  OTHER: 'Something else',
};

/** "2nd year, Bachelor's · Psychology" */
export function studyLine(p: { studyYear: number; degreeLevel: DegreeLevel; program: string }) {
  return `Year ${p.studyYear}, ${degreeLabels[p.degreeLevel]} · ${p.program}`;
}
