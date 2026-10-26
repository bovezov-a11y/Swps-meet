# Meet SWPS – mobile app

Expo (React Native) app for iOS and Android. See the [root README](../README.md) for the whole project.

```bash
npm install
cp .env.example .env.local   # set EXPO_PUBLIC_API_URL to your computer's IP address
npx expo start               # scan the QR code with Expo Go on your phone
```

| Command             | What it does                    |
|---------------------|---------------------------------|
| `npm run lint`      | ESLint                          |
| `npm run typecheck` | TypeScript                      |
| `npx expo start --web` | Browser preview (development only; needs the backend's CORS setting) |

Code layout: screens live in `src/app` (Expo Router), shared components in `src/components`, API calls and types
in `src/lib`.
