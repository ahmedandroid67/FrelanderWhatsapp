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
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

export default function BookingFormScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { clientId } = useLocalSearchParams<{ clientId: string }>();
  const { addBooking, getClientById } = useData();

  const client = getClientById(clientId ?? "");
  const today = new Date().toISOString().split("T")[0];

  const [date, setDate] = useState(today);
  const [time, setTime] = useState("");
  const [location, setLocation] = useState("");
  const [notes, setNotes] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const [dateError, setDateError] = useState("");

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;

  const handleSave = async () => {
    if (!date.trim()) {
      setDateError("Date is required");
      return;
    }
    setIsSaving(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    try {
      await addBooking({
        clientId: clientId ?? "",
        date: date.trim(),
        time: time.trim(),
        location: location.trim(),
        notes: notes.trim(),
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
            Adding booking for {client.name}
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
              Date *
            </Text>
            <TextInput
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: dateError ? colors.destructive : colors.border,
                },
              ]}
              value={date}
              onChangeText={(t) => {
                setDate(t);
                setDateError("");
              }}
              placeholder="YYYY-MM-DD"
              placeholderTextColor={colors.mutedForeground}
              returnKeyType="next"
            />
            {dateError ? (
              <Text style={[styles.error, { color: colors.destructive }]}>
                {dateError}
              </Text>
            ) : null}
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>Time</Text>
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
              placeholder="e.g. 10:00 AM"
              placeholderTextColor={colors.mutedForeground}
              returnKeyType="next"
            />
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>
              Location
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
              value={location}
              onChangeText={setLocation}
              placeholder="e.g. Central Park, NYC"
              placeholderTextColor={colors.mutedForeground}
              returnKeyType="next"
            />
          </View>

          <View style={styles.field}>
            <Text style={[styles.label, { color: colors.foreground }]}>Notes</Text>
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
              placeholder="Any notes about this booking..."
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
          <Text style={styles.saveBtnText}>Add Booking</Text>
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
  textarea: { height: 90, paddingTop: 12 },
  error: { fontSize: 12 },
  saveBtn: {
    paddingVertical: 16,
    borderRadius: 14,
    alignItems: "center",
    marginTop: 4,
  },
  saveBtnText: { fontSize: 16, fontWeight: "700", color: "#fff" },
});
