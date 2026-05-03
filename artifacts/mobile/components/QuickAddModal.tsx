import AsyncStorage from "@react-native-async-storage/async-storage";
import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import React, { useEffect, useRef, useState } from "react";
import {
  Animated,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import { KeyboardAwareScrollView } from "react-native-keyboard-controller";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

const USE_NATIVE_DRIVER = Platform.OS !== "web";
const LAST_SERVICE_KEY = "@cf_last_service";

interface QuickAddModalProps {
  visible: boolean;
  onClose: () => void;
  onClientAdded?: (clientId: string) => void;
}

export function QuickAddModal({
  visible,
  onClose,
  onClientAdded,
}: QuickAddModalProps) {
  const colors = useColors();
  const { addClient } = useData();
  const slideAnim = useRef(new Animated.Value(500)).current;
  const nameRef = useRef<TextInput>(null);
  const phoneRef = useRef<TextInput>(null);

  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [nameError, setNameError] = useState(false);
  const [phoneError, setPhoneError] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [lastService, setLastService] = useState("");

  useEffect(() => {
    if (visible) {
      setName("");
      setPhone("");
      setNameError(false);
      setPhoneError(false);
      setIsSaving(false);
      AsyncStorage.getItem(LAST_SERVICE_KEY).then((v) =>
        setLastService(v ?? "")
      );
      Animated.spring(slideAnim, {
        toValue: 0,
        useNativeDriver: USE_NATIVE_DRIVER,
        damping: 22,
        stiffness: 220,
      }).start(() => {
        setTimeout(() => nameRef.current?.focus(), 100);
      });
    } else {
      Animated.timing(slideAnim, {
        toValue: 500,
        duration: 180,
        useNativeDriver: USE_NATIVE_DRIVER,
      }).start();
    }
  }, [visible]);

  const handleSave = async () => {
    const nameOk = name.trim().length > 0;
    const phoneOk = phone.trim().length > 0;
    if (!nameOk || !phoneOk) {
      setNameError(!nameOk);
      setPhoneError(!phoneOk);
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Error);
      return;
    }
    setIsSaving(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    try {
      const client = await addClient({
        name: name.trim(),
        phone: phone.trim(),
        serviceType: lastService,
        notes: "",
        status: "Lead",
      });
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      onClientAdded?.(client.id);
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
      {/* Backdrop — tap to dismiss */}
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.backdrop} />
      </TouchableWithoutFeedback>

      {/* Sheet slides up from bottom */}
      <Animated.View
        style={[
          styles.sheet,
          {
            backgroundColor: colors.card,
            transform: [{ translateY: slideAnim }],
          },
        ]}
      >
        {/* KeyboardAvoidingView wraps content so inputs slide above keyboard */}
        <KeyboardAwareScrollView
          bottomOffset={Platform.OS === "ios" ? 0 : 20}
          keyboardShouldPersistTaps="handled"
          bounces={false}
          showsVerticalScrollIndicator={false}
          contentContainerStyle={[
            styles.content,
            { paddingBottom: Platform.OS === "ios" ? 44 : 28 },
          ]}
        >
            <View style={[styles.handle, { backgroundColor: colors.border }]} />

            <View style={styles.header}>
              <Text style={[styles.title, { color: colors.foreground }]}>
                New Client
              </Text>
              <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
                Just 2 fields — done in seconds
              </Text>
            </View>

            <TextInput
              ref={nameRef}
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: nameError ? colors.destructive : colors.border,
                },
              ]}
              placeholder="Full name *"
              placeholderTextColor={colors.mutedForeground}
              value={name}
              onChangeText={(t) => {
                setName(t);
                setNameError(false);
              }}
              returnKeyType="next"
              onSubmitEditing={() => phoneRef.current?.focus()}
              autoCapitalize="words"
              autoCorrect={false}
            />

            <TextInput
              ref={phoneRef}
              style={[
                styles.input,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: phoneError ? colors.destructive : colors.border,
                },
              ]}
              placeholder="Phone number *"
              placeholderTextColor={colors.mutedForeground}
              value={phone}
              onChangeText={(t) => {
                setPhone(t);
                setPhoneError(false);
              }}
              keyboardType="phone-pad"
              returnKeyType="done"
              onSubmitEditing={handleSave}
            />

            {lastService ? (
              <View style={[styles.serviceHint, { backgroundColor: colors.muted }]}>
                <Feather name="tag" size={13} color={colors.mutedForeground} />
                <Text style={[styles.hintText, { color: colors.mutedForeground }]}>
                  Service: {lastService} (auto-filled)
                </Text>
              </View>
            ) : null}

            <View style={styles.btnRow}>
              <Pressable
                onPress={onClose}
                style={[styles.cancelBtn, { borderColor: colors.border }]}
              >
                <Text style={[styles.cancelText, { color: colors.mutedForeground }]}>
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
                <Feather name="user-plus" size={17} color="#fff" />
                <Text style={styles.saveBtnText}>
                  {isSaving ? "Adding..." : "Add Client"}
                </Text>
              </Pressable>
            </View>
        </KeyboardAwareScrollView>
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
    shadowColor: "#000",
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.12,
    shadowRadius: 20,
    elevation: 20,
  },
  content: {
    padding: 24,
    gap: 14,
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
  input: {
    borderRadius: 14,
    borderWidth: 1.5,
    paddingHorizontal: 16,
    paddingVertical: 15,
    fontSize: 17,
  },
  serviceHint: {
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 10,
  },
  hintText: { fontSize: 12 },
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
