import React from "react";
import { StyleSheet, Text, View } from "react-native";
import type { ClientStatus, PaymentStatus } from "@/context/DataContext";

interface StatusBadgeProps {
  status: ClientStatus | PaymentStatus;
  size?: "sm" | "md";
}

type ConfigEntry = { label: string; bg: string; text: string };

const CONFIG: Record<string, ConfigEntry> = {
  Lead: { label: "Lead", bg: "#F3E8FF", text: "#7C3AED" },
  Quoted: { label: "Quoted", bg: "#FEF3C7", text: "#D97706" },
  Booked: { label: "Booked", bg: "#DBEAFE", text: "#1D4ED8" },
  Completed: { label: "Completed", bg: "#D1FAE5", text: "#059669" },
  Paid: { label: "Paid", bg: "#F0FDF4", text: "#16A34A" },
  unpaid: { label: "Unpaid", bg: "#FEE2E2", text: "#DC2626" },
  partial: { label: "Partial", bg: "#FEF3C7", text: "#D97706" },
  paid: { label: "Paid", bg: "#D1FAE5", text: "#059669" },
};

export function StatusBadge({ status, size = "md" }: StatusBadgeProps) {
  const cfg = CONFIG[status] ?? { label: status, bg: "#F1F5F9", text: "#64748B" };
  return (
    <View
      style={[
        styles.badge,
        { backgroundColor: cfg.bg },
        size === "sm" && styles.sm,
      ]}
    >
      <Text
        style={[
          styles.text,
          { color: cfg.text },
          size === "sm" && styles.textSm,
        ]}
      >
        {cfg.label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 20,
    alignSelf: "flex-start",
  },
  sm: { paddingHorizontal: 8, paddingVertical: 2 },
  text: { fontSize: 13, fontWeight: "600" },
  textSm: { fontSize: 11 },
});
