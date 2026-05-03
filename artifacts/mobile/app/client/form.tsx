import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { router, useLocalSearchParams } from "expo-router";
import React, { useState } from "react";
import {
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { KeyboardAwareScrollView } from "react-native-keyboard-controller";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import type { ClientStatus } from "@/context/DataContext";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";
import { useTranslation } from "react-i18next";

const STATUSES: ClientStatus[] = ["Lead", "Quoted", "Booked", "Completed", "Paid"];

export default function ClientFormScreen() {
  const { t } = useTranslation();
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { id } = useLocalSearchParams<{ id?: string }>();
  const { getClientById, addClient, updateClient } = useData();

  const existing = id ? getClientById(id) : undefined;

  const [name, setName] = useState(existing?.name ?? "");
  const [phone, setPhone] = useState(existing?.phone ?? "");
  const [serviceType, setServiceType] = useState(existing?.serviceType ?? "");
  const [notes, setNotes] = useState(existing?.notes ?? "");
  const [status, setStatus] = useState<ClientStatus>(existing?.status ?? "Lead");
  const [isSaving, setIsSaving] = useState(false);
  const [errors, setErrors] = useState<{ name?: string; phone?: string }>({});

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;

  const validate = () => {
    const e: { name?: string; phone?: string } = {};
    if (!name.trim()) e.name = t("nameRequired");
    if (!phone.trim()) e.phone = t("phoneRequired");
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setIsSaving(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    try {
      if (existing) {
        await updateClient(existing.id, {
          name: name.trim(),
          phone: phone.trim(),
          serviceType: serviceType.trim(),
          notes: notes.trim(),
          status,
        });
      } else {
        await addClient({
          name: name.trim(),
          phone: phone.trim(),
          serviceType: serviceType.trim(),
          notes: notes.trim(),
          status,
        });
      }
      router.back();
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <KeyboardAwareScrollView
      bottomOffset={Platform.OS === "ios" ? 0 : 20}
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
      style={[styles.container, { backgroundColor: colors.background }]}
      contentContainerStyle={[
        styles.content,
        { paddingBottom: insets.bottom + botPad + 24, paddingTop: topPad + 8 },
      ]}
    >
        <View
          style={[
            styles.card,
            { backgroundColor: colors.card, borderColor: colors.border },
          ]}
        >
          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>
              {t("name")} *
            </Text>
            <TextInput
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: errors.name ? colors.destructive : colors.border,
                },
              ]}
              value={name}
              onChangeText={(t) => {
                setName(t);
                if (errors.name) setErrors((e) => ({ ...e, name: undefined }));
              }}
              placeholder={t("clientNamePlaceholder")}
              placeholderTextColor={colors.mutedForeground}
              returnKeyType="next"
            />
            {errors.name && (
              <Text style={[styles.error, { color: colors.destructive }]}>
                {errors.name}
              </Text>
            )}
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>
              {t("phone")} *
            </Text>
            <TextInput
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: errors.phone ? colors.destructive : colors.border,
                },
              ]}
              value={phone}
              onChangeText={(t) => {
                setPhone(t);
                if (errors.phone) setErrors((e) => ({ ...e, phone: undefined }));
              }}
              placeholder="+1 234 567 8900"
              placeholderTextColor={colors.mutedForeground}
              keyboardType="phone-pad"
              returnKeyType="next"
            />
            {errors.phone && (
              <Text style={[styles.error, { color: colors.destructive }]}>
                {errors.phone}
              </Text>
            )}
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>
              {t("serviceType")}
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
              value={serviceType}
              onChangeText={setServiceType}
              placeholder={t("serviceTypePlaceholder")}
              placeholderTextColor={colors.mutedForeground}
              returnKeyType="next"
            />
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>{t("notes")}</Text>
            <TextInput
              style={[
                styles.input,
                styles.textarea,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: colors.border,
                },
              ]}
              value={notes}
              onChangeText={setNotes}
              placeholder={t("notesAdditionalPlaceholder")}
              placeholderTextColor={colors.mutedForeground}
              multiline
              numberOfLines={3}
              textAlignVertical="top"
            />
          </View>
        </View>

        <View
          style={[
            styles.card,
            { backgroundColor: colors.card, borderColor: colors.border },
          ]}
        >
          <Text style={[styles.sectionTitle, { color: colors.foreground }]}>
            {t("pipelineStatus")}
          </Text>
          <View style={styles.statusGrid}>
            {STATUSES.map((s) => (
              <Pressable
                key={s}
                onPress={() => setStatus(s)}
                style={({ pressed }) => [
                  styles.statusChip,
                  {
                    borderColor:
                      status === s ? colors.primary : colors.border,
                    backgroundColor:
                      status === s ? colors.primary + "15" : "transparent",
                  },
                  pressed && { opacity: 0.7 },
                ]}
              >
                {status === s && (
                  <Feather name="check" size={12} color={colors.primary} />
                )}
                <Text
                  style={[
                    styles.statusLabel,
                    {
                      color:
                        status === s ? colors.primary : colors.mutedForeground,
                      fontWeight: status === s ? "700" : "500",
                    },
                  ]}
                >
                  {t(`status${s}` as any)}
                </Text>
              </Pressable>
            ))}
          </View>
        </View>

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
          <Text style={styles.saveBtnText}>
            {existing ? t("saveChanges") : t("addClient")}
          </Text>
        </Pressable>
    </KeyboardAwareScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 16, gap: 12 },
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
  textarea: { height: 90, paddingTop: 12 },
  error: { fontSize: 12 },
  sectionTitle: { fontSize: 15, fontWeight: "700" },
  statusGrid: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  statusChip: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1.5,
    gap: 5,
  },
  statusLabel: { fontSize: 14 },
  saveBtn: {
    paddingVertical: 16,
    borderRadius: 14,
    alignItems: "center",
    marginTop: 4,
  },
  saveBtnText: { fontSize: 16, fontWeight: "700", color: "#fff" },
});
