import { Feather } from "@expo/vector-icons";
import { router } from "expo-router";
import React, { useMemo } from "react";
import {
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { BookingCard } from "@/components/BookingCard";
import { ClientCard } from "@/components/ClientCard";
import { EmptyState } from "@/components/EmptyState";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

export default function DashboardScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { clients, bookings, payments, getClientById } = useData();

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;

  const now = new Date();
  const today = now.toISOString().split("T")[0];

  const todayBookings = useMemo(
    () => bookings.filter((b) => b.date === today),
    [bookings, today]
  );

  const overduePayments = useMemo(
    () =>
      payments.filter(
        (p) => p.status !== "paid" && p.dueDate && p.dueDate < today
      ),
    [payments, today]
  );

  const activeClients = useMemo(
    () => clients.filter((c) => ["Lead", "Quoted", "Booked"].includes(c.status)),
    [clients]
  );

  const greeting = (() => {
    const h = now.getHours();
    if (h < 12) return "Good morning";
    if (h < 17) return "Good afternoon";
    return "Good evening";
  })();

  const dateStr = now.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });

  const stats = [
    {
      label: "Clients",
      value: clients.length,
      icon: "users" as const,
      color: colors.primary,
    },
    {
      label: "Bookings",
      value: bookings.length,
      icon: "calendar" as const,
      color: "#8B5CF6",
    },
    {
      label: "Overdue",
      value: overduePayments.length,
      icon: "alert-circle" as const,
      color: overduePayments.length > 0 ? "#EF4444" : "#10B981",
    },
  ];

  return (
    <ScrollView
      style={[styles.container, { backgroundColor: colors.background }]}
      contentContainerStyle={[
        styles.content,
        { paddingBottom: insets.bottom + botPad + 100, paddingTop: topPad + 16 },
      ]}
      showsVerticalScrollIndicator={false}
    >
      {/* Header */}
      <View style={styles.headerRow}>
        <View>
          <Text style={[styles.greeting, { color: colors.mutedForeground }]}>
            {greeting}
          </Text>
          <Text style={[styles.dateStr, { color: colors.foreground }]}>
            {dateStr}
          </Text>
        </View>
        <Pressable
          onPress={() => router.push("/client/form")}
          style={({ pressed }) => [
            styles.addBtn,
            { backgroundColor: colors.primary },
            pressed && { opacity: 0.85 },
          ]}
        >
          <Feather name="plus" size={20} color="#fff" />
        </Pressable>
      </View>

      {/* Stats */}
      <View style={styles.statsRow}>
        {stats.map((s) => (
          <View
            key={s.label}
            style={[
              styles.statCard,
              { backgroundColor: colors.card, borderColor: colors.border },
            ]}
          >
            <View
              style={[
                styles.statIcon,
                { backgroundColor: s.color + "18" },
              ]}
            >
              <Feather name={s.icon} size={16} color={s.color} />
            </View>
            <Text style={[styles.statValue, { color: colors.foreground }]}>
              {s.value}
            </Text>
            <Text style={[styles.statLabel, { color: colors.mutedForeground }]}>
              {s.label}
            </Text>
          </View>
        ))}
      </View>

      {/* Today's Bookings */}
      <View style={styles.section}>
        <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
          Today's Bookings
        </Text>
        {todayBookings.length === 0 ? (
          <View
            style={[
              styles.emptyCard,
              { backgroundColor: colors.card, borderColor: colors.border },
            ]}
          >
            <Feather name="sun" size={18} color={colors.mutedForeground} />
            <Text style={[styles.emptyText, { color: colors.mutedForeground }]}>
              No bookings today
            </Text>
          </View>
        ) : (
          todayBookings.map((b) => (
            <BookingCard
              key={b.id}
              booking={b}
              client={getClientById(b.clientId)}
              onPress={() => router.push(`/client/${b.clientId}`)}
            />
          ))
        )}
      </View>

      {/* Overdue Payments */}
      {overduePayments.length > 0 && (
        <View style={styles.section}>
          <View style={styles.sectionRow}>
            <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
              Overdue Payments
            </Text>
            <View style={styles.alertBadge}>
              <Text style={styles.alertBadgeText}>{overduePayments.length}</Text>
            </View>
          </View>
          {overduePayments.map((p) => {
            const client = getClientById(p.clientId);
            if (!client) return null;
            const daysOver = Math.max(
              0,
              Math.floor(
                (now.getTime() - new Date(p.dueDate).getTime()) / 86400000
              )
            );
            return (
              <Pressable
                key={p.id}
                onPress={() => router.push(`/client/${client.id}`)}
                style={({ pressed }) => [
                  styles.overdueCard,
                  pressed && { opacity: 0.75 },
                ]}
              >
                <View>
                  <Text style={styles.overdueName}>{client.name}</Text>
                  <Text style={styles.overdueAmount}>
                    ${(p.totalAmount - p.paidAmount).toFixed(2)} due
                  </Text>
                </View>
                <View style={styles.overdueRight}>
                  <Text style={styles.overdueDays}>{daysOver}d overdue</Text>
                  <Feather name="chevron-right" size={16} color="#EF4444" />
                </View>
              </Pressable>
            );
          })}
        </View>
      )}

      {/* Active Pipeline */}
      <View style={styles.section}>
        <View style={styles.sectionRow}>
          <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
            Active Pipeline
          </Text>
          <Pressable onPress={() => router.push("/(tabs)/clients")}>
            <Text style={[styles.seeAll, { color: colors.primary }]}>
              See all
            </Text>
          </Pressable>
        </View>
        {activeClients.length === 0 ? (
          <EmptyState
            icon="users"
            title="No active clients"
            subtitle="Add your first client to get started"
          />
        ) : (
          activeClients
            .slice(0, 5)
            .map((c) => <ClientCard key={c.id} client={c} />)
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 16, gap: 22 },
  headerRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  greeting: { fontSize: 13, fontWeight: "500" },
  dateStr: { fontSize: 20, fontWeight: "700", marginTop: 2 },
  addBtn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#2563EB",
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 4,
  },
  statsRow: { flexDirection: "row", gap: 10 },
  statCard: {
    flex: 1,
    borderRadius: 14,
    borderWidth: 1,
    padding: 14,
    gap: 6,
    alignItems: "flex-start",
  },
  statIcon: {
    width: 34,
    height: 34,
    borderRadius: 10,
    alignItems: "center",
    justifyContent: "center",
  },
  statValue: { fontSize: 22, fontWeight: "800", lineHeight: 26 },
  statLabel: { fontSize: 11, fontWeight: "500" },
  section: { gap: 10 },
  sectionTitle: { fontSize: 16, fontWeight: "700" },
  sectionRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  seeAll: { fontSize: 13, fontWeight: "600" },
  emptyCard: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 8,
    borderRadius: 14,
    borderWidth: 1,
    padding: 18,
  },
  emptyText: { fontSize: 14 },
  alertBadge: {
    backgroundColor: "#EF4444",
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 10,
  },
  alertBadgeText: { color: "#fff", fontSize: 11, fontWeight: "700" },
  overdueCard: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    padding: 14,
    borderRadius: 14,
    backgroundColor: "#FEF2F2",
    borderWidth: 1,
    borderColor: "#FECACA",
  },
  overdueName: { fontSize: 15, fontWeight: "600", color: "#0F172A" },
  overdueAmount: { fontSize: 13, color: "#EF4444", fontWeight: "500" },
  overdueRight: { flexDirection: "row", alignItems: "center", gap: 4 },
  overdueDays: { fontSize: 12, fontWeight: "600", color: "#EF4444" },
});
