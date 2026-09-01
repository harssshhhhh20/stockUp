import React, { useCallback, useState } from "react";
import { ActivityIndicator, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation, useRoute } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { StatusPill } from "../../components/StatusPill";
import { TextField } from "../../components/TextField";
import { useToast } from "../../components/Toast";
import { NotificationApi, ReservationApi } from "../../api/endpoints";
import { AppNotification, Reservation } from "../../api/types";
import { reservationStatus } from "../../theme/statusMap";
import { color, radius, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";
import { ApiError } from "../../api/client";

/**
 * The OTP the customer shows at pickup is delivered as a notification
 * (RESERVATION_ACTIVATED) rather than on the reservation itself, so we surface
 * it here by reading the matching notification — the shopper should never have
 * to go hunting through a feed for the code that collects their order.
 */
function extractOtp(message: string): string | null {
  const match = message.match(/\b(\d{6})\b/);
  return match ? match[1] : null;
}

export function ReservationDetailScreen() {
  const nav = useNavigation<any>();
  const route = useRoute<any>();
  const { reservationId } = route.params as { reservationId: string };
  const toast = useToast();

  const [reservation, setReservation] = useState<Reservation | null>(null);
  const [otp, setOtp] = useState<string | null>(null);
  const [cancelling, setCancelling] = useState(false);
  const [reason, setReason] = useState("");
  const [showCancel, setShowCancel] = useState(false);

  const load = useCallback(async () => {
    try {
      const r = await ReservationApi.detail(reservationId);
      setReservation(r);

      if (r.status === "ACTIVE") {
        const notifs = await NotificationApi.list(0, 30);
        const match = notifs.content.find(
          (n: AppNotification) =>
            n.type === "RESERVATION_ACTIVATED" && n.referenceId === reservationId
        );
        if (match) setOtp(extractOtp(match.message));
      }
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
  const canCancel = reservation?.status === "PENDING_NOTIFICATION";

  return (
    <View style={styles.flex}>
      <AppBar title="Your reservation" onBack={() => nav.goBack()} />
      <ScrollView contentContainerStyle={[styles.content, contentWidth.column]} showsVerticalScrollIndicator={false}>
        {!reservation ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : (
          <>
            <Card elevated>
              {s ? <StatusPill status={s.status} label={s.label} /> : null}
              <Text variant="body" color={color.neutral.inkMuted} style={styles.explain}>
                {reservation.status === "PENDING_NOTIFICATION"
                  ? "We're letting the shop know. This takes a moment."
                  : reservation.status === "ACTIVE"
                  ? "Your order is being held. Show the code below at the shop."
                  : reservation.status === "COMPLETED"
                  ? "Picked up. Thanks for shopping local!"
                  : reservation.status === "EXPIRED"
                  ? "This reservation ran out of time and the shop released it."
                  : "This reservation was cancelled."}
              </Text>
            </Card>

            {reservation.status === "ACTIVE" && otp ? (
              <Card elevated style={styles.otpCard}>
                <Text variant="caption" color={color.brand[600]}>
                  Show this at the shop
                </Text>
                <Text style={styles.otpValue}>{otp}</Text>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  The shopkeeper enters this code to complete your pickup.
                </Text>
              </Card>
            ) : null}

            {canCancel && !showCancel ? (
              <Button
                label="Cancel this reservation"
                variant="secondary"
                onPress={() => setShowCancel(true)}
              />
            ) : null}

            {canCancel && showCancel ? (
              <Card elevated>
                <Text variant="h3">Why are you cancelling?</Text>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  The shop set items aside for you, so a quick reason helps them.
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
  otpValue: {
    fontFamily: "JetBrainsMono_700Bold",
    fontSize: 40,
    letterSpacing: 6,
    color: color.brand[700],
    marginVertical: spacing.xs,
  },
  reasonInput: {
    height: 80,
    paddingTop: spacing.sm,
    textAlignVertical: "top",
  },
  loader: {
    marginTop: spacing.xxl,
  },
});
