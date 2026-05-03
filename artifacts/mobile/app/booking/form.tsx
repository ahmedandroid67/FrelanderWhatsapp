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
import { useTranslation } from "react-i18next";
import { KeyboardAwareScrollView } from "react-native-keyboard-controller";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { DatePickerModal } from "@/components/DatePickerModal";
import { useData } from "@/context/DataContext";
import { useNotifications } from "@/hooks/useNotifications";
import { useColors } from "@/hooks/useColors";

export default function BookingFormScreen() {
  const { t } = useTranslation();
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { clientId, bookingId } = useLocalSearchParams<{
    clientId?: string;
    bookingId?: string;
  }>();
  const { addBooking, updateBooking, getClientById, getBookingsForClient } = useData();
  const { scheduleBookingNotification, cancelBookingNotification } = useNotifications();

  // If editing, find existing booking
  const allBookings = clientId ? getBookingsForClient(clientId) : [];
  const existingBooking = bookingId
    ? allBookings.find((b) => b.id === bookingId) ?? null
    : null;

  const client = getClientById(clientId ?? "");
  const today = new Date().toISOString().split("T")[0];

  const [date, setDate] = useState(existingBooking?.date ?? today);
  const [time, setTime] = useState(existingBooking?.time ?? "");
  const [location, setLocation] = useState(existingBooking?.location ?? "");
  const [notes, setNotes] = useState(existingBooking?.notes ?? "");
  const [isSaving, setIsSaving] = useState(false);
  const [dateError, setDateError] = useState("");
  const [showDatePicker, setShowDatePicker] = useState(false);

  const formatDisplayDate = (d: string) => {
    if (!d) return "";
    const parts = d.split('-');
    if (parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`;
    return d;
  };

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;
  const isEditing = !!existingBooking;

  const handleSave = async () => {
    if (!date.trim()) {
      setDateError(t("dateRequired"));
      return;
    }
    setIsSaving(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    try {
      if (isEditing && existingBooking) {
        await updateBooking(existingBooking.id, {
          date: date.trim(),
          time: time.trim(),
          location: location.trim(),
          notes: notes.trim(),
        });
        // Reschedule notification with new date
        await cancelBookingNotification(existingBooking.id);
        if (client) {
          await scheduleBookingNotification(existingBooking.id, client.name, date.trim(), time.trim());
        }
      } else {
        const booking = await addBooking({
          clientId: clientId ?? "",
          date: date.trim(),
          time: time.trim(),
          location: location.trim(),
          notes: notes.trim(),
        });
        // Schedule notification
        if (client) {
          await scheduleBookingNotification(booking.id, client.name, date.trim(), time.trim());
        }
      }
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      router.back();
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <>
    <KeyboardAwareScrollView
      bottomOffset={Platform.OS === "ios" ? 0 : 20}
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
      style={[styles.container, { backgroundColor: colors.background }]}
      contentContainerStyle={[
        styles.content,
        {
          paddingBottom: insets.bottom + botPad + 100,
          paddingTop: topPad + 16,
        },
      ]}
    >
        {client && (
          <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
            {isEditing 
              ? t("editingBookingFor", { name: client.name }) 
              : t("addingBookingFor", { name: client.name })}
          </Text>
        )}

        <View
          style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}
        >
          {/* Date field — taps open picker */}
          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>{t("date")} *</Text>
            <Pressable
              onPress={() => setShowDatePicker(true)}
              style={[
                styles.dateBtn,
                {
                   backgroundColor: colors.muted,
                   borderColor: dateError ? colors.destructive : colors.border,
                },
              ]}
            >
              <Feather name="calendar" size={16} color={colors.primary} />
              <Text style={[styles.dateBtnText, { color: date ? colors.foreground : colors.mutedForeground }]}>
                {date ? formatDisplayDate(date) : t("selectDate")}
              </Text>
              <Feather name="chevron-right" size={14} color={colors.mutedForeground} />
            </Pressable>
            {dateError ? (
              <Text style={[styles.error, { color: colors.destructive }]}>{dateError}</Text>
            ) : null}
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>{t("time")}</Text>
            <TextInput
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: colors.border,
                },
              ]}
              value={time}
              onChangeText={setTime}
              placeholder={t("eGTime")}
              placeholderTextColor={colors.mutedForeground}
              returnKeyType="next"
            />
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>{t("location")}</Text>
            <TextInput
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: colors.border,
                },
              ]}
              value={location}
              onChangeText={setLocation}
              placeholder={t("eGLocation")}
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
              placeholder={t("notesPlaceholder")}
              placeholderTextColor={colors.mutedForeground}
              multiline
              numberOfLines={3}
              textAlignVertical="top"
            />
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
          <Feather name={isEditing ? "check" : "calendar"} size={18} color="#fff" />
          <Text style={styles.saveBtnText}>
            {isSaving ? t("saving") : isEditing ? t("saveChanges") : t("addBooking")}
          </Text>
        </Pressable>
    </KeyboardAwareScrollView>
      <DatePickerModal
        visible={showDatePicker}
        value={date}
        label={t("selectBookingDate")}
        onConfirm={(d) => {
          setDate(d);
          setDateError("");
        }}
        onClose={() => setShowDatePicker(false)}
      />
    </>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 16, gap: 12 },
  subtitle: { fontSize: 14 },
  card: { borderRadius: 16, borderWidth: 1, padding: 16, gap: 14 },
  field: { gap: 6 },
  label: { fontSize: 14, fontWeight: "600" },
  dateBtn: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    borderRadius: 10,
    borderWidth: 1,
    paddingHorizontal: 14,
    paddingVertical: 13,
  },
  dateBtnText: { flex: 1, fontSize: 15 },
  input: {
    borderRadius: 10,
    borderWidth: 1,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 15,
  },
  textarea: { height: 90, paddingTop: 12 },
  error: { fontSize: 12 },
  saveBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 8,
    paddingVertical: 16,
    borderRadius: 14,
    marginTop: 4,
  },
  saveBtnText: { fontSize: 16, fontWeight: "700", color: "#fff" },
});
