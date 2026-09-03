import { useEffect, useState } from "react";

/**
 * Milliseconds left until `iso`, ticking once a second.
 *
 * Returns null when there is no deadline, so callers can tell "no timer" apart
 * from "timer finished" — the difference between a button that was never
 * time-limited and one whose window just closed.
 */
export function useCountdown(iso: string | null | undefined): number | null {
  const target = iso ? new Date(iso).getTime() : null;
  const [remaining, setRemaining] = useState(() =>
    target == null ? null : target - Date.now()
  );

  useEffect(() => {
    if (target == null) {
      setRemaining(null);
      return;
    }
    setRemaining(target - Date.now());
    const id = setInterval(() => setRemaining(target - Date.now()), 1000);
    return () => clearInterval(id);
  }, [target]);

  return remaining;
}
