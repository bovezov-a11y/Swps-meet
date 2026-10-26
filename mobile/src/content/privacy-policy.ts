/** Must match app.privacy.policy-version in backend/src/main/resources/application.yml. */
export const PRIVACY_POLICY_VERSION = '1.1';

export const PRIVACY_POLICY: { heading: string; text: string }[] = [
  {
    heading: 'Who we are',
    text: 'Meet SWPS is an unofficial app made by students for students of SWPS University. It is not run or endorsed by the university.',
  },
  {
    heading: 'What we store',
    text: 'Your SWPS email address, a hash of your password, and the profile you fill in: first name, date of birth, campus, program, year, the sections you choose, and optionally a bio, interests, languages, photos and your Instagram username. We also store your likes, matches, messages, blocks and reports, and, if you allow notifications, a push token for your phone. We do not ask for your gender, sexual orientation or any other sensitive data.',
  },
  {
    heading: 'Why we use it',
    text: 'We use your data only to run the app: to check that you are an SWPS student and an adult, to show your profile to students in the sections you chose, to deliver messages and notifications, and to keep the app safe. The legal basis is our agreement with you when you sign up (GDPR art. 6(1)(b)) and, for safety and moderation, our legitimate interest in protecting students (art. 6(1)(f)). We never sell your data or use it for advertising.',
  },
  {
    heading: 'Who sees what',
    text: 'Other students only see you in sections you have both chosen. They see your first name and age, never your surname, email or date of birth. Your Instagram username is only shown to people you match with. Nobody is told when you like, unmatch, block or report them.',
  },
  {
    heading: 'Safety and moderation',
    text: 'You can block or report anyone. Reporting also blocks them. A profile reported by several students is hidden until a volunteer moderator reviews it. Moderators see the reports and the reported account, but never who reported it.',
  },
  {
    heading: 'How long we keep it',
    text: 'We keep your data while you have an account. When you delete your account, your profile, photos, likes, matches, messages, blocks and reports are deleted at once.',
  },
  {
    heading: 'Your rights',
    text: 'You can change your profile at any time. In the Me tab you can download a copy of all your data (Download my data) and delete your account. Under the GDPR you also have the right to have wrong data corrected, to object to how we use it, and to complain to the Polish data protection authority (UODO).',
  },
];
