import { Feather } from "@expo/vector-icons";
import { router } from "expo-router";
import React, { useMemo, useState } from "react";
import {
  Platform,
  Pressable,
  RefreshControl,
  SectionList,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useTranslation } from "react-i18next";
import { BookingCard } from "@/components/BookingCard";
import { CalendarView } from "@/components/CalendarView";
import { EmptyState } from "@/components/EmptyState";
import type { Booking } from "@/context/DataContext";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

function formatDate(dateStr: string): string {
  const [year, month, day] = dateStr.split("-");
  return `${day.padStart(2, "0")}/${month.padStart(2, "0")}/${year}`;
}

function groupByDate(
  bookings: Booking[],
  today: string,
  t: any
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
          ? t("unscheduled")
          : date === today
          ? t("today")
          : formatDate(date),
      data,
    }));
}

type ViewMode = "list" | "calendar";

export default function BookingsScreen() {
  const { t } = useTranslation();
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { bookings, getClientById } = useData();

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;
  const today = new Date().toISOString().split("T")[0];

  const [viewMode, setViewMode] = useState<ViewMode>("list");
  const [search, setSearch] = useState("");
  const [isRefreshing, setIsRefreshing] = useState(false);

  const onRefresh = () => {
    setIsRefreshing(true);
    setTimeout(() => setIsRefreshing(false), 800);
  };

  // Filter bookings by client name or date
  const filteredBookings = useMemo(() => {
    const q = search.toLowerCase().trim();
    if (!q) return bookings;
    return bookings.filter((b) => {
      const client = getClientById(b.clientId);
      const clientMatch = client?.name.toLowerCase().includes(q) ?? false;
      const dateMatch = b.date.includes(q);
      const locationMatch = b.location.toLowerCase().includes(q);
      return clientMatch || dateMatch || locationMatch;
    });
  }, [bookings, search, getClientById]);

  const sections = useMemo(
    () => groupByDate(filteredBookings, today, t),
    [filteredBookings, today, t]
  );

  const handleBookingPress = (booking: Booking) => {
    router.push(`/client/${booking.clientId}`);
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Header: search + view toggle */}
      <View
        style={[
          styles.header,
          { paddingTop: topPad + 12, borderBottomColor: colors.border },
        ]}
      >
        {/* Search bar */}
        <View style={[styles.searchBar, { backgroundColor: colors.muted, borderColor: colors.border }]}>
          <Feather name="search" size={16} color={colors.mutedForeground} />
          <TextInput
            style={[styles.searchInput, { color: colors.foreground }]}
            placeholder={t("searchBookings")}
            placeholderTextColor={colors.mutedForeground}
            value={search}
            onChangeText={setSearch}
          />
          {search.length > 0 && (
            <Pressable onPress={() => setSearch("")}>
              <Feather name="x" size={16} color={colors.mutedForeground} />
            </Pressable>
          )}
        </View>

        {/* List / Calendar toggle */}
        <View style={[styles.toggle, { backgroundColor: colors.muted, borderColor: colors.border }]}>
          <Pressable
            onPress={() => setViewMode("list")}
            style={[
              styles.toggleBtn,
              viewMode === "list" && { backgroundColor: colors.primary },
            ]}
          >
            <Feather
              name="list"
              size={16}
              color={viewMode === "list" ? "#fff" : colors.mutedForeground}
            />
          </Pressable>
          <Pressable
            onPress={() => setViewMode("calendar")}
            style={[
              styles.toggleBtn,
              viewMode === "calendar" && { backgroundColor: colors.primary },
            ]}
          >
            <Feather
              name="calendar"
              size={16}
              color={viewMode === "calendar" ? "#fff" : colors.mutedForeground}
            />
          </Pressable>
        </View>
      </View>

      {/* Calendar view */}
      {viewMode === "calendar" ? (
        <CalendarView
          bookings={filteredBookings}
          getClientById={getClientById}
          onBookingPress={handleBookingPress}
        />
      ) : (
        /* List view */
        bookings.length === 0 ? (
          <View style={[styles.center, { paddingTop: topPad + 60 }]}>
            <EmptyState
              icon="calendar"
              title={t("noBookingsYet")}
              subtitle={t("addBookingFromProfile")}
            />
          </View>
        ) : filteredBookings.length === 0 ? (
          <View style={[styles.center, { paddingTop: 40 }]}>
            <EmptyState
              icon="search"
              title={t("noResults")}
              subtitle={t("noBookingsMatching", { query: search })}
            />
          </View>
        ) : (
          <SectionList
            sections={sections}
            keyExtractor={(item) => item.id}
            contentContainerStyle={[
              styles.content,
              {
                paddingBottom: insets.bottom + botPad + 100,
                paddingTop: 16,
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
            refreshControl={
              <RefreshControl
                refreshing={isRefreshing}
                onRefresh={onRefresh}
                tintColor={colors.primary}
                colors={[colors.primary]}
              />
            }
          />
        )
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    borderBottomWidth: 1,
    paddingHorizontal: 16,
    paddingBottom: 12,
  },
  searchBar: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    borderRadius: 12,
    borderWidth: 1,
    paddingHorizontal: 12,
    paddingVertical: 10,
    gap: 8,
  },
  searchInput: { flex: 1, fontSize: 15 },
  toggle: {
    flexDirection: "row",
    borderRadius: 10,
    borderWidth: 1,
    overflow: "hidden",
  },
  toggleBtn: {
    padding: 9,
    alignItems: "center",
    justifyContent: "center",
  },
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
