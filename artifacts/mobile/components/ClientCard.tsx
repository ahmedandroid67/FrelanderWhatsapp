import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { router } from "expo-router";
import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import type { Client, PaymentStatus } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";
import { StatusBadge } from "./StatusBadge";

interface ClientCardProps {
  client: Client;
  paymentStatus?: PaymentStatus;
}

export function ClientCard({ client, paymentStatus }: ClientCardProps) {
  const colors = useColors();
  const initials = client.name
    .split(" ")
    .map((w) => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  const handlePress = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    router.push(`/client/${client.id}`);
  };

  return (
    <Pressable
      onPress={handlePress}
      style={({ pressed }) => [
        styles.card,
        {
          backgroundColor: colors.card,
          borderColor: colors.border,
        },
        pressed && styles.pressed,
      ]}
    >
      <View
        style={[
          styles.avatar,
          { backgroundColor: colors.primary + "18" },
        ]}
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
          style={[styles.service, { color: colors.mutedForeground }]}
          numberOfLines={1}
        >
          {client.serviceType || "No service type"}
        </Text>
        <View style={styles.badges}>
          <StatusBadge status={client.status} size="sm" />
          {paymentStatus && paymentStatus !== "paid" && (
            <StatusBadge status={paymentStatus} size="sm" />
          )}
        </View>
      </View>
      <Feather name="chevron-right" size={18} color={colors.mutedForeground} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    alignItems: "center",
    padding: 14,
    borderRadius: 14,
    borderWidth: 1,
    marginBottom: 10,
    gap: 12,
  },
  pressed: { opacity: 0.7 },
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
  service: { fontSize: 13 },
  badges: { flexDirection: "row", gap: 6, marginTop: 2 },
});
