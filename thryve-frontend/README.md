# Edu Frontend Starter

A minimal React + Vite + TypeScript + Tailwind app wired to your Spring backend.

## Quick start

1. **Install Node.js LTS** (>= 18) and npm.
2. Unzip this folder, open a terminal in it, then run:
   ```bash
   npm install
   cp .env.example .env
   # edit .env if your backend isn't http://localhost:8080
   npm run dev
   ```
3. Make sure your **Spring backend** is running on `http://localhost:8080` and exposes `/api/...` endpoints.

### Sign in
- Open `http://localhost:5173/login`
- Use a valid user from your backend DB. After login we store `accessToken` and `refreshToken` in `localStorage` and redirect to `/`.

### Pages included
- `/login` — basic sign-in form calling `POST /api/auth/login`.
- `/` — Dashboard with a Logout button.
- `/courses` — Paged list using `GET /api/courses?pageNumber=&pageSize=&sortBy=&direction=`
- `/courses/:id` — Course detail + sections using `GET /api/courses/{id}` and `GET /api/courses/{id}/sections`

### API client
- Config in `src/lib/api.ts`
- Base URL comes from `.env` → `VITE_API_URL`
- Attaches `Authorization: Bearer <accessToken>`
- On `401`, automatically calls `POST /api/auth/refresh-token` with `{ refreshToken }` and retries the original request.

### Tailwind
- Configured in `tailwind.config.js` and `postcss.config.js`.
- Utilities are ready in `src/index.css`.

## Troubleshooting

**CORS error in console**
- Add a CORS bean in Spring Security allowing origin `http://localhost:5173` and methods `GET,POST,PUT,PATCH,DELETE,OPTIONS`.

**401 on every request**
- Confirm `Authorization` header is present (DevTools → Network).
- Make sure `POST /api/auth/login` returns both `accessToken` and `refreshToken` and they’re stored in `localStorage`.

**Refresh loop**
- Check `POST /api/auth/refresh-token` returns a new `accessToken`. If you rotate refresh tokens, also return `refreshToken` and we’ll store it.

**404 /api/courses**
- Verify your backend base path: it should be `/api/...`. If not, adjust calls in the code (`/api/...`).

**Port already in use 5173**
- Vite will suggest another port; allow it or stop the other process.
