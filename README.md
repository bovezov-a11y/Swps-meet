# Meet SWPS

A mobile app where SWPS University students meet each other: to find an internship contact, make friends,
go out together or date. Only people with a verified SWPS email can join.

> Unofficial student project. Not affiliated with or endorsed by SWPS University.

## How it works

1. **Sign up with an SWPS email** (`@st.swps.edu.pl` or `@swps.edu.pl`) and type in the 6-digit code from the email.
2. **Create a profile:** first name, date of birth (18+), campus, program, year, and what you are looking for.
   Optional: photos (up to 3), bio, interests, languages and Instagram.
3. **Pick your sections:** 💼 Internships, 🤝 Friends, 🎉 Socialising and 💜 Looking for fun. Each one is its own list of
   students, and you can be in several.
4. **Say you want to meet someone.** They only find out if they want to meet you too. Then it is a match: you can
   chat and see each other's Instagram.
5. **Get notified** on your phone about new matches and messages; tapping the notification opens the chat.

### Privacy and safety

- Others see your first name and age, never your surname, email or date of birth.
- You only appear in sections you chose, to students who chose them too.
- The Looking for fun section is only visible to students in it, and only to people whose preferences fit yours both ways.
  Internship and Looking for fun answers are deleted when you leave that section.
- Instagram is shown to matches only.
- Block or report anyone. Reporting also blocks. A profile reported by 3 students is hidden until a moderator
  reviews it; moderators can restore it or suspend the account (which logs it out immediately).
- Delete your account in the app; everything is removed.

## Project layout

| Folder      | What it is                                                                 |
|-------------|----------------------------------------------------------------------------|
| `backend/`  | Spring Boot 4 API (Java 21), PostgreSQL, Flyway, JWT auth                   |
| `mobile/`   | Expo (React Native) app for iOS and Android, Expo Router, TanStack Query    |

The backend reuses the registration, email verification and moderation approach of
[SWPS Hub](https://github.com/bovezov-a11y/swps-hub).

## Running it locally

You need Java 21, Node 22, Docker, and the **Expo Go** app on your phone.

```bash
# 1. Backend (starts PostgreSQL and Mailpit in Docker automatically)
cd backend
./mvnw spring-boot:run
# API on http://localhost:8080, API docs on http://localhost:8080/swagger-ui.html
# Verification emails land in Mailpit: http://localhost:8025

# 2. Mobile app
cd mobile
npm install
cp .env.example .env.local    # put your computer's Wi-Fi IP address in EXPO_PUBLIC_API_URL
npx expo start                # scan the QR code with Expo Go
```

To try the moderator tools, start the backend with `ADMIN_EMAILS=you@swps.edu.pl`; that account can appoint
moderators through `POST /api/admin/moderators`.

### Push notifications

Push notifications do not work in Expo Go; they need a *development build* of the app (your own version of Expo
Go with Meet SWPS inside). One-time setup:

1. Create a free account at [expo.dev](https://expo.dev/signup).
2. In `mobile/`: `npx eas-cli@latest login`, then `npx eas-cli@latest init`. This adds the project id to `app.json`.
3. Android: `npx eas-cli@latest build --profile development --platform android`, then open the link it prints on
   your phone and install the app. (Push on Android also needs Firebase credentials; EAS walks you through it, see
   [FCM credentials](https://docs.expo.dev/push-notifications/fcm-credentials/).)
   iPhone: the same with `--platform ios`, which needs a paid Apple Developer account.
4. Start the bundler with `npx expo start --dev-client` and open it from the installed app.

Everything else keeps working in Expo Go; the app simply skips notifications there.

### Tests

```bash
cd backend && ./mvnw verify            # API tests on PostgreSQL in Docker (Testcontainers)
cd mobile && npm run lint && npm run typecheck
```

Without Docker, point the backend tests at any empty PostgreSQL database with `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`.

## Configuration (backend)

| Variable               | Purpose                                                        |
|------------------------|----------------------------------------------------------------|
| `JWT_SECRET`           | Base64 key for signing sessions (`openssl rand -base64 32`). Required in production. |
| `SPRING_DATASOURCE_*`  | PostgreSQL connection                                          |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | SMTP server for verification codes |
| `STORAGE_DIR`          | Where profile photos are stored (mount a volume in production) |
| `ADMIN_EMAILS`         | Comma-separated accounts that get the admin role               |
| `EXPO_ACCESS_TOKEN`    | Only if "enhanced push security" is turned on in the Expo project |
| `CORS_ALLOWED_ORIGINS` | Browser origins allowed to call the API (only the Expo web preview needs it) |

## API overview

| Endpoint | Purpose |
|----------|---------|
| `POST /api/auth/register`, `/verify-email`, `/resend-verification`, `/login` | Sign-up with emailed code, login |
| `GET /api/account`, `DELETE /api/account` | Current account; delete account (password required) |
| `GET/PUT /api/profile`, `POST/DELETE /api/profile/photos`, `PUT /api/profile/photos/order` | Own profile and photos |
| `GET /api/discover?intent=FRIENDS&campus=&program=&interest=&page=` | Students in a section |
| `GET /api/profiles/{id}`, `GET /api/photos/{id}` | Another student, as you are allowed to see them |
| `PUT/DELETE /api/likes/{id}?intent=` | Want to meet / take it back |
| `GET /api/matches`, `DELETE /api/matches/{id}`, `GET/POST /api/matches/{id}/messages` | Matches and chat |
| `PUT/DELETE /api/push-tokens` | Register this phone for notifications / stop (on logout) |
| `PUT/DELETE /api/blocks/{id}`, `GET /api/blocks`, `POST /api/reports` | Safety |
| `GET /api/moderation/reports`, `POST /api/moderation/users/{id}/restore` or `/ban` | Moderators |

## Ideas for next steps

- Real-time chat over WebSockets instead of polling
- Group chats or events per section
- Polish and Turkish translations
- Moderator screens in the app, photo moderation, S3 storage for photos
- App Store / Google Play release with EAS Build
