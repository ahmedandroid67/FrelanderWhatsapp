import { Feather } from "@expo/vector-icons";
import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import type { Booking, Client } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

import { useTranslation } from "react-i18next";

interface BookingCardProps {
  booking: Booking;
  client?: Client;
  onPress?: () => void;
}

function formatDate(dateStr: string): string {
  if (!dateStr) return "";
  const [year, month, day] = dateStr.split("-");
  return `${day.padStart(2, "0")}/${month.padStart(2, "0")}/${year}`;
}

export function BookingCard({ booking, client, onPress }: BookingCardProps) {
  const { t } = useTranslation();
  const colors = useColors();
  const today = new Date().toISOString().split("T")[0];
  const isToday = booking.date === today;

  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        styles.card,
        {
          backgroundColor: colors.card,
          borderColor: isToday ? colors.primary : colors.border,
        },
        pressed && styles.pressed,
      ]}
    >
      <View
        style={[
          styles.dateBox,
          { backgroundColor: isToday ? colors.primary : colors.muted },
        ]}
      >
        <Feather
          name="calendar"
          size={14}
          color={isToday ? "#fff" : colors.mutedForeground}
        />
        <Text
          style={[
            styles.dateText,
            { color: isToday ? "#fff" : colors.mutedForeground },
          ]}
        >
          {isToday ? t("today") : formatDate(booking.date)}
        </Text>
      </View>
      <View style={styles.info}>
        {client && (
          <Text
            style={[styles.clientName, { color: colors.foreground }]}
            numberOfLines={1}
          >
            {client.name}
          </Text>
        )}
        <View style={styles.row}>
          <Feather name="clock" size={12} color={colors.mutedForeground} />
          <Text style={[styles.meta, { color: colors.mutedForeground }]}>
            {booking.time || t("unscheduled")}
          </Text>
        </View>
        {booking.location ? (
          <View style={styles.row}>
            <Feather name="map-pin" size={12} color={colors.mutedForeground} />
            <Text
              style={[styles.meta, { color: colors.mutedForeground }]}
              numberOfLines={1}
            >
              {booking.location}
            </Text>
          </View>
        ) : null}
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: "row",
    alignItems: "flex-start",
    padding: 14,
    borderRadius: 14,
    borderWidth: 1,
    marginBottom: 10,
    gap: 12,
  },
  pressed: { opacity: 0.7 },
  dateBox: {
    flexDirection: "row",
    alignItems: "center",
    gap: 5,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
  },
  dateText: { fontSize: 12, fontWeight: "600" },
  info: { flex: 1, gap: 4 },
  clientName: { fontSize: 15, fontWeight: "600" },
  row: { flexDirection: "row", alignItems: "center", gap: 4 },
  meta: { fontSize: 13 },
});
