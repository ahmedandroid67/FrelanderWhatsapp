import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { router } from "expo-router";
import React, { useState } from "react";
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
import { PinPad } from "@/components/PinPad";
import { useAuth } from "@/context/AuthContext";
import { useColors } from "@/hooks/useColors";

type Mode = "menu" | "change-old" | "change-new" | "change-confirm";

export default function SecurityScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { lock, clearAuth, unlockWithPin, setupPin, hasBiometrics } = useAuth();
  const [mode, setMode] = useState<Mode>("menu");
  const [pin, setPin] = useState("");
  const [newPin, setNewPin] = useState("");
  const [error, setError] = useState(false);

  const topPad = Platform.OS === "web" ? 67 : 0;

  const handleLock = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    lock();
    router.replace("/(tabs)");
  };

  const handleResetPin = () => {
    Alert.alert(
      "Reset PIN",
      "This will remove your PIN and require you to set a new one next time you open the app.",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Reset",
          style: "destructive",
          onPress: async () => {
            await clearAuth();
            router.replace("/(tabs)");
          },
        },
      ]
    );
  };

  const handleOldPinSubmit = async () => {
    const ok = await unlockWithPin(pin);
    if (ok) {
      setPin("");
      setMode("change-new");
    } else {
      setError(true);
      setPin("");
      setTimeout(() => setError(false), 600);
    }
  };

  const handleNewPinSubmit = () => {
    setNewPin(pin);
    setPin("");
    setMode("change-confirm");
  };

  const handleConfirmSubmit = async () => {
    if (pin === newPin) {
      await setupPin(pin);
      setPin("");
      setMode("menu");
      Alert.alert("Success", "Your PIN has been updated.");
    } else {
      setError(true);
      setPin("");
      setTimeout(() => {
        setError(false);
        setMode("change-new");
        setNewPin("");
      }, 700);
    }
  };

  const cancelChange = () => {
    setPin("");
    setNewPin("");
    setMode("menu");
  };

  if (mode !== "menu") {
    const titles: Record<Mode, string> = {
      menu: "",
      "change-old": "Enter current PIN",
      "change-new": "Enter new PIN",
      "change-confirm": "Confirm new PIN",
    };
    const subtitles: Record<Mode, string> = {
      menu: "",
      "change-old": "Verify your identity first",
      "change-new": "Choose a new 6-digit PIN",
      "change-confirm": "Re-enter your new PIN",
    };
    const onSubmit =
      mode === "change-old"
        ? handleOldPinSubmit
        : mode === "change-new"
        ? handleNewPinSubmit
        : handleConfirmSubmit;

    return (
      <View
        style={[
          styles.pinContainer,
          {
            backgroundColor: colors.background,
            paddingTop: insets.top + topPad + 20,
            paddingBottom: insets.bottom + 24,
          },
        ]}
      >
        <View style={styles.pinHeader}>
          <Text style={[styles.pinTitle, { color: colors.foreground }]}>
            {titles[mode]}
          </Text>
          <Text style={[styles.pinSubtitle, { color: colors.mutedForeground }]}>
            {subtitles[mode]}
          </Text>
        </View>

        <View style={styles.padWrap}>
          <PinPad
            value={pin}
            onChange={(v) => {
              setPin(v);
              setError(false);
            }}
            onSubmit={onSubmit}
            error={error}
          />
        </View>

        <Pressable onPress={cancelChange} style={styles.cancelBtn}>
          <Text style={[styles.cancelText, { color: colors.mutedForeground }]}>
            Cancel
          </Text>
        </Pressable>
      </View>
    );
  }

  return (
    <ScrollView
      style={[styles.container, { backgroundColor: colors.background }]}
      contentContainerStyle={[
        styles.content,
        { paddingTop: topPad + 16, paddingBottom: insets.bottom + 24 },
      ]}
    >
      {/* Header */}
      <View
        style={[
          styles.headerCard,
          { backgroundColor: colors.primary + "12", borderColor: colors.primary + "30" },
        ]}
      >
        <View style={[styles.shieldCircle, { backgroundColor: colors.primary }]}>
          <Feather name="shield" size={26} color="#fff" />
        </View>
        <View style={styles.headerText}>
          <Text style={[styles.headerTitle, { color: colors.foreground }]}>
            App Security
          </Text>
          <Text style={[styles.headerSub, { color: colors.mutedForeground }]}>
            Your data is protected with a PIN
            {hasBiometrics ? " and biometrics" : ""}.
          </Text>
        </View>
      </View>

      {/* Options */}
      <View
        style={[
          styles.section,
          { backgroundColor: colors.card, borderColor: colors.border },
        ]}
      >
        <SettingRow
          icon="refresh-cw"
          label="Change PIN"
          subtitle="Update your 6-digit PIN"
          onPress={() => {
            setPin("");
            setMode("change-old");
          }}
          colors={colors}
          iconColor={colors.primary}
        />

        <View style={[styles.divider, { backgroundColor: colors.border }]} />

        <SettingRow
          icon="lock"
          label="Lock App Now"
          subtitle="Require PIN to re-open"
          onPress={handleLock}
          colors={colors}
          iconColor="#8B5CF6"
        />

        <View style={[styles.divider, { backgroundColor: colors.border }]} />

        <SettingRow
          icon="trash-2"
          label="Reset PIN"
          subtitle="Remove PIN — you'll need to set a new one"
          onPress={handleResetPin}
          colors={colors}
          iconColor={colors.destructive}
          danger
        />
      </View>

      {/* Info box */}
      <View
        style={[
          styles.infoBox,
          { backgroundColor: colors.muted, borderColor: colors.border },
        ]}
      >
        <Feather name="info" size={14} color={colors.mutedForeground} />
        <Text style={[styles.infoText, { color: colors.mutedForeground }]}>
          The app locks automatically when you switch away from it. Your PIN is
          stored securely on this device using encrypted hardware storage and
          is never transmitted anywhere.
        </Text>
      </View>
    </ScrollView>
  );
}

