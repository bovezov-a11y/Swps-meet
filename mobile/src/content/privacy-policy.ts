/** Must match app.privacy.policy-version in backend/src/main/resources/application.yml. */
export const PRIVACY_POLICY_VERSION = '1.0';

export const PRIVACY_POLICY: { heading: string; text: string }[] = [
  {
    heading: 'Who we are',
    text: 'Meet SWPS is an unofficial app made by students for students of SWPS University. It is not run or endorsed by the university.',
  },
  {
    heading: 'What we store',
    text: 'Your SWPS email address, a hash of your password, and the profile you fill in: first name, date of birth, campus, program, year, the sections you choose, and optionally a bio, interests, languages, photos and your Instagram username. We also store your likes, matches, messages, blocks and reports, and, if you allow notifications, a push token for your phone.',
  },
  {
    heading: 'Who sees what',
    text: 'Other students only see you in sections you have both chosen. They see your first name and age, never your surname, email or date of birth. Your Instagram username is only shown to people you match with. The Looking for fun section is only visible to other students in it, and only to people whose preferences fit yours. Nobody is told when you like, unmatch, block or report them.',
  },
  {
    heading: 'Safety and moderation',
    text: 'You can block or report anyone. Reporting also blocks them. A profile reported by several students is hidden until a volunteer moderator reviews it. Moderators see the reports and the reported account, but never who reported it.',
  },
  {
    heading: 'Your rights',
    text: 'You can change your profile at any time, and delete your account in Settings. Deleting it permanently removes your profile, photos, likes, matches, messages and blocks. Under the GDPR you can also ask us for a copy of your data.',
  },
];
