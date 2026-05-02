import { Feather } from "@expo/vector-icons";
import React, { useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAuth } from "@/context/AuthContext";
import { useColors } from "@/hooks/useColors";
import { PinPad } from "./PinPad";

type Step = "enter" | "confirm";

export function SetupPinScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { setupPin } = useAuth();
  const [step, setStep] = useState<Step>("enter");
  const [firstPin, setFirstPin] = useState("");
  const [pin, setPin] = useState("");
  const [error, setError] = useState(false);

  const handleFirstSubmit = () => {
    if (pin.length < 4) return;
    setFirstPin(pin);
    setPin("");
    setStep("confirm");
  };

  const handleConfirmSubmit = async () => {
    if (pin === firstPin) {
      await setupPin(pin);
    } else {
      setError(true);
      setPin("");
      setTimeout(() => {
        setError(false);
        setStep("enter");
        setFirstPin("");
      }, 700);
    }
  };

  return (
    <View
      style={[
        styles.container,
        {
          backgroundColor: colors.background,
          paddingTop: insets.top + 40,
          paddingBottom: insets.bottom + 24,
        },
      ]}
    >
      {/* Header */}
      <View style={styles.header}>
        <View style={[styles.logoCircle, { backgroundColor: colors.primary }]}>
          <Feather name="shield" size={28} color="#fff" />
        </View>
        <Text style={[styles.title, { color: colors.foreground }]}>
          {step === "enter" ? "Set Your PIN" : "Confirm PIN"}
        </Text>
        <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>
          {step === "enter"
            ? "Choose a 6-digit PIN to protect your data"
            : "Re-enter your PIN to confirm"}
        </Text>

        {/* Progress dots */}
        <View style={styles.steps}>
          {(["enter", "confirm"] as Step[]).map((s, i) => (
            <View
              key={s}
              style={[
                styles.stepDot,
                {
                  backgroundColor:
                    s === step
                      ? colors.primary
                      : step === "confirm" && i === 0
                      ? colors.primary + "60"
                      : colors.muted,
                },
              ]}
            />
          ))}
        </View>
      </View>

      {/* PIN Pad */}
      <View style={styles.padWrap}>
        <PinPad
          value={pin}
          onChange={(v) => {
            setPin(v);
            setError(false);
          }}
          onSubmit={step === "enter" ? handleFirstSubmit : handleConfirmSubmit}
          error={error}
        />
      </View>

      {error && (
        <Text style={[styles.errorText, { color: colors.destructive }]}>
          PINs don't match — try again
        </Text>
      )}

      {/* Security note */}
      <View style={[styles.noteBox, { backgroundColor: colors.muted, borderColor: colors.border }]}>
        <Feather name="lock" size={14} color={colors.mutedForeground} />
        <Text style={[styles.noteText, { color: colors.mutedForeground }]}>
          Your PIN is stored securely on this device and never leaves it.
        </Text>
      </View>
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
  header: { alignItems: "center", gap: 10 },
  logoCircle: {
    width: 68,
    height: 68,
    borderRadius: 34,
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#2563EB",
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 8,
    marginBottom: 4,
  },
  title: { fontSize: 26, fontWeight: "800" },
  subtitle: { fontSize: 14, textAlign: "center", lineHeight: 20 },
  steps: { flexDirection: "row", gap: 8, marginTop: 8 },
  stepDot: { width: 28, height: 6, borderRadius: 3 },
  padWrap: { flex: 1, justifyContent: "center", width: "100%" },
  errorText: { fontSize: 14, fontWeight: "600" },
  noteBox: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: 8,
    padding: 14,
    borderRadius: 12,
    borderWidth: 1,
    width: "100%",
  },
  noteText: { flex: 1, fontSize: 12, lineHeight: 18 },
});
