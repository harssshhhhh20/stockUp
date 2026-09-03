import React, { useCallback, useEffect, useState } from "react";
import { ActivityIndicator, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation, useRoute } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { StatusPill } from "../../components/StatusPill";
import { TextField } from "../../components/TextField";
import { useToast } from "../../components/Toast";
import { ReservationApi } from "../../api/endpoints";
import { Reservation } from "../../api/types";
import { reservationStatus } from "../../theme/statusMap";
import { color, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";
import { ApiError } from "../../api/client";
import { openDirections } from "../../lib/directions";
import { useCountdown } from "../../lib/useCountdown";
import { LiveTimer } from "../../components/LiveTimer";

/** Statuses where the shopper has no reason to walk anywhere. */
const TERMINAL: string[] = [
  "COMPLETED",
  "EXPIRED",
  "MERCHANT_CANCELLED",
  "CUSTOMER_CANCELLED",
];

export function ReservationDetailScreen() {
  const nav = useNavigation<any>();
  const route = useRoute<any>();
  const { reservationId } = route.params as { reservationId: string };
  const toast = useToast();

  const [reservation, setReservation] = useState<Reservation | null>(null);
  const [cancelling, setCancelling] = useState(false);
  const [reason, setReason] = useState("");
  const [showCancel, setShowCancel] = useState(false);

  const load = useCallback(async () => {
    try {
      setReservation(await ReservationApi.detail(reservationId));
    } catch {
      // handled by empty render
    }
  }, [reservationId]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  async function cancel() {
    setCancelling(true);
    try {
      await ReservationApi.cancel(reservationId, reason.trim());
      toast("Reservation cancelled", "info");
      await load();
      setShowCancel(false);
      setReason("");
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "Couldn't cancel", "urgent");
    } finally {
      setCancelling(false);
    }
  }

  const s = reservation ? reservationStatus(reservation.status) : null;

  /**
   * The shop is told about this order two minutes after it's placed. Until then
   * the customer can call it off; after, the shopkeeper has already started
   * setting things aside. That window is the countdown beside the button.
   */
  const cancelWindow = useCountdown(reservation?.cancellableUntil);
  const windowClosed = cancelWindow != null && cancelWindow <= 0;
  const canCancel = reservation?.status === "PENDING_NOTIFICATION";

  /**
   * The server brings the reservation live on read once the window has passed,
   * so one refetch is normally enough. We retry a few times anyway: the phone's
   * clock and the server's rarely agree to the second, and firing a moment
   * early would otherwise leave the screen showing "confirming" with no code
   * and nothing left to trigger another look.
   */
  useEffect(() => {
    if (!canCancel || !windowClosed) return;

    let attempts = 0;
    let cancelled = false;

    const tick = async () => {
      if (cancelled) return;
      attempts += 1;
      await load();
      if (!cancelled && attempts < 5) timer = setTimeout(tick, 2000);
    };

    let timer = setTimeout(tick, 0);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [canCancel, windowClosed, load]);

  return (
    <View style={styles.flex}>
      <AppBar
        title="Your reservation"
        subtitle={reservation?.storeName}
        onBack={() => nav.goBack()}
      />
      <ScrollView contentContainerStyle={[styles.content, contentWidth.column]} showsVerticalScrollIndicator={false}>
        {!reservation ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : (
          <>
            <Card elevated>
              {s ? <StatusPill status={s.status} label={s.label} /> : null}
              <Text variant="body" color={color.neutral.inkMuted} style={styles.explain}>
                {reservation.status === "PENDING_NOTIFICATION"
                  ? "The shop hasn't been told yet — you can still change your mind."
                  : reservation.status === "ACTIVE"
                  ? "Your order is being held. Show the code below at the shop."
                  : reservation.status === "COMPLETED"
                  ? "Picked up. Thanks for shopping local!"
                  : reservation.status === "EXPIRED"
                  ? "This reservation ran out of time and the shop released it."
                  : "This reservation was cancelled."}
              </Text>
            </Card>

            {/* Where to go. Placed above the pickup code because you need the
                shop before you need the code, and a held order the shopper
                can't find is an order that expires. */}
            {!TERMINAL.includes(reservation.status) ? (
              <Card elevated>
                <Text variant="caption" color={color.neutral.inkMuted}>
                  Collect from
                </Text>
                <Text variant="h3">{reservation.storeName}</Text>
                {reservation.storeAddress ? (
                  <Text variant="bodySm" color={color.neutral.inkMuted}>
                    {reservation.storeAddress}
                  </Text>
                ) : null}
                <Button
                  label="Take me to the shop"
                  variant="secondary"
                  onPress={async () => {
                    const opened = await openDirections({
                      name: reservation.storeName,
                      address: reservation.storeAddress,
                      latitude: reservation.storeLatitude,
                      longitude: reservation.storeLongitude,
                    });
                    if (!opened) {
                      toast("No address on file for this shop yet.", "attention");
                    }
                  }}
                  style={styles.directions}
                />
              </Card>
            ) : null}

            {reservation.status === "ACTIVE" && reservation.otp ? (
              <Card elevated style={styles.otpCard}>
                <Text variant="caption" color={color.brand[600]}>
                  Show this at the shop
                </Text>
                <Text style={styles.otpValue}>{reservation.otp}</Text>
                {reservation.expiresAt ? (
                  <LiveTimer expiresAt={reservation.expiresAt} label="held" />
                ) : null}
                <Text
                  variant="bodySm"
                  color={color.neutral.inkMuted}
                  style={styles.otpHint}
                >
                  The shopkeeper enters this code to complete your pickup.
                </Text>
              </Card>
            ) : null}

            {canCancel && !showCancel ? (
              <Card elevated>
                <View style={styles.rowBetween}>
                  <Text variant="caption" color={color.neutral.inkMuted}>
                    Changed your mind?
                  </Text>
                  {reservation.cancellableUntil && !windowClosed ? (
                    <LiveTimer expiresAt={reservation.cancellableUntil} label="until" />
                  ) : null}
                </View>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  {windowClosed
                    ? "The shop has been told about this order, so it can't be called off here. Ask them at the counter."
                    : "Call it off now and the shop never hears about it."}
                </Text>
                <Button
                  label="Cancel this reservation"
                  variant="secondary"
                  onPress={() => setShowCancel(true)}
                  disabled={windowClosed}
                  style={styles.cancelBtn}
                />
              </Card>
            ) : null}

            {canCancel && showCancel ? (
              <Card elevated>
                <Text variant="h3">Why are you cancelling?</Text>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  The shop hasn't been told about this order yet, so nobody is left
                  waiting. The reason is just for your own records.
                </Text>
                <TextField
                  placeholder="e.g. Found it closer to home"
                  value={reason}
                  onChangeText={setReason}
                  multiline
                  style={styles.reasonInput}
                />
                <Button
                  label="Confirm cancellation"
                  variant="danger"
                  onPress={cancel}
                  loading={cancelling}
                  disabled={!reason.trim()}
                />
                <Button label="Keep it" variant="ghost" onPress={() => setShowCancel(false)} />
              </Card>
            ) : null}
          </>
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1, backgroundColor: color.neutral.background },
  content: {
    padding: spacing.lg,
    paddingTop: 0,
    gap: spacing.sm,
    paddingBottom: spacing.xxxl,
  },
  explain: {
    marginTop: spacing.xxs,
  },
  otpCard: {
    backgroundColor: color.brand[50],
    borderColor: color.brand[100],
    alignItems: "center",
    paddingVertical: spacing.xl,
  },
  otpHint: { textAlign: "center", marginTop: spacing.xs },
  otpValue: {
    fontFamily: "JetBrainsMono_700Bold",
    fontSize: 40,
    lineHeight: 50,
    letterSpacing: 6,
    color: color.brand[700],
    marginVertical: spacing.xs,
  },
  reasonInput: {
    height: 80,
    paddingTop: spacing.sm,
    textAlignVertical: "top",
  },
  rowBetween: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    gap: spacing.xs,
  },
  directions: { marginTop: spacing.sm },
  cancelBtn: { marginTop: spacing.sm },
  loader: {
    marginTop: spacing.xxl,
  },
});
