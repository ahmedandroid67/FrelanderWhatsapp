import { Feather } from "@expo/vector-icons";
import { router } from "expo-router";
import React, { useMemo, useState } from "react";
import {
  Alert,
  Linking,
  Platform,
  Pressable,
  RefreshControl,
  ScrollView,
  Share,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useTranslation } from "react-i18next";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { BookingCard } from "@/components/BookingCard";
import { ClientCard } from "@/components/ClientCard";
import { EmptyState } from "@/components/EmptyState";
import { MessageTemplateSheet } from "@/components/MessageTemplateSheet";
import { QuickAddModal } from "@/components/QuickAddModal";
import { useData, MessageTemplates } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";
import { formatMessage } from "@/lib/messages";


export default function DashboardScreen() {
  const { t } = useTranslation();
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { clients, bookings, payments, getClientById, templates } = useData();
  const [showQuickAdd, setShowQuickAdd] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [selectedReminder, setSelectedReminder] = useState<{ phone: string; name: string; amount: string } | null>(null);

  const sendWhatsApp = async (phone: string, name: string, amount: string) => {
    setSelectedReminder({ phone, name, amount });
  };

  const sendAllReminders = async (overdueList: { phone: string; name: string; amount: string }[]) => {
    for (const item of overdueList) {
      await sendWhatsApp(item.phone, item.name, item.amount);
    }
  };

  const onRefresh = () => {
    setIsRefreshing(true);
    // Data is local AsyncStorage — just a brief visual refresh
    setTimeout(() => setIsRefreshing(false), 800);
  };

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

  const totalRevenue = useMemo(
    () => payments.reduce((sum, p) => sum + (p.paidAmount ?? 0), 0),
    [payments]
  );

  function fmtRevenue(n: number): string {
    if (n >= 1000) return `$${(n / 1000).toFixed(1)}k`;
    return `$${n.toFixed(0)}`;
  }

  const activeClients = useMemo(
    () =>
      clients.filter((c) =>
        ["Lead", "Quoted", "Booked"].includes(c.status)
      ),
    [clients]
  );

  const greeting = (() => {
    const h = now.getHours();
    if (h < 12) return t("goodMorning");
    if (h < 17) return t("goodAfternoon");
    return t("goodEvening");
  })();

  const dateStr = `${String(now.getDate()).padStart(2, '0')}/${String(now.getMonth() + 1).padStart(2, '0')}/${now.getFullYear()}`;

  const stats = [
    {
      label: t("clients"),
      value: clients.length.toString(),
      icon: "users" as const,
      color: colors.primary,
    },
    {
      label: t("bookings"),
      value: bookings.length.toString(),
      icon: "calendar" as const,
      color: "#8B5CF6",
    },
    {
      label: t("revenue"),
      value: fmtRevenue(totalRevenue),
      icon: "trending-up" as const,
      color: "#10B981",
    },
    {
      label: t("overdue"),
      value: overduePayments.length.toString(),
      icon: "alert-circle" as const,
      color: overduePayments.length > 0 ? "#EF4444" : "#10B981",
    },
  ];

  const handleExport = async () => {
    try {
      const data = {
        exportedAt: new Date().toISOString(),
        clients,
        bookings,
        payments,
      };
      const json = JSON.stringify(data, null, 2);
      await Share.share({
        message: json,
        title: "ClientFlow Export",
      });
    } catch (e: any) {
      Alert.alert(t("exportFailed"), e?.message ?? t("couldNotExport"));
    }
  };

  return (
    <>
      <ScrollView
        style={[styles.container, { backgroundColor: colors.background }]}
        contentContainerStyle={[
          styles.content,
          {
            paddingBottom: insets.bottom + botPad + 100,
            paddingTop: topPad + 16,
          },
        ]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={isRefreshing}
            onRefresh={onRefresh}
            tintColor={colors.primary}
            colors={[colors.primary]}
          />
        }
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
          <View style={styles.headerBtns}>
            <Pressable
              onPress={handleExport}
              style={({ pressed }) => [
                styles.iconBtn,
                { backgroundColor: colors.muted, borderColor: colors.border },
                pressed && { opacity: 0.7 },
              ]}
            >
              <Feather name="upload" size={18} color={colors.mutedForeground} />
            </Pressable>
            <Pressable
              onPress={() => router.push("/security")}
              style={({ pressed }) => [
                styles.iconBtn,
                { backgroundColor: colors.muted, borderColor: colors.border },
                pressed && { opacity: 0.7 },
              ]}
            >
              <Feather name="shield" size={18} color={colors.mutedForeground} />
            </Pressable>
            <Pressable
              onPress={() => setShowQuickAdd(true)}
              style={({ pressed }) => [
                styles.addBtn,
                { backgroundColor: colors.primary },
                pressed && { opacity: 0.85, transform: [{ scale: 0.93 }] },
              ]}
            >
              <Feather name="plus" size={22} color="#fff" />
            </Pressable>
          </View>
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
              <Text
                style={[styles.statLabel, { color: colors.mutedForeground }]}
              >
                {s.label}
              </Text>
            </View>
          ))}
        </View>

        {/* Today's Bookings */}
        <View style={styles.section}>
          <View style={styles.sectionRow}>
            <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
              {t("todaysBookings")}
            </Text>
            {todayBookings.length > 0 && (
              <View
                style={[
                  styles.countBadge,
                  { backgroundColor: colors.primary + "18" },
                ]}
              >
                <Text
                  style={[styles.countBadgeText, { color: colors.primary }]}
                >
                  {todayBookings.length}
                </Text>
              </View>
            )}
          </View>
          {todayBookings.length === 0 ? (
            <View
              style={[
                styles.emptyCard,
                { backgroundColor: colors.card, borderColor: colors.border },
              ]}
            >
              <Feather name="sun" size={18} color={colors.mutedForeground} />
              <Text
                style={[styles.emptyText, { color: colors.mutedForeground }]}
              >
                {t("noBookingsToday")}
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

        {/* Overdue Payments — action center */}
        {overduePayments.length > 0 && (
          <View style={styles.section}>
            <View style={styles.sectionRow}>
              <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
                {t("overduePayments")}
              </Text>
              <View style={styles.alertBadge}>
                <Text style={styles.alertBadgeText}>
                  {overduePayments.length}
                </Text>
              </View>
            </View>

            {/* "Remind all" if multiple */}
            {overduePayments.length > 1 && (
              <Pressable
                onPress={() => {
                  const list = overduePayments
                    .map((p) => {
                      const c = getClientById(p.clientId);
                      if (!c) return null;
                      return {
                        phone: c.phone,
                        name: c.name,
                        amount: `$${(p.totalAmount - p.paidAmount).toFixed(2)}`,
                      };
                    })
                    .filter(Boolean) as {
                    phone: string;
                    name: string;
                    amount: string;
                  }[];
                  sendAllReminders(list);
                }}
                style={({ pressed }) => [
                  styles.remindAllBtn,
                  { borderColor: "#25D366", backgroundColor: "#25D36610" },
                  pressed && { opacity: 0.75 },
                ]}
              >
                <Feather name="message-circle" size={16} color="#25D366" />
                <Text style={styles.remindAllText}>
                  {t("remindAll", { count: overduePayments.length })}
                </Text>
              </Pressable>
            )}

            {overduePayments.map((p) => {
              const client = getClientById(p.clientId);
              if (!client) return null;
              const daysOver = Math.max(
                0,
                Math.floor(
                  (now.getTime() - new Date(p.dueDate).getTime()) / 86400000
                )
              );
              const balance = `$${(p.totalAmount - p.paidAmount).toFixed(2)}`;
              return (
                <View
                  key={p.id}
                  style={[
                    styles.overdueCard,
                    {
                      backgroundColor: colors.overdueCard,
                      borderColor: colors.overdueBorder,
                    },
                  ]}
                >
                  {/* Client info */}
                  <Pressable
                    onPress={() => router.push(`/client/${client.id}`)}
                    style={styles.overdueTop}
                  >
                    <View>
                      <Text style={[styles.overdueName, { color: colors.overdueName }]}>{client.name}</Text>
                      <Text style={styles.overdueAmount}>
                        {balance} · {t("daysOverdue", { count: daysOver })}
                      </Text>
                    </View>
                    <Feather
                      name="chevron-right"
                      size={16}
                      color="#EF4444"
                    />
                  </Pressable>

                  {/* 1-tap WhatsApp reminder */}
                  <Pressable
                    onPress={() =>
                      sendWhatsApp(client.phone, client.name, balance)
                    }
                    style={({ pressed }) => [
                      styles.remindBtn,
                      pressed && { opacity: 0.7 },
                    ]}
                  >
                    <Feather name="message-circle" size={15} color="#25D366" />
                    <Text style={styles.remindBtnText}>
                      {t("sendReminderWhatsApp")}
                    </Text>
                  </Pressable>
                </View>
              );
            })}
          </View>
        )}

        {/* Active Pipeline */}
        <View style={styles.section}>
          <View style={styles.sectionRow}>
            <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
              {t("activePipeline")}
            </Text>
            <Pressable onPress={() => router.push("/(tabs)/clients")}>
              <Text style={[styles.seeAll, { color: colors.primary }]}>
                {t("seeAll")}
              </Text>
            </Pressable>
          </View>
          {activeClients.length === 0 ? (
            <EmptyState
              icon="users"
              title={t("noActiveClients")}
              subtitle={t("addFirstClient")}
            />
          ) : (
            activeClients
              .slice(0, 4)
              .map((c) => <ClientCard key={c.id} client={c} />)
          )}
        </View>
      </ScrollView>

      <QuickAddModal
        visible={showQuickAdd}
        onClose={() => setShowQuickAdd(false)}
      />

      <MessageTemplateSheet
        visible={!!selectedReminder}
        onClose={() => setSelectedReminder(null)}
        vars={{
          name: selectedReminder?.name,
          phone: selectedReminder?.phone ?? "",
          amount: selectedReminder?.amount,
        }}
        suggestPayment={true}
      />
    </>
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
  headerBtns: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
  },
  iconBtn: {
    width: 42,
    height: 42,
    borderRadius: 21,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
  },
  addBtn: {
    width: 46,
    height: 46,
    borderRadius: 23,
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
  countBadge: {
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 10,
  },
  countBadgeText: { fontSize: 12, fontWeight: "700" },
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
  remindAllBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 8,
    paddingVertical: 12,
    borderRadius: 12,
    borderWidth: 1.5,
  },
  remindAllText: {
    fontSize: 14,
    fontWeight: "700",
    color: "#25D366",
  },
  overdueCard: {
    borderRadius: 14,
    borderWidth: 1,
    overflow: "hidden",
  },
  overdueTop: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    padding: 14,
    paddingBottom: 10,
  },
  overdueName: { fontSize: 15, fontWeight: "700" },
  overdueAmount: { fontSize: 13, color: "#EF4444", fontWeight: "500", marginTop: 2 },
  remindBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 7,
    paddingVertical: 10,
    backgroundColor: "#25D36615",
    borderTopWidth: 1,
    borderTopColor: "#FECACA",
  },
  remindBtnText: {
    fontSize: 13,
    fontWeight: "700",
    color: "#25D366",
  },
});