function SettingRow({
  icon,
  label,
  subtitle,
  onPress,
  colors,
  iconColor,
  danger = false,
}: {
  icon: React.ComponentProps<typeof Feather>["name"];
  label: string;
  subtitle: string;
  onPress: () => void;
  colors: ReturnType<typeof useColors>;
  iconColor: string;
  danger?: boolean;
}) {
  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        styles.settingRow,
        pressed && { opacity: 0.7 },
      ]}
    >
      <View style={[styles.settingIcon, { backgroundColor: iconColor + "18" }]}>
        <Feather name={icon} size={18} color={iconColor} />
      </View>
      <View style={styles.settingText}>
        <Text
          style={[
            styles.settingLabel,
            { color: danger ? colors.destructive : colors.foreground },
          ]}
        >
          {label}
        </Text>
        <Text style={[styles.settingSubtitle, { color: colors.mutedForeground }]}>
          {subtitle}
        </Text>
      </View>
      <Feather name="chevron-right" size={16} color={colors.mutedForeground} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 16, gap: 14 },
  headerCard: {
    flexDirection: "row",
    alignItems: "center",
    gap: 14,
    padding: 16,
    borderRadius: 16,
    borderWidth: 1,
  },
  shieldCircle: {
    width: 54,
    height: 54,
    borderRadius: 27,
    alignItems: "center",
    justifyContent: "center",
  },
  headerText: { flex: 1, gap: 3 },
  headerTitle: { fontSize: 17, fontWeight: "700" },
  headerSub: { fontSize: 13, lineHeight: 18 },
  section: { borderRadius: 16, borderWidth: 1, overflow: "hidden" },
  settingRow: {
    flexDirection: "row",
    alignItems: "center",
    padding: 16,
    gap: 14,
  },
  settingIcon: {
    width: 40,
    height: 40,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
  },
  settingText: { flex: 1, gap: 2 },
  settingLabel: { fontSize: 15, fontWeight: "600" },
  settingSubtitle: { fontSize: 13 },
  divider: { height: 1, marginLeft: 70 },
  infoBox: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: 10,
    padding: 14,
    borderRadius: 12,
    borderWidth: 1,
  },
  infoText: { flex: 1, fontSize: 12, lineHeight: 18 },
  pinContainer: {
    flex: 1,
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 32,
  },
  pinHeader: { alignItems: "center", gap: 8, marginTop: 20 },
  pinTitle: { fontSize: 22, fontWeight: "800" },
  pinSubtitle: { fontSize: 14, textAlign: "center" },
  padWrap: { flex: 1, justifyContent: "center", width: "100%" },
  cancelBtn: { paddingVertical: 14 },
  cancelText: { fontSize: 16, fontWeight: "600" },
});
