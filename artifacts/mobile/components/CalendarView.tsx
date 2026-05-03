import { Feather } from "@expo/vector-icons";
import React, { useMemo, useState } from "react";
import {
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import type { Booking, Client } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

const DAYS_OF_WEEK = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
const MONTHS_SHORT = [
  "Jan","Feb","Mar","Apr","May","Jun",
  "Jul","Aug","Sep","Oct","Nov","Dec",
];
const MONTHS_LONG = [
  "January","February","March","April","May","June",
  "July","August","September","October","November","December",
];

function formatDate(dateStr: string): string {
  if (!dateStr) return "";
  const [year, month, day] = dateStr.split("-");
  return `${day.padStart(2, "0")}/${month.padStart(2, "0")}/${year}`;
}

interface CalendarViewProps {
  bookings: Booking[];
  getClientById: (id: string) => Client | undefined;
  onBookingPress: (booking: Booking) => void;
}

export function CalendarView({ bookings, getClientById, onBookingPress }: CalendarViewProps) {
  const colors = useColors();
  const today = new Date();
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2,"0")}-${String(today.getDate()).padStart(2,"0")}`;

  const [viewMonth, setViewMonth] = useState(
    new Date(today.getFullYear(), today.getMonth(), 1)
  );
  const [selectedDay, setSelectedDay] = useState<string | null>(todayStr);

  const prevMonth = () =>
    setViewMonth((v) => new Date(v.getFullYear(), v.getMonth() - 1, 1));
  const nextMonth = () =>
    setViewMonth((v) => new Date(v.getFullYear(), v.getMonth() + 1, 1));

  // Map of dateStr -> bookings[]
  const bookingsByDate = useMemo(() => {
    const map: Record<string, Booking[]> = {};
    bookings.forEach((b) => {
      if (!b.date) return;
      if (!map[b.date]) map[b.date] = [];
      map[b.date].push(b);
    });
    return map;
  }, [bookings]);

  // Build day grid
  const firstDayOfWeek = new Date(viewMonth.getFullYear(), viewMonth.getMonth(), 1).getDay();
  const daysInMonth = new Date(viewMonth.getFullYear(), viewMonth.getMonth() + 1, 0).getDate();
  const cells: (number | null)[] = [
    ...Array(firstDayOfWeek).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];
  while (cells.length % 7 !== 0) cells.push(null);

  const selectedDayBookings = selectedDay ? (bookingsByDate[selectedDay] ?? []) : [];

  return (
    <View style={styles.container}>
      {/* Month nav */}
      <View style={[styles.navRow, { borderBottomColor: colors.border }]}>
        <Pressable onPress={prevMonth} hitSlop={12} style={styles.navBtn}>
          <Feather name="chevron-left" size={22} color={colors.foreground} />
        </Pressable>
        <Text style={[styles.monthLabel, { color: colors.foreground }]}>
          {MONTHS_LONG[viewMonth.getMonth()]} {viewMonth.getFullYear()}
        </Text>
        <Pressable onPress={nextMonth} hitSlop={12} style={styles.navBtn}>
          <Feather name="chevron-right" size={22} color={colors.foreground} />
        </Pressable>
      </View>

      {/* Weekday headers */}
      <View style={styles.weekRow}>
        {DAYS_OF_WEEK.map((d) => (
          <Text key={d} style={[styles.weekDay, { color: colors.mutedForeground }]}>
            {d}
          </Text>
        ))}
      </View>

      {/* Day grid */}
      <View style={styles.grid}>
        {cells.map((day, i) => {
          if (!day) return <View key={`e-${i}`} style={styles.dayCell} />;

          const y = viewMonth.getFullYear();
          const mo = String(viewMonth.getMonth() + 1).padStart(2, "0");
          const da = String(day).padStart(2, "0");
          const dateStr = `${y}-${mo}-${da}`;

          const isSelected = selectedDay === dateStr;
          const isToday = dateStr === todayStr;
          const hasBookings = !!(bookingsByDate[dateStr]?.length);
          const bookingCount = bookingsByDate[dateStr]?.length ?? 0;

          return (
            <Pressable
              key={dateStr}
              onPress={() => setSelectedDay(isSelected ? null : dateStr)}
              style={[
                styles.dayCell,
                isSelected && { backgroundColor: colors.primary, borderRadius: CELL_SIZE / 2 },
                !isSelected && isToday && {
                  borderWidth: 1.5,
                  borderColor: colors.primary,
                  borderRadius: CELL_SIZE / 2,
                },
              ]}
            >
              <Text
                style={[
                  styles.dayNum,
                  {
                    color: isSelected ? "#fff" : isToday ? colors.primary : colors.foreground,
                    fontWeight: isSelected || isToday ? "700" : "400",
                  },
                ]}
              >
                {day}
              </Text>
              {/* Booking dots */}
              {hasBookings && (
                <View style={styles.dotsRow}>
                  {Array.from({ length: Math.min(bookingCount, 3) }).map((_, di) => (
                    <View
                      key={di}
                      style={[
                        styles.dot,
                        { backgroundColor: isSelected ? "#fff" : colors.primary },
                      ]}
                    />
                  ))}
                </View>
              )}
            </Pressable>
          );
        })}
      </View>

      {/* Selected day bookings */}
      {selectedDay && (
        <View style={[styles.dayDetail, { borderTopColor: colors.border }]}>
          <Text style={[styles.dayDetailTitle, { color: colors.foreground }]}>
            {selectedDay === todayStr ? "Today" : formatDate(selectedDay)}
            {selectedDayBookings.length > 0
              ? ` · ${selectedDayBookings.length} booking${selectedDayBookings.length > 1 ? "s" : ""}`
              : ""}
          </Text>

          <ScrollView
            showsVerticalScrollIndicator={false}
            style={styles.dayScroll}
          >
            {selectedDayBookings.length === 0 ? (
              <View style={[styles.emptyDay, { backgroundColor: colors.muted, borderColor: colors.border }]}>
                <Feather name="calendar" size={16} color={colors.mutedForeground} />
                <Text style={[styles.emptyDayText, { color: colors.mutedForeground }]}>
                  No bookings this day
                </Text>
              </View>
            ) : (
              selectedDayBookings.map((b) => {
                const client = getClientById(b.clientId);
                return (
                  <Pressable
                    key={b.id}
                    onPress={() => onBookingPress(b)}
                    style={({ pressed }) => [
                      styles.bookingRow,
                      { backgroundColor: colors.card, borderColor: colors.border },
                      pressed && { opacity: 0.7 },
                    ]}
                  >
                    <View style={[styles.bookingAccent, { backgroundColor: colors.primary }]} />
                    <View style={styles.bookingInfo}>
                      <Text style={[styles.bookingClient, { color: colors.foreground }]}>
                        {client?.name ?? "Unknown Client"}
                      </Text>
                      <View style={styles.bookingMeta}>
                        {b.time ? (
                          <View style={styles.metaItem}>
                            <Feather name="clock" size={11} color={colors.mutedForeground} />
                            <Text style={[styles.metaText, { color: colors.mutedForeground }]}>
                              {b.time}
                            </Text>
                          </View>
                        ) : null}
                        {b.location ? (
                          <View style={styles.metaItem}>
                            <Feather name="map-pin" size={11} color={colors.mutedForeground} />
                            <Text style={[styles.metaText, { color: colors.mutedForeground }]} numberOfLines={1}>
                              {b.location}
                            </Text>
                          </View>
                        ) : null}
                      </View>
                    </View>
                    <Feather name="chevron-right" size={14} color={colors.mutedForeground} />
                  </Pressable>
                );
              })
            )}
          </ScrollView>
        </View>
      )}
    </View>
  );
}

const CELL_SIZE = 40;

const styles = StyleSheet.create({
  container: { flex: 1 },
  navRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 16,
    paddingVertical: 14,
    borderBottomWidth: 1,
  },
  navBtn: { padding: 4 },
  monthLabel: { fontSize: 17, fontWeight: "700" },
  weekRow: {
    flexDirection: "row",
    justifyContent: "space-around",
    paddingHorizontal: 8,
    paddingVertical: 8,
  },
  weekDay: {
    width: CELL_SIZE,
    textAlign: "center",
    fontSize: 11,
    fontWeight: "600",
  },
  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    justifyContent: "space-around",
    paddingHorizontal: 8,
    rowGap: 4,
    paddingBottom: 8,
  },
  dayCell: {
    width: CELL_SIZE,
    height: CELL_SIZE,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 2,
  },
  dayNum: { fontSize: 14 },
  dotsRow: {
    flexDirection: "row",
    gap: 2,
    position: "absolute",
    bottom: 3,
    alignSelf: "center",
  },
  dot: {
    width: 4,
    height: 4,
    borderRadius: 2,
  },
  dayDetail: {
    flex: 1,
    borderTopWidth: 1,
    paddingHorizontal: 16,
    paddingTop: 12,
  },
  dayDetailTitle: {
    fontSize: 13,
    fontWeight: "700",
    letterSpacing: 0.3,
    marginBottom: 10,
  },
  dayScroll: { flex: 1 },
  emptyDay: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    paddingVertical: 16,
    paddingHorizontal: 14,
    borderRadius: 12,
    borderWidth: 1,
  },
  emptyDayText: { fontSize: 14 },
  bookingRow: {
    flexDirection: "row",
    alignItems: "center",
    borderRadius: 12,
    borderWidth: 1,
    marginBottom: 8,
    overflow: "hidden",
    gap: 12,
  },
  bookingAccent: {
    width: 4,
    alignSelf: "stretch",
  },
  bookingInfo: { flex: 1, paddingVertical: 12, gap: 4 },
  bookingClient: { fontSize: 15, fontWeight: "600" },
  bookingMeta: { flexDirection: "row", gap: 12 },
  metaItem: { flexDirection: "row", alignItems: "center", gap: 3 },
  metaText: { fontSize: 12 },
});
