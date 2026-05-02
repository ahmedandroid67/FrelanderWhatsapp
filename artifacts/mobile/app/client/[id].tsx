import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { router, useLocalSearchParams, useNavigation } from "expo-router";
import React, { useLayoutEffect } from "react";
import {
  Alert,
  Linking,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { BookingCard } from "@/components/BookingCard";
import { EmptyState } from "@/components/EmptyState";
import { StatusBadge } from "@/components/StatusBadge";
import type { ClientStatus } from "@/context/DataContext";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

const PIPELINE: ClientStatus[] = [
  "Lead",
  "Quoted",
  "Booked",
  "Completed",
  "Paid",
];

async function openWhatsApp(phone: string, message: string) {
  const cleaned = phone.replace(/\D/g, "");
  const native = `whatsapp://send?phone=${cleaned}&text=${encodeURIComponent(message)}`;
  const web = `https://wa.me/${cleaned}?text=${encodeURIComponent(message)}`;
  try {
    const ok = await Linking.canOpenURL(native);
    await Linking.openURL(ok ? native : web);
  } catch {
    await Linking.openURL(web);
  }
}

export default function ClientDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const navigation = useNavigation();
  const {
    getClientById,
    updateClient,
    deleteClient,
    getBookingsForClient,
    getPaymentForClient,
    addInvoice,
    getInvoicesForClient,
    deleteBooking,
  } = useData();

  const client = getClientById(id ?? "");
  const bookings = getBookingsForClient(id ?? "");
  const payment = getPaymentForClient(id ?? "");
  const invoices = getInvoicesForClient(id ?? "");

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;

  useLayoutEffect(() => {
    navigation.setOptions({
      title: client?.name ?? "Client",
      headerRight: () =>
        client ? (
          <Pressable
            onPress={() =>
              router.push({
                pathname: "/client/form",
                params: { id: client.id },
              })
            }
            style={{ marginRight: 8 }}
          >
            <Feather name="edit-2" size={18} color={colors.primary} />
          </Pressable>
        ) : null,
    });
  }, [navigation, client, colors.primary]);

  if (!client) {
    return (
      <View style={[styles.container, { backgroundColor: colors.background }]}>
        <EmptyState icon="user-x" title="Client not found" />
      </View>
    );
  }

  const handleStatusChange = async (status: ClientStatus) => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    await updateClient(client.id, { status });
  };

  const handleDelete = () => {
    Alert.alert(
      "Delete Client",
      `Delete ${client.name}? This also removes their bookings and payments.`,
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete",
          style: "destructive",
          onPress: async () => {
            await deleteClient(client.id);
            router.back();
          },
        },
      ]
    );
  };

  const handleWhatsApp = async (type: "reminder" | "invoice" | "booking") => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    let message = "";
    if (type === "reminder") {
      const balance = payment
        ? `$${(payment.totalAmount - payment.paidAmount).toFixed(2)}`
        : "your balance";
      message = `Hi ${client.name}, just a reminder that ${balance} is due. Please let me know if you have any questions. Thank you!`;
    } else if (type === "invoice") {
      const latest = invoices[invoices.length - 1];
      message = latest
        ? `Hi ${client.name}, here is your invoice for ${latest.description}. Amount: $${latest.amount.toFixed(2)}. Thank you for your business!`
        : `Hi ${client.name}, please find your invoice here. Thank you for your business!`;
    } else {
      const next = [...bookings].sort((a, b) => a.date.localeCompare(b.date))[0];
      message = next
        ? `Hi ${client.name}, confirming your booking on ${next.date}${next.time ? ` at ${next.time}` : ""}${next.location ? `, ${next.location}` : ""}. Looking forward to it!`
        : `Hi ${client.name}, confirming your upcoming booking. Let me know if you have any questions!`;
    }
    await openWhatsApp(client.phone, message);
  };

  const handleGenerateInvoice = async () => {
    if (!payment) {
      Alert.alert("No Payment", "Add payment details first to generate an invoice.");
      return;
    }
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    await addInvoice({
      clientId: client.id,
      amount: payment.totalAmount,
      description: client.serviceType || "Services",
    });
    Alert.alert("Invoice Created", "Invoice generated successfully.");
  };

  const balance = payment ? payment.totalAmount - payment.paidAmount : null;
  const currentIdx = PIPELINE.indexOf(client.status);

  return (
    <ScrollView
      style={[styles.container, { backgroundColor: colors.background }]}
      contentContainerStyle={[
        styles.content,
        { paddingBottom: insets.bottom + botPad + 24, paddingTop: topPad + 8 },
      ]}
      showsVerticalScrollIndicator={false}
    >
      {/* Client Header */}
      <View
        style={[
          styles.card,
          { backgroundColor: colors.card, borderColor: colors.border },
        ]}
      >
        <View style={styles.clientHeader}>
          <View style={[styles.avatar, { backgroundColor: colors.primary + "18" }]}>
            <Text style={[styles.avatarText, { color: colors.primary }]}>
              {client.name
                .split(" ")
                .map((w) => w[0])
                .join("")
                .slice(0, 2)
                .toUpperCase()}
            </Text>
          </View>
          <View style={styles.clientMeta}>
            <Text style={[styles.clientName, { color: colors.foreground }]}>
              {client.name}
            </Text>
            <Text style={[styles.clientPhone, { color: colors.mutedForeground }]}>
              {client.phone}
            </Text>
            {client.serviceType ? (
              <Text style={[styles.clientService, { color: colors.primary }]}>
                {client.serviceType}
              </Text>
            ) : null}
          </View>
        </View>
        {client.notes ? (
          <View style={[styles.notesBox, { backgroundColor: colors.muted }]}>
            <Text style={[styles.notesText, { color: colors.mutedForeground }]}>
              {client.notes}
            </Text>
          </View>
        ) : null}
      </View>

      {/* Pipeline */}
      <View
        style={[
          styles.card,
          { backgroundColor: colors.card, borderColor: colors.border },
        ]}
      >
        <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
          Pipeline
        </Text>
        <View style={styles.pipeline}>
          {PIPELINE.map((status, i) => {
            const isActive = i <= currentIdx;
            const isCurrent = i === currentIdx;
            return (
              <React.Fragment key={status}>
                <Pressable
                  onPress={() => handleStatusChange(status)}
                  style={styles.step}
                >
                  <View
                    style={[
                      styles.dot,
                      {
                        backgroundColor: isActive ? colors.primary : colors.muted,
                        borderColor: isCurrent ? colors.primary : "transparent",
                      },
                      isCurrent && styles.dotActive,
                    ]}
                  >
                    {isActive && (
                      <Feather name="check" size={10} color="#fff" />
                    )}
                  </View>
                  <Text
                    style={[
                      styles.stepLabel,
                      {
                        color: isCurrent
                          ? colors.primary
                          : isActive
                          ? colors.foreground
                          : colors.mutedForeground,
                        fontWeight: isCurrent ? "700" : "500",
                      },
                    ]}
                    numberOfLines={1}
                  >
                    {status}
                  </Text>
                </Pressable>
                {i < PIPELINE.length - 1 && (
                  <View
                    style={[
                      styles.stepLine,
                      {
                        backgroundColor:
                          i < currentIdx ? colors.primary : colors.muted,
                      },
                    ]}
                  />
                )}
              </React.Fragment>
            );
          })}
        </View>
      </View>

      {/* WhatsApp Actions */}
      <View
        style={[
          styles.card,
          { backgroundColor: colors.card, borderColor: colors.border },
        ]}
      >
        <View style={styles.sectionRow}>
          <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
            WhatsApp
          </Text>
          <View style={styles.waIcon}>
            <Feather name="message-circle" size={16} color="#25D366" />
          </View>
        </View>
        <View style={styles.waRow}>
          {(
            [
              { type: "reminder" as const, icon: "bell", label: "Reminder" },
              { type: "invoice" as const, icon: "file-text", label: "Invoice" },
              { type: "booking" as const, icon: "calendar", label: "Confirm" },
            ] as const
          ).map((item) => (
            <Pressable
              key={item.type}
              onPress={() => handleWhatsApp(item.type)}
              style={({ pressed }) => [
                styles.waButton,
                pressed && { opacity: 0.7 },
              ]}
            >
              <Feather name={item.icon} size={18} color="#25D366" />
              <Text style={styles.waButtonText}>{item.label}</Text>
            </Pressable>
          ))}
        </View>
      </View>

      {/* Payment */}
      <View
        style={[
          styles.card,
          { backgroundColor: colors.card, borderColor: colors.border },
        ]}
      >
        <View style={styles.sectionRow}>
          <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
            Payment
          </Text>
          <Pressable onPress={() => router.push(`/payment/${client.id}`)}>
            <Feather
              name={payment ? "edit-2" : "plus"}
              size={18}
              color={colors.primary}
            />
          </Pressable>
        </View>
        {payment ? (
          <View style={styles.paymentGrid}>
            <View style={styles.paymentRow}>
              <Text style={[styles.paymentLabel, { color: colors.mutedForeground }]}>
                Total
              </Text>
              <Text style={[styles.paymentValue, { color: colors.foreground }]}>
                ${payment.totalAmount.toFixed(2)}
              </Text>
            </View>
            <View style={styles.paymentRow}>
              <Text style={[styles.paymentLabel, { color: colors.mutedForeground }]}>
                Paid
              </Text>
              <Text style={[styles.paymentValue, { color: "#10B981" }]}>
                ${payment.paidAmount.toFixed(2)}
              </Text>
            </View>
            <View style={[styles.divider, { backgroundColor: colors.border }]} />
            <View style={styles.paymentRow}>
              <Text style={[styles.paymentLabel, { color: colors.mutedForeground }]}>
                Balance
              </Text>
              <Text
                style={[
                  styles.paymentValue,
                  {
                    color: (balance ?? 0) > 0 ? "#EF4444" : "#10B981",
                    fontWeight: "700",
                    fontSize: 17,
                  },
                ]}
              >
                ${(balance ?? 0).toFixed(2)}
              </Text>
            </View>
            {payment.dueDate ? (
              <View style={styles.paymentRow}>
                <Text
                  style={[styles.paymentLabel, { color: colors.mutedForeground }]}
                >
                  Due
                </Text>
                <Text style={[styles.paymentValue, { color: colors.foreground }]}>
                  {payment.dueDate}
                </Text>
              </View>
            ) : null}
            <StatusBadge status={payment.status} />
          </View>
        ) : (
          <Pressable
            onPress={() => router.push(`/payment/${client.id}`)}
            style={[styles.addButton, { borderColor: colors.border }]}
          >
            <Feather name="plus" size={16} color={colors.mutedForeground} />
            <Text style={[styles.addButtonText, { color: colors.mutedForeground }]}>
              Add payment details
            </Text>
          </Pressable>
        )}
      </View>

      {/* Bookings */}
      <View
        style={[
          styles.card,
          { backgroundColor: colors.card, borderColor: colors.border },
        ]}
      >
        <View style={styles.sectionRow}>
          <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
            Bookings
          </Text>
          <Pressable
            onPress={() =>
              router.push({
                pathname: "/booking/form",
                params: { clientId: client.id },
              })
            }
          >
            <Feather name="plus" size={18} color={colors.primary} />
          </Pressable>
        </View>
        {bookings.length === 0 ? (
          <Pressable
            onPress={() =>
              router.push({
                pathname: "/booking/form",
                params: { clientId: client.id },
              })
            }
            style={[styles.addButton, { borderColor: colors.border }]}
          >
            <Feather name="plus" size={16} color={colors.mutedForeground} />
            <Text style={[styles.addButtonText, { color: colors.mutedForeground }]}>
              Add a booking
            </Text>
          </Pressable>
        ) : (
          [...bookings]
            .sort((a, b) => a.date.localeCompare(b.date))
            .map((booking) => (
              <BookingCard
                key={booking.id}
                booking={booking}
                onPress={() => {
                  Alert.alert(
                    `${booking.date}${booking.time ? ` · ${booking.time}` : ""}`,
                    `${booking.location ? `📍 ${booking.location}\n` : ""}${booking.notes || ""}`,
                    [
                      { text: "Close", style: "cancel" },
                      {
                        text: "Delete",
                        style: "destructive",
                        onPress: () => deleteBooking(booking.id),
                      },
                    ]
                  );
                }}
              />
            ))
        )}
      </View>

      {/* Invoices */}
      {invoices.length > 0 && (
        <View
          style={[
            styles.card,
            { backgroundColor: colors.card, borderColor: colors.border },
          ]}
        >
          <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
            Invoices
          </Text>
          {invoices.map((inv, idx) => (
            <View
              key={inv.id}
              style={[
                styles.invoiceItem,
                {
                  borderTopColor: colors.border,
                  borderTopWidth: idx === 0 ? 0 : 1,
                },
              ]}
            >
              <View style={styles.invoiceLeft}>
                <Text style={[styles.invoiceDesc, { color: colors.foreground }]}>
                  {inv.description}
                </Text>
                <Text style={[styles.invoiceDate, { color: colors.mutedForeground }]}>
                  {new Date(inv.createdAt).toLocaleDateString()}
                </Text>
              </View>
              <Text style={[styles.invoiceAmount, { color: colors.primary }]}>
                ${inv.amount.toFixed(2)}
              </Text>
            </View>
          ))}
        </View>
      )}

      {/* Actions */}
      <View style={styles.actions}>
        <Pressable
          onPress={handleGenerateInvoice}
          style={({ pressed }) => [
            styles.primaryBtn,
            { backgroundColor: colors.primary },
            pressed && { opacity: 0.85 },
          ]}
        >
          <Feather name="file-text" size={18} color="#fff" />
          <Text style={styles.primaryBtnText}>Generate Invoice</Text>
        </Pressable>
        <Pressable
          onPress={handleDelete}
          style={({ pressed }) => [
            styles.dangerBtn,
            { borderColor: colors.destructive },
            pressed && { opacity: 0.85 },
          ]}
        >
          <Feather name="trash-2" size={16} color={colors.destructive} />
          <Text style={[styles.dangerBtnText, { color: colors.destructive }]}>
            Delete Client
          </Text>
        </Pressable>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 16, gap: 12 },
  card: { borderRadius: 16, borderWidth: 1, padding: 16, gap: 12 },
  clientHeader: { flexDirection: "row", gap: 14, alignItems: "flex-start" },
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: "center",
    justifyContent: "center",
  },
  avatarText: { fontSize: 20, fontWeight: "700" },
  clientMeta: { flex: 1, gap: 3 },
  clientName: { fontSize: 20, fontWeight: "700" },
  clientPhone: { fontSize: 14 },
  clientService: { fontSize: 13, fontWeight: "600" },
  notesBox: { borderRadius: 8, padding: 10 },
  notesText: { fontSize: 13, lineHeight: 18 },
  sectionTitle: { fontSize: 15, fontWeight: "700" },
  sectionRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  waIcon: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: "#25D36618",
    alignItems: "center",
    justifyContent: "center",
  },
  pipeline: {
    flexDirection: "row",
    alignItems: "flex-start",
    justifyContent: "space-between",
  },
  step: { alignItems: "center", flex: 1, gap: 4 },
  dot: {
    width: 22,
    height: 22,
    borderRadius: 11,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 2,
  },
  dotActive: {
    shadowColor: "#2563EB",
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.5,
    shadowRadius: 4,
  },
  stepLabel: { fontSize: 10, textAlign: "center" },
  stepLine: { height: 2, flex: 0.5, marginTop: 10 },
  waRow: { flexDirection: "row", gap: 8 },
  waButton: {
    flex: 1,
    alignItems: "center",
    paddingVertical: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#25D36640",
    backgroundColor: "#25D36612",
    gap: 4,
  },
  waButtonText: { fontSize: 11, fontWeight: "600", color: "#25D366" },
  paymentGrid: { gap: 10 },
  paymentRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  paymentLabel: { fontSize: 14 },
  paymentValue: { fontSize: 15, fontWeight: "600" },
  divider: { height: 1 },
  addButton: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 12,
    borderRadius: 10,
    borderWidth: 1,
    borderStyle: "dashed",
    gap: 8,
  },
  addButtonText: { fontSize: 14 },
  invoiceItem: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingVertical: 10,
  },
  invoiceLeft: { flex: 1 },
  invoiceDesc: { fontSize: 14, fontWeight: "500" },
  invoiceDate: { fontSize: 12 },
  invoiceAmount: { fontSize: 16, fontWeight: "700" },
  actions: { gap: 10 },
  primaryBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 16,
    borderRadius: 14,
    gap: 8,
  },
  primaryBtnText: { fontSize: 16, fontWeight: "700", color: "#fff" },
  dangerBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 14,
    borderRadius: 14,
    borderWidth: 1,
    gap: 8,
  },
  dangerBtnText: { fontSize: 15, fontWeight: "600" },
});
