import * as Haptics from "expo-haptics";
import { router, useLocalSearchParams } from "expo-router";
import React, { useState } from "react";
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { StatusBadge } from "@/components/StatusBadge";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

export default function PaymentScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { clientId } = useLocalSearchParams<{ clientId: string }>();
  const { getClientById, getPaymentForClient, setPayment } = useData();

  const client = getClientById(clientId ?? "");
  const existing = getPaymentForClient(clientId ?? "");

  const [total, setTotal] = useState(existing?.totalAmount.toString() ?? "");
  const [paid, setPaid] = useState(existing?.paidAmount.toString() ?? "");
  const [dueDate, setDueDate] = useState(existing?.dueDate ?? "");
  const [isSaving, setIsSaving] = useState(false);

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;

  const totalNum = parseFloat(total) || 0;
  const paidNum = parseFloat(paid) || 0;
  const balance = totalNum - paidNum;
  const computedStatus =
    paidNum <= 0 ? "unpaid" : paidNum >= totalNum ? "paid" : "partial";

  const handleSave = async () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    setIsSaving(true);
    try {
      await setPayment(clientId ?? "", {
        totalAmount: totalNum,
        paidAmount: paidNum,
        dueDate: dueDate.trim(),
      });
      router.back();
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={[styles.container, { backgroundColor: colors.background }]}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
      <ScrollView
        contentContainerStyle={[
          styles.content,
          { paddingBottom: insets.bottom + botPad + 24, paddingTop: topPad + 8 },
        ]}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >
        {client && (
          <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
            Payment for {client.name}
          </Text>
        )}

        <View
          style={[
            styles.card,
            { backgroundColor: colors.card, borderColor: colors.border },
          ]}
        >
          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>
              Total Amount ($)
            </Text>
            <TextInput
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: colors.border,
                },
              ]}
              value={total}
              onChangeText={setTotal}
              placeholder="0.00"
              placeholderTextColor={colors.mutedForeground}
              keyboardType="decimal-pad"
              returnKeyType="next"
            />
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>
              Amount Paid ($)
            </Text>
            <TextInput
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: colors.border,
                },
              ]}
              value={paid}
              onChangeText={setPaid}
              placeholder="0.00"
              placeholderTextColor={colors.mutedForeground}
              keyboardType="decimal-pad"
              returnKeyType="next"
            />
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>
              Due Date
            </Text>
            <TextInput
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: colors.border,
                },
              ]}
              value={dueDate}
              onChangeText={setDueDate}
              placeholder="YYYY-MM-DD"
              placeholderTextColor={colors.mutedForeground}
              returnKeyType="done"
            />
          </View>
        </View>

        {totalNum > 0 && (
          <View
            style={[
              styles.summary,
              { backgroundColor: colors.card, borderColor: colors.border },
            ]}
          >
            <Text style={[styles.summaryTitle, { color: colors.foreground }]}>
              Summary
            </Text>
            <View style={styles.summaryRow}>
              <Text style={[styles.summaryLabel, { color: colors.mutedForeground }]}>
                Total
              </Text>
              <Text style={[styles.summaryValue, { color: colors.foreground }]}>
                ${totalNum.toFixed(2)}
              </Text>
            </View>
            <View style={styles.summaryRow}>
              <Text style={[styles.summaryLabel, { color: colors.mutedForeground }]}>
                Paid
              </Text>
              <Text style={[styles.summaryValue, { color: "#10B981" }]}>
                ${paidNum.toFixed(2)}
              </Text>
            </View>
            <View style={[styles.divider, { backgroundColor: colors.border }]} />
            <View style={styles.summaryRow}>
              <Text style={[styles.summaryLabel, { color: colors.mutedForeground }]}>
                Balance
              </Text>
              <Text
                style={[
                  styles.summaryValue,
                  {
                    color: balance > 0 ? "#EF4444" : "#10B981",
                    fontWeight: "700",
                    fontSize: 20,
                  },
                ]}
              >
                ${balance.toFixed(2)}
              </Text>
            </View>
            <StatusBadge status={computedStatus} />
          </View>
        )}

        <Pressable
          onPress={handleSave}
          disabled={isSaving}
          style={({ pressed }) => [
            styles.saveBtn,
            { backgroundColor: colors.primary },
            pressed && { opacity: 0.85 },
            isSaving && { opacity: 0.6 },
          ]}
        >
          <Text style={styles.saveBtnText}>Save Payment</Text>
        </Pressable>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 16, gap: 12 },
  subtitle: { fontSize: 14 },
  card: { borderRadius: 16, borderWidth: 1, padding: 16, gap: 14 },
  field: { gap: 6 },
  label: { fontSize: 14, fontWeight: "600" },
  input: {
    borderRadius: 10,
    borderWidth: 1,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 15,
  },
  summary: { borderRadius: 16, borderWidth: 1, padding: 16, gap: 10 },
  summaryTitle: { fontSize: 14, fontWeight: "700" },
  summaryRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  summaryLabel: { fontSize: 14 },
  summaryValue: { fontSize: 16, fontWeight: "600" },
  divider: { height: 1 },
  saveBtn: {
    paddingVertical: 16,
    borderRadius: 14,
    alignItems: "center",
    marginTop: 4,
  },
  saveBtnText: { fontSize: 16, fontWeight: "700", color: "#fff" },
});
