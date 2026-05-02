import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import React, { useEffect, useRef } from "react";
import {
  Animated,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useColors } from "@/hooks/useColors";

interface PinPadProps {
  value: string;
  onChange: (v: string) => void;
  maxLength?: number;
  onSubmit?: () => void;
  error?: boolean;
}

const KEYS = [
  ["1", "2", "3"],
  ["4", "5", "6"],
  ["7", "8", "9"],
  ["", "0", "del"],
];

export function PinPad({
  value,
  onChange,
  maxLength = 6,
  onSubmit,
  error = false,
}: PinPadProps) {
  const colors = useColors();
  const shakeAnim = useRef(new Animated.Value(0)).current;
  const dotAnims = useRef(
    Array.from({ length: maxLength }, () => new Animated.Value(0))
  ).current;

  useEffect(() => {
    if (error) {
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Error);
      Animated.sequence([
        Animated.timing(shakeAnim, { toValue: 10, duration: 60, useNativeDriver: Platform.OS !== "web" }),
        Animated.timing(shakeAnim, { toValue: -10, duration: 60, useNativeDriver: Platform.OS !== "web" }),
        Animated.timing(shakeAnim, { toValue: 8, duration: 60, useNativeDriver: Platform.OS !== "web" }),
        Animated.timing(shakeAnim, { toValue: -8, duration: 60, useNativeDriver: Platform.OS !== "web" }),
        Animated.timing(shakeAnim, { toValue: 0, duration: 60, useNativeDriver: Platform.OS !== "web" }),
      ]).start();
    }
  }, [error]);

  useEffect(() => {
    const idx = value.length - 1;
    if (idx >= 0 && idx < maxLength) {
      Animated.sequence([
        Animated.timing(dotAnims[idx], {
          toValue: 1,
          duration: 80,
          useNativeDriver: Platform.OS !== "web",
        }),
      ]).start();
    }
    if (value.length === 0) {
      dotAnims.forEach((a) => a.setValue(0));
    }
  }, [value]);

  const handleKey = (key: string) => {
    if (key === "del") {
      Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
      onChange(value.slice(0, -1));
    } else if (key === "") {
      return;
    } else if (value.length < maxLength) {
      Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
      const next = value + key;
      onChange(next);
      if (next.length === maxLength) {
        setTimeout(() => onSubmit?.(), 80);
      }
    }
  };

  return (
    <View style={styles.container}>
      {/* PIN dots */}
      <Animated.View
        style={[
          styles.dotsRow,
          { transform: [{ translateX: shakeAnim }] },
        ]}
      >
        {Array.from({ length: maxLength }).map((_, i) => {
          const filled = i < value.length;
          return (
            <Animated.View
              key={i}
              style={[
                styles.dot,
                {
                  backgroundColor: filled
                    ? error
                      ? colors.destructive
                      : colors.primary
                    : "transparent",
                  borderColor: error
                    ? colors.destructive
                    : filled
                    ? colors.primary
                    : colors.border,
                  transform: filled
                    ? [
                        {
                          scale: dotAnims[i].interpolate({
                            inputRange: [0, 1],
                            outputRange: [1, 1.25],
                          }),
                        },
                      ]
                    : [],
                },
              ]}
            />
          );
        })}
      </Animated.View>

      {/* Keypad */}
      <View style={styles.keypad}>
        {KEYS.map((row, ri) => (
          <View key={ri} style={styles.row}>
            {row.map((key, ki) => (
              <Pressable
                key={ki}
                onPress={() => handleKey(key)}
                disabled={key === ""}
                style={({ pressed }) => [
                  styles.key,
                  {
                    backgroundColor:
                      key === ""
                        ? "transparent"
                        : key === "del"
                        ? pressed
                          ? colors.destructive + "22"
                          : colors.border
                        : pressed
                        ? colors.primary + "18"
                        : colors.muted,
                    borderColor: key === "" ? "transparent" : key === "del" ? colors.destructive + "55" : colors.border,
                  },
                ]}
              >
                {key === "del" ? (
                  <Feather
                    name="delete"
                    size={24}
                    color={colors.destructive}
                  />
                ) : (
                  <Text style={[styles.keyText, { color: colors.foreground }]}>
                    {key}
                  </Text>
                )}
              </Pressable>
            ))}
          </View>
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { alignItems: "center", gap: 32 },
  dotsRow: { flexDirection: "row", gap: 16 },
  dot: {
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 2,
  },
  keypad: { gap: 12, width: "100%" },
  row: { flexDirection: "row", justifyContent: "center", gap: 12 },
  key: {
    width: 80,
    height: 80,
    borderRadius: 40,
    borderWidth: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  keyText: { fontSize: 26, fontWeight: "500" },
});
