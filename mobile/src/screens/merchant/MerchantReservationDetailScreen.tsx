import React, { useCallback, useState } from "react";
import { ActivityIndicator, ScrollView, StyleSheet, View } from "react-native";
import { useFocusEffect, useNavigation, useRoute } from "@react-navigation/native";
import { AppBar } from "../../components/AppBar";
import { Card } from "../../components/Card";
import { Text } from "../../components/Text";
import { Button } from "../../components/Button";
import { StatusPill } from "../../components/StatusPill";
import { OtpInput } from "../../components/OtpInput";
import { TextField } from "../../components/TextField";
import { useToast } from "../../components/Toast";
import { ReservationApi } from "../../api/endpoints";
import { Reservation } from "../../api/types";
import { reservationStatus } from "../../theme/statusMap";
import { useAuth } from "../../state/AuthContext";
import { color, spacing } from "../../theme/tokens";
import { contentWidth } from "../../theme/layoutStyles";
import { ApiError } from "../../api/client";

export function MerchantReservationDetailScreen() {
  const nav = useNavigation<any>();
  const route = useRoute<any>();
  const { reservationId } = route.params as { reservationId: string };
  const toast = useToast();
  const { refreshMerchantState } = useAuth();

  const [reservation, setReservation] = useState<Reservation | null>(null);
  const [otp, setOtp] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showCancel, setShowCancel] = useState(false);
  const [reason, setReason] = useState("");

  const load = useCallback(async () => {
    try {
      setReservation(await ReservationApi.detail(reservationId));
    } catch {
      // rendered as loading-empty
    }
  }, [reservationId]);

  useFocusEffect(
    useCallback(() => {
      load();
    }, [load])
  );

  async function complete() {
    setBusy(true);
    setError(null);
    try {
      await ReservationApi.complete(reservationId, otp);
      toast("Order handed over 🎉  +5 Bharosa", "positive");
      await Promise.all([load(), refreshMerchantState()]);
      setOtp("");
    } catch (e) {
      setError(
        e instanceof ApiError
          ? "That code doesn't match. Ask the customer to check again."
          : "Something went wrong."
      );
    } finally {
      setBusy(false);
    }
  }

  async function cancel() {
    setBusy(true);
    try {
      await ReservationApi.cancel(reservationId, reason.trim());
      toast("Reservation cancelled", "info");
      await Promise.all([load(), refreshMerchantState()]);
      setShowCancel(false);
      setReason("");
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "Couldn't cancel", "urgent");
    } finally {
      setBusy(false);
    }
  }

  const s = reservation ? reservationStatus(reservation.status) : null;
  const isActive = reservation?.status === "ACTIVE";

  return (
    <View style={styles.flex}>
      <AppBar title="Pickup" onBack={() => nav.goBack()} />
      <ScrollView contentContainerStyle={[styles.content, contentWidth.column]} showsVerticalScrollIndicator={false}>
        {!reservation ? (
          <ActivityIndicator style={styles.loader} color={color.brand[500]} />
        ) : (
          <>
            <Card elevated>
              {s ? <StatusPill status={s.status} label={s.label} /> : null}
              <Text variant="body" color={color.neutral.inkMuted} style={styles.explain}>
                {reservation.status === "PENDING_NOTIFICATION"
                  ? "A customer just reserved this. It becomes collectable in a moment."
                  : isActive
                  ? "Hold these items. Ask the customer for their 6-digit code when they arrive."
                  : reservation.status === "COMPLETED"
                  ? "Handed over. Nice work!"
                  : reservation.status === "EXPIRED"
                  ? "The customer didn't collect in time. Items are released."
                  : "This reservation was cancelled."}
              </Text>
            </Card>

            {isActive ? (
              <Card elevated>
                <Text variant="h3">Enter the customer's code</Text>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  They'll show it on their phone.
                </Text>
                <View style={styles.otpWrap}>
                  <OtpInput value={otp} onChange={setOtp} />
                </View>
                {error ? (
                  <Text variant="bodySm" color={color.status.urgent.strong}>
                    {error}
                  </Text>
                ) : null}
                <Button
                  label="Complete handover"
                  onPress={complete}
                  loading={busy}
                  disabled={otp.length !== 6}
                />
              </Card>
            ) : null}

            {isActive && !showCancel ? (
              <Button
                label="Can't fulfil this order"
                variant="secondary"
                onPress={() => setShowCancel(true)}
              />
            ) : null}

            {isActive && showCancel ? (
              <Card elevated>
                <Text variant="h3">Tell the customer why</Text>
                <Text variant="bodySm" color={color.neutral.inkMuted}>
                  Cancelling lowers your Bharosa Score, and you can't cancel in the last 3 minutes.
                </Text>
                <TextField
                  placeholder="e.g. Sold out before you arrived"
                  value={reason}
                  onChangeText={setReason}
                  multiline
                  style={styles.reasonInput}
                />
                <Button
                  label="Confirm cancellation"
                  variant="danger"
                  onPress={cancel}
                  loading={busy}
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
  explain: { marginTop: spacing.xxs },
  otpWrap: { marginVertical: spacing.sm },
  reasonInput: {
    height: 80,
    paddingTop: spacing.sm,
    textAlignVertical: "top",
  },
  loader: { marginTop: spacing.xxl },
});
