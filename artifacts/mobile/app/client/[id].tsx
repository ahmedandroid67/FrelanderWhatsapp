import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { router, useLocalSearchParams, useNavigation } from "expo-router";
import React, { useLayoutEffect, useMemo, useState } from "react";
import {
  Alert,
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
import { MessageTemplateSheet } from "@/components/MessageTemplateSheet";
import { QuickBookingModal } from "@/components/QuickBookingModal";
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

  const [showTemplates, setShowTemplates] = useState(false);
  const [showBooking, setShowBooking] = useState(false);

  const client = getClientById(id ?? "");
  const bookings = getBookingsForClient(id ?? "");
  const payment = getPaymentForClient(id ?? "");
  const invoices = getInvoicesForClient(id ?? "");

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;

  const today = new Date().toISOString().split("T")[0];

  const nextBooking = useMemo(() => {
    return [...bookings]
      .filter((b) => b.date >= today)
      .sort((a, b) => a.date.localeCompare(b.date))[0];
  }, [bookings, today]);

  const isOverdue =
    payment &&
    payment.status !== "paid" &&
    payment.dueDate &&
    payment.dueDate < today;

  const balance =
    payment && payment.totalAmount > payment.paidAmount
      ? `$${(payment.totalAmount - payment.paidAmount).toFixed(2)}`
      : undefined;

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

  const handleGenerateInvoice = async () => {
    if (!payment) {
      Alert.alert(
        "No Payment",
        "Add payment details first to generate an invoice."
      );
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

  const paymentBalance = payment ? payment.totalAmount - payment.paidAmount : null;
  const currentIdx = PIPELINE.indexOf(client.status);

  return (
    <>
      <ScrollView
        style={[styles.container, { backgroundColor: colors.background }]}
        contentContainerStyle={[
          styles.content,
          {
            paddingBottom: insets.bottom + botPad + 24,
            paddingTop: topPad + 8,
          },
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
            <View
              style={[
                styles.avatar,
                { backgroundColor: colors.primary + "18" },
              ]}
            >
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
              <Text
                style={[
                  styles.clientPhone,
                  { color: colors.mutedForeground },
                ]}
              >
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
            <View
              style={[styles.notesBox, { backgroundColor: colors.muted }]}
            >
              <Text
                style={[
                  styles.notesText,
                  { color: colors.mutedForeground },
                ]}
              >
                {client.notes}
              </Text>
            </View>
          ) : null}

          {/* Send Message CTA */}
          <Pressable
            onPress={() => {
              Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
              setShowTemplates(true);
            }}
            style={({ pressed }) => [
              styles.sendMsgBtn,
              pressed && { opacity: 0.8 },
            ]}
          >
            <Feather name="message-circle" size={17} color="#25D366" />
            <Text style={styles.sendMsgText}>Send WhatsApp Message</Text>
            <View style={styles.sendMsgArrow}>
              <Feather name="chevron-right" size={14} color="#25D366" />
            </View>
          </Pressable>
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
                          backgroundColor: isActive
                            ? colors.primary
                            : colors.muted,
                          borderColor: isCurrent
                            ? colors.primary
                            : "transparent",
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
                <Text
                  style={[
                    styles.paymentLabel,
                    { color: colors.mutedForeground },
                  ]}
                >
                  Total
                </Text>
                <Text
                  style={[styles.paymentValue, { color: colors.foreground }]}
                >
                  ${payment.totalAmount.toFixed(2)}
                </Text>
              </View>
              <View style={styles.paymentRow}>
                <Text
                  style={[
                    styles.paymentLabel,
                    { color: colors.mutedForeground },
                  ]}
                >
                  Paid
                </Text>
                <Text style={[styles.paymentValue, { color: "#10B981" }]}>
                  ${payment.paidAmount.toFixed(2)}
                </Text>
              </View>
              <View
                style={[styles.divider, { backgroundColor: colors.border }]}
              />
              <View style={styles.paymentRow}>
                <Text
                  style={[
                    styles.paymentLabel,
                    { color: colors.mutedForeground },
                  ]}
                >
                  Balance
                </Text>
                <Text
                  style={[
                    styles.paymentValue,
                    {
                      color:
                        (paymentBalance ?? 0) > 0 ? "#EF4444" : "#10B981",
                      fontWeight: "700",
                      fontSize: 17,
                    },
                  ]}
                >
                  ${(paymentBalance ?? 0).toFixed(2)}
                </Text>
              </View>
              {payment.dueDate ? (
                <View style={styles.paymentRow}>
                  <Text
                    style={[
                      styles.paymentLabel,
                      { color: colors.mutedForeground },
                    ]}
                  >
                    Due
                  </Text>
                  <Text
                    style={[styles.paymentValue, { color: colors.foreground }]}
                  >
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
              <Text
                style={[
                  styles.addButtonText,
                  { color: colors.mutedForeground },
                ]}
              >
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
            <Pressable onPress={() => setShowBooking(true)}>
              <Feather name="plus" size={18} color={colors.primary} />
            </Pressable>
          </View>
          {bookings.length === 0 ? (
            <Pressable
              onPress={() => setShowBooking(true)}
              style={[styles.addButton, { borderColor: colors.border }]}
            >
              <Feather name="plus" size={16} color={colors.mutedForeground} />
              <Text
                style={[
                  styles.addButtonText,
                  { color: colors.mutedForeground },
                ]}
              >
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
                  <Text
                    style={[styles.invoiceDesc, { color: colors.foreground }]}
                  >
                    {inv.description}
                  </Text>
                  <Text
                    style={[
                      styles.invoiceDate,
                      { color: colors.mutedForeground },
                    ]}
                  >
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

        {/* Bottom actions */}
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
            <Text
              style={[styles.dangerBtnText, { color: colors.destructive }]}
            >
              Delete Client
            </Text>
          </Pressable>
        </View>
      </ScrollView>

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

      <QuickBookingModal
        visible={showBooking}
        clientId={client.id}
        clientName={client.name}
        onClose={() => setShowBooking(false)}
      />
    </>
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
  sendMsgBtn: {
    flexDirection: "row",
    alignItems: "center",
    backgroundColor: "#25D36610",
    borderWidth: 1,
    borderColor: "#25D36630",
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 13,
    gap: 10,
  },
  sendMsgText: { flex: 1, fontSize: 14, fontWeight: "700", color: "#25D366" },
  sendMsgArrow: {
    backgroundColor: "#25D36620",
    borderRadius: 8,
    padding: 4,
  },
  sectionTitle: { fontSize: 15, fontWeight: "700" },
  sectionRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
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
