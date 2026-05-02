import { router } from "expo-router";
import React, { useMemo } from "react";
import { Platform, SectionList, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { BookingCard } from "@/components/BookingCard";
import { EmptyState } from "@/components/EmptyState";
import type { Booking } from "@/context/DataContext";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

function formatDate(dateStr: string): string {
  const [year, month, day] = dateStr.split("-");
  const months = [
    "Jan","Feb","Mar","Apr","May","Jun",
    "Jul","Aug","Sep","Oct","Nov","Dec",
  ];
  return `${parseInt(day)} ${months[parseInt(month) - 1]}, ${year}`;
}

function groupByDate(
  bookings: Booking[],
  today: string
): { title: string; data: Booking[] }[] {
  const groups: Record<string, Booking[]> = {};
  bookings.forEach((b) => {
    const key = b.date || "no-date";
    if (!groups[key]) groups[key] = [];
    groups[key].push(b);
  });
  return Object.entries(groups)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([date, data]) => ({
      title:
        date === "no-date"
          ? "Unscheduled"
          : date === today
          ? "Today"
          : formatDate(date),
      data,
    }));
}

export default function BookingsScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { bookings, getClientById } = useData();

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;
  const today = new Date().toISOString().split("T")[0];

  const sections = useMemo(
    () => groupByDate(bookings, today),
    [bookings, today]
  );

  if (bookings.length === 0) {
    return (
      <View
        style={[
          styles.container,
          { backgroundColor: colors.background },
        ]}
      >
        <View style={[styles.center, { paddingTop: topPad + 60 }]}>
          <EmptyState
            icon="calendar"
            title="No bookings yet"
            subtitle="Add bookings from a client's profile"
          />
        </View>
      </View>
    );
  }

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <SectionList
        sections={sections}
        keyExtractor={(item) => item.id}
        contentContainerStyle={[
          styles.content,
          {
            paddingBottom: insets.bottom + botPad + 100,
            paddingTop: topPad + 16,
          },
        ]}
        renderSectionHeader={({ section }) => (
          <Text
            style={[
              styles.sectionHeader,
              {
                color: colors.mutedForeground,
                backgroundColor: colors.background,
              },
            ]}
          >
            {section.title}
          </Text>
        )}
        renderItem={({ item }) => {
          const client = getClientById(item.clientId);
          return (
            <BookingCard
              booking={item}
              client={client}
              onPress={() => router.push(`/client/${item.clientId}`)}
            />
          );
        }}
        showsVerticalScrollIndicator={false}
        stickySectionHeadersEnabled={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  center: { flex: 1, alignItems: "center" },
  content: { paddingHorizontal: 16 },
  sectionHeader: {
    fontSize: 12,
    fontWeight: "700",
    textTransform: "uppercase",
    letterSpacing: 0.8,
    marginBottom: 8,
    marginTop: 16,
  },
});
