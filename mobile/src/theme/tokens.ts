/**
 * StockUp design tokens.
 *
 * Brand: a single deep teal ("Bazaar Teal") carries identity — primary actions,
 * headers, the brand mark. It never doubles as a status color, so it's always
 * unambiguous: teal = "this is StockUp / do this," never "this is a state."
 *
 * Status: a fixed five-color semantic system used identically everywhere in the
 * app — an order, a basket, a notification and a merchant's own trust score all
 * read the same color the same way. Users should stop consciously reading labels
 * and start reading color.
 *
 *   positive (green)  — available, healthy, completed, trusted
 *   attention (amber)  — pending, needs a look, awaiting response
 *   urgent (red)        — unavailable, problem, expiring, cancelled
 *   info (blue)          — neutral, FYI, in progress
 *   special (purple)   — featured, IPO/exclusive, admin override
 */

export const color = {
  brand: {
    50: "#E9F5F3",
    100: "#CBE9E5",
    300: "#5FAFA7",
    500: "#0E7C74",
    600: "#0B655F",
    700: "#0A544F",
  },

  // secondary warm accent — used ONLY for decorative/illustrative moments
  // (empty states, onboarding), never for actions or status.
  marigold: {
    100: "#FBEBCB",
    400: "#E8A33D",
    600: "#C97F1D",
  },

  status: {
    positive: { soft: "#E3F5EA", base: "#2E9E5B", strong: "#1F7A44" },
    attention: { soft: "#FDF0DD", base: "#D97706", strong: "#9A5B0C" },
    urgent: { soft: "#FBE7E5", base: "#D6503F", strong: "#A83A2C" },
    info: { soft: "#E6EEF6", base: "#3B6EA5", strong: "#2C5480" },
    special: { soft: "#F0EBFA", base: "#7C5CBF", strong: "#5E4394" },
  },

  // Three text weights, all verified ≥4.5:1 (WCAG AA) against both the app
  // background and white cards — inkFaint is used for timestamps and captions,
  // which are small, so it must not be decorative-grey.
  neutral: {
    background: "#F5F3EA",
    surface: "#FFFFFF",
    surfaceMuted: "#ECE8DB",
    border: "#DEDACB",
    borderStrong: "#C7C2AE",
    ink: "#1C2321", // 14.4:1
    inkMuted: "#4E5852", // 6.7:1
    inkFaint: "#686F66", // 4.7:1
    onBrand: "#FFFFFF",
  },
} as const;

export const spacing = {
  xxs: 4,
  xs: 8,
  sm: 12,
  md: 16,
  lg: 20,
  xl: 24,
  xxl: 32,
  xxxl: 40,
} as const;

export const radius = {
  sm: 8,
  md: 12,
  lg: 18,
  pill: 999,
} as const;

export const font = {
  display: {
    regular: "Baloo2_500Medium",
    semibold: "Baloo2_600SemiBold",
    bold: "Baloo2_700Bold",
    extrabold: "Baloo2_800ExtraBold",
  },
  body: {
    regular: "Manrope_400Regular",
    medium: "Manrope_500Medium",
    semibold: "Manrope_600SemiBold",
    bold: "Manrope_700Bold",
  },
  mono: {
    medium: "JetBrainsMono_500Medium",
    semibold: "JetBrainsMono_600SemiBold",
    bold: "JetBrainsMono_700Bold",
  },
} as const;

export const type = {
  hero: { fontSize: 30, lineHeight: 36 },
  h1: { fontSize: 24, lineHeight: 30 },
  h2: { fontSize: 19, lineHeight: 25 },
  h3: { fontSize: 16, lineHeight: 22 },
  body: { fontSize: 15, lineHeight: 22 },
  bodySm: { fontSize: 13, lineHeight: 19 },
  caption: { fontSize: 11.5, lineHeight: 15 },
} as const;

/**
 * Phone-first, but on a tablet or a wide web window the content column stops
 * growing so lines stay readable instead of stretching edge to edge.
 */
export const layout = {
  maxContentWidth: 620,
} as const;

export const shadow = {
  card: {
    shadowColor: "#141914",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 6,
    elevation: 2,
  },
  floating: {
    shadowColor: "#141914",
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.14,
    shadowRadius: 20,
    elevation: 8,
  },
} as const;

export type StatusKey = keyof typeof color.status;
