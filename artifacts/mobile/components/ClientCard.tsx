import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { router } from "expo-router";
import React, { useMemo, useState } from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import type { Client, PaymentStatus } from "@/context/DataContext";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";
import { MessageTemplateSheet } from "./MessageTemplateSheet";
import { QuickBookingModal } from "./QuickBookingModal";
import { StatusBadge } from "./StatusBadge";

interface ClientCardProps {
  client: Client;
  paymentStatus?: PaymentStatus;
}

export function ClientCard({ client, paymentStatus }: ClientCardProps) {
  const colors = useColors();
  const {
    getPaymentForClient,
    getBookingsForClient,
    updatePayment,
  } = useData();

  const [showBooking, setShowBooking] = useState(false);
  const [showTemplates, setShowTemplates] = useState(false);

  const payment = getPaymentForClient(client.id);
  const bookings = getBookingsForClient(client.id);

  const nextBooking = useMemo(() => {
    const today = new Date().toISOString().split("T")[0];
    return [...bookings]
      .filter((b) => b.date >= today)
      .sort((a, b) => a.date.localeCompare(b.date))[0];
  }, [bookings]);

  const balance =
    payment && payment.totalAmount > payment.paidAmount
      ? `$${(payment.totalAmount - payment.paidAmount).toFixed(2)}`
      : undefined;

  const today = new Date().toISOString().split("T")[0];
  const isOverdue =
    payment &&
    payment.status !== "paid" &&
    payment.dueDate &&
    payment.dueDate < today;

  const initials = client.name
    .split(" ")
    .map((w) => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  const handleMarkPaid = async () => {
    if (paymentStatus === "paid") return;
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    if (payment) {
      await updatePayment(payment.id, { paidAmount: payment.totalAmount });
    } else {
      router.push(`/payment/${client.id}`);
    }
  };

  const isPaid = paymentStatus === "paid";

  return (
    <>
      {/* Card top — tapping this navigates to client detail */}
      <Pressable
        onPress={() => {
          Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
          router.push(`/client/${client.id}`);
        }}
        style={({ pressed }) => [
          styles.card,
          { backgroundColor: colors.card, borderColor: colors.border },
          pressed && styles.pressed,
        ]}
      >
        {/* Top row */}
        <View style={styles.top}>
          <View
            style={[styles.avatar, { backgroundColor: colors.primary + "18" }]}
          >
            <Text style={[styles.initials, { color: colors.primary }]}>
              {initials}
            </Text>
          </View>
          <View style={styles.info}>
            <Text
              style={[styles.name, { color: colors.foreground }]}
              numberOfLines={1}
            >
              {client.name}
            </Text>
            <Text
              style={[styles.sub, { color: colors.mutedForeground }]}
              numberOfLines={1}
            >
              {client.serviceType || client.phone}
            </Text>
            <View style={styles.badges}>
              <StatusBadge status={client.status} size="sm" />
              {paymentStatus && !isPaid && (
                <StatusBadge status={paymentStatus} size="sm" />
              )}
            </View>
          </View>
          <Feather
            name="chevron-right"
            size={16}
            color={colors.mutedForeground}
          />
        </View>
      </Pressable>

      {/* Action row — separate Pressable zone, does NOT trigger card navigation */}
      <View
        style={[
          styles.actions,
          {
            backgroundColor: colors.card,
            borderColor: colors.border,
            borderTopColor: colors.border,
          },
        ]}
      >
        <Pressable
          onPress={() => {
            Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
            setShowTemplates(true);
          }}
          style={({ pressed }) => [
            styles.actionBtn,
            { backgroundColor: "#25D36612" },
            pressed && styles.actionPressed,
          ]}
        >
          <Feather name="message-circle" size={16} color="#25D366" />
          <Text style={[styles.actionLabel, { color: "#25D366" }]}>
            Message
          </Text>
        </Pressable>

        <View style={[styles.divider, { backgroundColor: colors.border }]} />

        <Pressable
          onPress={() => {
            Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
            setShowBooking(true);
          }}
          style={({ pressed }) => [
            styles.actionBtn,
            { backgroundColor: colors.primary + "10" },
            pressed && styles.actionPressed,
          ]}
        >
          <Feather name="calendar" size={16} color={colors.primary} />
          <Text style={[styles.actionLabel, { color: colors.primary }]}>
            Book
          </Text>
        </Pressable>

        <View style={[styles.divider, { backgroundColor: colors.border }]} />

        <Pressable
          onPress={handleMarkPaid}
          style={({ pressed }) => [
            styles.actionBtn,
            {
              backgroundColor: isPaid ? "#10B98110" : "#F59E0B12",
            },
            isPaid && styles.disabledAction,
            pressed && !isPaid && styles.actionPressed,
          ]}
        >
          <Feather
            name={isPaid ? "check-circle" : "dollar-sign"}
            size={16}
            color={isPaid ? "#10B981" : "#F59E0B"}
          />
          <Text
            style={[
              styles.actionLabel,
              { color: isPaid ? "#10B981" : "#F59E0B" },
            ]}
          >
            {isPaid ? "Paid ✓" : "Mark Paid"}
          </Text>
        </Pressable>
      </View>

      <QuickBookingModal
        visible={showBooking}
        clientId={client.id}
        clientName={client.name}
        onClose={() => setShowBooking(false)}
      />

      <MessageTemplateSheet
        visible={showTemplates}
        onClose={() => setShowTemplates(false)}
        vars={{
          phone: client.phone,
          name: client.name,
          amount: balance,
          date: nextBooking?.date,
          service: client.serviceType || undefined,
        }}
        suggestPayment={!!isOverdue}
        suggestBooking={!!nextBooking}
      />
    </>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    borderWidth: 1,
    marginBottom: 2,
    overflow: "hidden",
  },
  pressed: { opacity: 0.75 },
  top: {
    flexDirection: "row",
    alignItems: "center",
    padding: 14,
    gap: 12,
  },
  avatar: {
    width: 46,
    height: 46,
    borderRadius: 23,
    alignItems: "center",
    justifyContent: "center",
  },
  initials: { fontSize: 16, fontWeight: "700" },
  info: { flex: 1, gap: 3 },
  name: { fontSize: 15, fontWeight: "600" },
  sub: { fontSize: 13 },
  badges: { flexDirection: "row", gap: 6, marginTop: 2 },
  actions: {
    flexDirection: "row",
    borderTopWidth: 1,
    borderWidth: 1,
    borderRadius: 16,
    marginBottom: 10,
    overflow: "hidden",
  },
  actionBtn: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 11,
    gap: 5,
  },
  actionLabel: { fontSize: 12, fontWeight: "700" },
  actionPressed: { opacity: 0.6 },
  disabledAction: { opacity: 0.7 },
  divider: { width: 1 },
});
