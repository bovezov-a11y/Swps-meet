/**
 * The backend URL. On a phone, `localhost` is the phone itself, so set EXPO_PUBLIC_API_URL to your computer's
 * address on the same Wi-Fi, e.g. http://192.168.1.20:8080 (see README).
 */
export const API_URL = (process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8080').replace(/\/$/, '');

/** An RFC 9457 problem detail from the backend. */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
    readonly code?: string,
    readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message);
  }

  /** The first field error, or the general message, for showing under a form. */
  get summary(): string {
    const first = Object.entries(this.fieldErrors)[0];
    return first ? `${humanize(first[0])}: ${first[1]}` : this.message;
  }
}

function humanize(field: string) {
  const words = field.replace(/([A-Z])/g, ' $1').toLowerCase();
  return words.charAt(0).toUpperCase() + words.slice(1);
}

let token: string | null = null;
let onUnauthorized: () => void = () => {};

/** Called by the auth provider; every request after this sends the token. */
export function setApiToken(value: string | null, unauthorized?: () => void) {
  token = value;
  if (unauthorized) onUnauthorized = unauthorized;
}

export function authHeaders(): Record<string, string> {
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export function photoUrl(photoId: string) {
  return `${API_URL}/api/photos/${photoId}`;
}

type Options = { method?: string; body?: unknown; form?: FormData };

export async function api<T>(path: string, { method = 'GET', body, form }: Options = {}): Promise<T> {
  let response: Response;
  try {
    response = await fetch(API_URL + path, {
      method,
      headers: {
        Accept: 'application/json',
        ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
        ...authHeaders(),
      },
      body: form ?? (body !== undefined ? JSON.stringify(body) : undefined),
    });
  } catch {
    throw new ApiError(0, 'Cannot reach Meet SWPS. Check your internet connection.');
  }

  if (response.status === 401 && token) {
    // The session expired, or the account was suspended or deleted.
    onUnauthorized();
  }
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}));
    throw new ApiError(
      response.status,
      problem.detail ?? 'Something went wrong. Please try again.',
      problem.code,
      problem.errors ?? {},
    );
  }
  if (response.status === 204 || response.headers.get('Content-Length') === '0') {
    return undefined as T;
  }
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}
