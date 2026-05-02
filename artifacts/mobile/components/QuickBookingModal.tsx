import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import React, { useEffect, useRef, useState } from "react";
import {
  Animated,
  Keyboard,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

const USE_NATIVE_DRIVER = Platform.OS !== "web";

interface QuickBookingModalProps {
  visible: boolean;
  clientId: string;
  clientName?: string;
  onClose: () => void;
}

export function QuickBookingModal({
  visible,
  clientId,
  clientName,
  onClose,
}: QuickBookingModalProps) {
  const colors = useColors();
  const { addBooking } = useData();
  const slideAnim = useRef(new Animated.Value(500)).current;
  const timeRef = useRef<TextInput>(null);

  const [date, setDate] = useState(new Date().toISOString().split("T")[0]);
  const [time, setTime] = useState("");
  const [location, setLocation] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (visible) {
      setDate(new Date().toISOString().split("T")[0]);
      setTime("");
      setLocation("");
      setIsSaving(false);
      Animated.spring(slideAnim, {
        toValue: 0,
        useNativeDriver: USE_NATIVE_DRIVER,
        damping: 22,
        stiffness: 220,
      }).start();
    } else {
      Animated.timing(slideAnim, {
        toValue: 500,
        duration: 180,
        useNativeDriver: USE_NATIVE_DRIVER,
      }).start();
    }
  }, [visible]);

  const handleSave = async () => {
    if (!date.trim()) return;
    setIsSaving(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    try {
      await addBooking({
        clientId,
        date: date.trim(),
        time: time.trim(),
        location: location.trim(),
        notes: "",
      });
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      onClose();
    } finally {
      setIsSaving(false);
    }
  };

  if (!visible) return null;

  return (
    <Modal
      transparent
      visible={visible}
      animationType="none"
      statusBarTranslucent
      onRequestClose={onClose}
    >
      <TouchableWithoutFeedback
        onPress={() => {
          Keyboard.dismiss();
          onClose();
        }}
      >
        <View style={styles.backdrop} />
      </TouchableWithoutFeedback>

      <Animated.View
        style={[
          styles.sheet,
          {
            backgroundColor: colors.card,
            transform: [{ translateY: slideAnim }],
            paddingBottom: Platform.OS === "ios" ? 44 : 28,
          },
        ]}
      >
        <View style={[styles.handle, { backgroundColor: colors.border }]} />

        <View style={styles.header}>
          <Text style={[styles.title, { color: colors.foreground }]}>
            Book {clientName ?? "Client"}
          </Text>
          <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
            Date auto-set to today
          </Text>
        </View>

        <View
          style={[
            styles.dateRow,
            {
              backgroundColor: colors.muted,
              borderColor: colors.border,
            },
          ]}
        >
          <Feather name="calendar" size={18} color={colors.primary} />
          <TextInput
            style={[styles.dateInput, { color: colors.foreground }]}
            value={date}
            onChangeText={setDate}
            placeholder="YYYY-MM-DD"
            placeholderTextColor={colors.mutedForeground}
            returnKeyType="next"
            onSubmitEditing={() => timeRef.current?.focus()}
          />
          <Pressable
            onPress={() =>
              setDate(new Date().toISOString().split("T")[0])
            }
          >
            <View
              style={[
                styles.todayPill,
                { backgroundColor: colors.primary + "18" },
              ]}
            >
              <Text style={[styles.todayText, { color: colors.primary }]}>
                Today
              </Text>
            </View>
          </Pressable>
        </View>

        <TextInput
          ref={timeRef}
          style={[
            styles.input,
            {
              backgroundColor: colors.muted,
              color: colors.foreground,
              borderColor: colors.border,
            },
          ]}
          placeholder="Time — e.g. 10:00 AM (optional)"
          placeholderTextColor={colors.mutedForeground}
          value={time}
          onChangeText={setTime}
          returnKeyType="next"
        />

        <TextInput
          style={[
            styles.input,
            {
              backgroundColor: colors.muted,
              color: colors.foreground,
              borderColor: colors.border,
            },
          ]}
          placeholder="Location (optional)"
          placeholderTextColor={colors.mutedForeground}
          value={location}
          onChangeText={setLocation}
          returnKeyType="done"
          onSubmitEditing={handleSave}
        />

        <View style={styles.btnRow}>
          <Pressable
            onPress={onClose}
            style={[styles.cancelBtn, { borderColor: colors.border }]}
          >
            <Text
              style={[styles.cancelText, { color: colors.mutedForeground }]}
            >
              Cancel
            </Text>
          </Pressable>
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
            <Feather name="calendar" size={17} color="#fff" />
            <Text style={styles.saveBtnText}>
              {isSaving ? "Saving..." : "Book It"}
            </Text>
          </Pressable>
        </View>
      </Animated.View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: "rgba(0,0,0,0.45)",
  },
  sheet: {
    position: "absolute",
    bottom: 0,
    left: 0,
    right: 0,
    borderTopLeftRadius: 26,
    borderTopRightRadius: 26,
    padding: 24,
    gap: 14,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.12,
    shadowRadius: 20,
    elevation: 20,
  },
  handle: {
    width: 40,
    height: 4,
    borderRadius: 2,
    alignSelf: "center",
    marginBottom: 8,
  },
  header: { gap: 2, marginBottom: 4 },
  title: { fontSize: 22, fontWeight: "800" },
  subtitle: { fontSize: 13 },
  dateRow: {
    flexDirection: "row",
    alignItems: "center",
    borderRadius: 14,
    borderWidth: 1.5,
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 10,
  },
  dateInput: { flex: 1, fontSize: 17 },
  todayPill: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
  },
  todayText: { fontSize: 12, fontWeight: "700" },
  input: {
    borderRadius: 14,
    borderWidth: 1.5,
    paddingHorizontal: 16,
    paddingVertical: 15,
    fontSize: 16,
  },
  btnRow: { flexDirection: "row", gap: 10, marginTop: 6 },
  cancelBtn: {
    flex: 1,
    paddingVertical: 16,
    borderRadius: 14,
    alignItems: "center",
    borderWidth: 1,
  },
  cancelText: { fontSize: 15, fontWeight: "600" },
  saveBtn: {
    flex: 2,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 16,
    borderRadius: 14,
    gap: 8,
  },
  saveBtnText: { fontSize: 16, fontWeight: "700", color: "#fff" },
});
