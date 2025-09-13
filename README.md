## Streaming Pagination Demo (Ktor SSE + StreamingPager)

Real‑time pagination demo that keeps lists fresh without polling. The server streams updates via Server‑Sent Events (
SSE) and the client maintains an always‑up‑to‑date paged list using `StreamingPager` from `ua.wwind.paging:paging-core`.

### What this shows

- **SSE‑driven pagination**: server pushes total count and page windows as data changes
- **Always‑fresh UI**: `StreamingPager` merges total/page streams and updates visible rows automatically
- **Resilience**: transparent restaging on expired filter IDs (403) and reconnection handling
- **Kotlin Multiplatform**: shared client logic for Android, iOS, Desktop (JVM), and Web

Read the full write‑up with a flow diagram in `ARTICLE.md`.

### How it works (high level)

1) Client stages current filters: `POST /orders/sse` → `requestId`
2) Client opens two SSE streams with header `X-SSE-Request-ID`:
    - `GET /orders/sse/count` → streams total item count
    - `GET /orders/sse?position=X&size=Y` → streams current page as `{ index -> item }`
3) On DB changes, the server re‑queries and pushes fresh count/page
4) Client’s `StreamingPager` merges these flows into `PagingData` and updates the UI

Production note: use an external store for staged filters (e.g., Redis) rather than in‑memory cache to survive restarts
and scale horizontally.

---

## Project structure

- `server/` — Ktor server with SSE endpoints and in‑memory H2 DB
- `shared/` — shared Kotlin Multiplatform code (domain, networking, repo with `StreamingPager`)
- `composeApp/` — Compose Multiplatform UI (Android/Desktop/Web targets)
- `iosApp/` — iOS entry point (Xcode project)

---

## Quick start

Open two terminals.

- Start the server

```bash
./gradlew :server:run
```

- Run the UI (pick one target)
    - Desktop (JVM)
  ```bash
  ./gradlew :composeApp:run
  ```
    - Android (assemble debug)
  ```bash
  ./gradlew :composeApp:assembleDebug
  ```
    - Web (Wasm)
  ```bash
  ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
  ```
    - iOS: open `iosApp/` in Xcode and run

Then open the Orders screen and watch the list update as the server inserts random orders.

---

## API endpoints (server)

- `POST /orders/sse` — stage current filters, returns `requestId`
- `GET /orders/sse/count` — SSE stream of total count; requires header `X-SSE-Request-ID: <id>`
- `GET /orders/sse?position=<pos>&size=<size>` — SSE stream of a page window as JSON map `{ index -> item }`; requires
  header `X-SSE-Request-ID`

Response semantics:

- If `requestId` is missing/expired, the server responds `403` and the client should restage filters and reopen streams

---

## Key dependencies

- `ua.wwind.paging:paging-core:2.2.1` — StreamingPager core
- Ktor 3.3.x — server, client, SSE, and JSON serialization

See `ARTICLE.md` for full dependency snippets.

---

## Notes for production

- Store staged filters in Redis or a DB table (not in‑memory) to survive restarts and enable horizontal scaling
- Authenticate the staging endpoint and validate ownership of `requestId`
- Keep SSE handlers lightweight; offload heavy work to background jobs

---

## Links

- Article and flow diagram: see `ARTICLE.md`