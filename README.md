# StockUp

Ask the shops on your street what's in stock — before you walk over.

A shopper posts a list of what they need. Nearby shops get it, reply with what
they actually have, and the shopper reserves at whichever shop suits them. The
handover is confirmed with a 6-digit code.

## Repository layout

```
backend/          Spring Boot 3.5 / Java 21 API (Postgres, Redis, Flyway)
mobile/           React Native (Expo) app — both shopper and shopkeeper
infrastructure/   docker compose for Postgres, Redis, RabbitMQ
docs/             changelog and notes
admin/            (empty — no admin UI yet; the admin API lives in the backend)
```

## Running it locally

Start the infrastructure:

```bash
cd infrastructure/compose && docker compose up -d
```

Start the API (defaults to printing login codes to the log, so no email
provider is needed):

```bash
cd backend && ./mvnw spring-boot:run
```

Start the app:

```bash
cd mobile && npm install && npm start
```

Press `w` for the browser, or scan the QR code with Expo Go. See
[`mobile/README.md`](mobile/README.md) for testing on a physical phone.

## Configuration

All secrets come from the environment — see [`.env.example`](.env.example).
Nothing is required for local development.

| Variable | Purpose |
|---|---|
| `EMAIL_PROVIDER` | `console` (default) logs login codes; `brevo` sends real email |
| `BREVO_API_KEY` | Brevo key, only needed when `EMAIL_PROVIDER=brevo` |
| `EMAIL_SENDER` | From-address for outgoing mail |
| `JWT_SECRET` | Token signing key — **set this in any deployed environment** |
| `ADMIN_EMAIL` | Grants `ADMIN` to this account on boot (the user must already exist) |

## How the domain fits together

1. **Basket** — a shopper's list. Broadcasts to nearby shops shortly after
   creation, then expires 15 minutes later.
2. **Broadcast** — the fan-out to shops within the search radius. Each shop is
   a *recipient* and moves `PENDING → VIEWED → RESPONDED`.
3. **Merchant offer** — a shop's reply, per item: available, partial, or none.
4. **Reservation** — the shopper commits to one offer.
   `PENDING_NOTIFICATION → ACTIVE` (an OTP is issued) `→ COMPLETED`, or a
   cancellation/expiry.
5. **Notification** — every step above writes to an in-app feed and mirrors to
   email.

### Bharosa Score

A 0–100 trust score per merchant, starting at 100, adjusted automatically:

| Event | Delta |
|---|---|
| Completed a pickup | +5 |
| Replied to a request | +2 |
| Never opened a request before it expired | −2 |
| Opened a request, then let it expire unanswered | −8 |
| Cancelled a confirmed reservation | −10 |
| Let a confirmed reservation expire unfulfilled | −25 |

Ignoring a request you've seen costs more than never seeing it — that's
deliberate. Merchants also can't cancel within 3 minutes of a reservation
expiring, so a shopper isn't stranded with no time to react.

## Tests

```bash
cd backend && ./mvnw test
```

## Known gaps

- No push notifications (in-app feed and email only).
- No admin UI — `/api/v1/admin/**` exists, but nothing consumes it.
- Baskets can only target nearby shops; the backend also supports targeting
  specific shops, but the app doesn't expose it.
- The app has not been tested on a physical device.
