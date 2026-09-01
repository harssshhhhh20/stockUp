# StockUp — mobile app

React Native (Expo) app for both sides of StockUp: shoppers asking nearby shops
what's in stock, and shopkeepers replying and handing orders over.

## Running it

The app talks to the Spring Boot backend, so start that first:

```bash
cd ../infrastructure/compose && docker compose up -d
```

```bash
cd ../backend && ./mvnw spring-boot:run
```

Then start the app:

```bash
npm install
npm start
```

Press `w` for the browser, `i` for the iOS Simulator (needs Xcode), or scan the
QR code with Expo Go on your phone.

### Testing on a physical phone

`localhost` on your phone is the phone itself, so point the app at your Mac's
LAN IP by creating `mobile/.env`:

```
EXPO_PUBLIC_API_URL=http://192.168.1.42:8080
```

Find the IP with `ipconfig getifaddr en0`. Both devices must be on the same
Wi-Fi network.

### Reading the OTP without real email

Run the backend with the console email provider and the login code is printed
to the server log instead of being emailed:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--stockup.email.provider=console"
```

## How it's put together

```
src/
├── theme/       tokens.ts (the design system) + statusMap.ts (enum → color meaning)
├── components/  shared UI primitives
├── api/         typed client + endpoints, matching the backend contracts
├── state/       AuthContext — session, role, merchant profile
├── screens/     grouped by audience: customer / merchant / shared / auth
└── navigation/  tab + stack structure, switches on merchant vs shopper mode
```

### The design system in one paragraph

One brand color (teal) means "StockUp / do this" and is never used for status.
Five semantic colors carry meaning everywhere, consistently: **green** available
/ healthy / done, **amber** pending / needs attention, **red** unavailable /
problem / expired, **blue** neutral info, **purple** special / featured. Every
backend enum is translated to those colors in a single file — `theme/statusMap.ts`
— so no screen invents its own meaning. Two recurring motifs carry the brand:
the **Bharosa Ring** (a merchant's trust score, same three-color banding) and
the **live timer** (a pulsing dot + countdown that escalates teal → amber → red
as a window closes).
