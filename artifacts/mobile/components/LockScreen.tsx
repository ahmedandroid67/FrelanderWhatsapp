import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import React, { useEffect, useState } from "react";
import {
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAuth } from "@/context/AuthContext";
import { useColors } from "@/hooks/useColors";
import { PinPad } from "./PinPad";

export function LockScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { unlockWithPin, unlockWithBiometrics, hasBiometrics } = useAuth();
  const [pin, setPin] = useState("");
  const [error, setError] = useState(false);
  const [attempts, setAttempts] = useState(0);
  const [lockedOut, setLockedOut] = useState(false);
  const [lockoutSeconds, setLockoutSeconds] = useState(0);

  useEffect(() => {
    if (hasBiometrics && Platform.OS !== "web") {
      setTimeout(() => tryBiometrics(), 400);
    }
  }, [hasBiometrics]);

  useEffect(() => {
    if (lockedOut && lockoutSeconds > 0) {
      const t = setTimeout(() => {
        setLockoutSeconds((s) => {
          if (s <= 1) {
            setLockedOut(false);
            setAttempts(0);
            return 0;
          }
          return s - 1;
        });
      }, 1000);
      return () => clearTimeout(t);
    }
  }, [lockedOut, lockoutSeconds]);

  const tryBiometrics = async () => {
    await unlockWithBiometrics();
  };

  const handlePinSubmit = async () => {
    if (lockedOut) return;
    const ok = await unlockWithPin(pin);
    if (ok) {
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    } else {
      const next = attempts + 1;
      setAttempts(next);
      setError(true);
      setPin("");
      setTimeout(() => setError(false), 600);
      if (next >= 5) {
        setLockedOut(true);
        setLockoutSeconds(30);
      }
    }
  };

  return (
    <View
      style={[
        styles.container,
        {
          backgroundColor: colors.background,
          paddingTop: insets.top + 40,
          paddingBottom: insets.bottom + 20,
        },
      ]}
    >
      {/* Brand */}
      <View style={styles.brand}>
        <View style={[styles.logoCircle, { backgroundColor: colors.primary }]}>
          <Feather name="briefcase" size={28} color="#fff" />
        </View>
        <Text style={[styles.appName, { color: colors.foreground }]}>
          ClientFlow
        </Text>
        <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
          {lockedOut
            ? `Too many attempts — wait ${lockoutSeconds}s`
            : "Enter your PIN to continue"}
        </Text>
      </View>

      {/* PIN pad */}
      <View style={styles.padWrap}>
        <PinPad
          value={pin}
          onChange={setPin}
          onSubmit={handlePinSubmit}
          error={error}
        />
      </View>

      {/* Biometrics button */}
      {hasBiometrics && Platform.OS !== "web" && !lockedOut && (
        <Pressable
          onPress={tryBiometrics}
          style={({ pressed }) => [
            styles.bioBtn,
            { borderColor: colors.border },
            pressed && { opacity: 0.7 },
          ]}
        >
          <Feather name="cpu" size={20} color={colors.primary} />
          <Text style={[styles.bioBtnText, { color: colors.primary }]}>
            Use Face ID / Fingerprint
          </Text>
        </Pressable>
      )}

      {/* Attempt counter */}
      {attempts > 0 && !lockedOut && (
        <Text style={[styles.attemptsText, { color: colors.destructive }]}>
          {5 - attempts} attempt{5 - attempts !== 1 ? "s" : ""} remaining
        </Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 32,
  },
  brand: { alignItems: "center", gap: 12 },
  logoCircle: {
    width: 72,
    height: 72,
    borderRadius: 36,
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#2563EB",
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 8,
  },
  appName: { fontSize: 26, fontWeight: "800" },
  subtitle: { fontSize: 15, textAlign: "center" },
  padWrap: { flex: 1, justifyContent: "center", width: "100%" },
  bioBtn: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
    paddingHorizontal: 24,
    paddingVertical: 14,
    borderRadius: 16,
    borderWidth: 1,
    marginBottom: 8,
  },
  bioBtnText: { fontSize: 15, fontWeight: "600" },
  attemptsText: { fontSize: 13, fontWeight: "600", marginBottom: 8 },
});
