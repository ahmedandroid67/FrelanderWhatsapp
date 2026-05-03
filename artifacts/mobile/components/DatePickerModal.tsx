import { Feather } from "@expo/vector-icons";
import React, { useEffect, useState } from "react";
import {
  Modal,
  Pressable,
  StyleSheet,
  Text,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import { useColors } from "@/hooks/useColors";

const DAYS_OF_WEEK = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
const MONTHS = [
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December",
];

function parseDateStr(str: string): Date {
  if (!str) return new Date();
  const parts = str.split("-").map(Number);
  if (parts.length !== 3 || parts.some(isNaN)) return new Date();
  return new Date(parts[0], parts[1] - 1, parts[2]);
}

function formatDate(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

interface Props {
  visible: boolean;
  value: string; // YYYY-MM-DD
  label?: string;
  onConfirm: (date: string) => void;
  onClose: () => void;
}

export function DatePickerModal({ visible, value, label, onConfirm, onClose }: Props) {
  const colors = useColors();
  const todayStr = formatDate(new Date());

  const [selected, setSelected] = useState<Date>(() => parseDateStr(value || todayStr));
  const [viewMonth, setViewMonth] = useState<Date>(() => {
    const d = parseDateStr(value || todayStr);
    return new Date(d.getFullYear(), d.getMonth(), 1);
  });

  useEffect(() => {
    if (visible) {
      const d = parseDateStr(value || todayStr);
      setSelected(d);
      setViewMonth(new Date(d.getFullYear(), d.getMonth(), 1));
    }
  }, [visible, value]);

  const prevMonth = () =>
    setViewMonth((v) => new Date(v.getFullYear(), v.getMonth() - 1, 1));
  const nextMonth = () =>
    setViewMonth((v) => new Date(v.getFullYear(), v.getMonth() + 1, 1));

  // Build day grid
  const firstDayOfWeek = new Date(viewMonth.getFullYear(), viewMonth.getMonth(), 1).getDay();
  const daysInMonth = new Date(viewMonth.getFullYear(), viewMonth.getMonth() + 1, 0).getDate();
  const cells: (number | null)[] = [
    ...Array(firstDayOfWeek).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];
  while (cells.length % 7 !== 0) cells.push(null);

  const selectedStr = formatDate(selected);

  const handleConfirm = () => {
    onConfirm(formatDate(selected));
    onClose();
  };

  const goToday = () => {
    const now = new Date();
    setSelected(now);
    setViewMonth(new Date(now.getFullYear(), now.getMonth(), 1));
  };

  if (!visible) return null;

  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.backdrop} />
      </TouchableWithoutFeedback>

      <View style={styles.centeredContainer}>
        <View style={[styles.picker, { backgroundColor: colors.card, borderColor: colors.border }]}>
          {/* Label */}
          {label ? (
            <Text style={[styles.pickerLabel, { color: colors.mutedForeground }]}>
              {label}
            </Text>
          ) : null}

          {/* Month Navigation */}
          <View style={styles.navRow}>
            <Pressable onPress={prevMonth} style={styles.navBtn} hitSlop={12}>
              <Feather name="chevron-left" size={20} color={colors.foreground} />
            </Pressable>
            <Text style={[styles.monthLabel, { color: colors.foreground }]}>
              {MONTHS[viewMonth.getMonth()]} {viewMonth.getFullYear()}
            </Text>
            <Pressable onPress={nextMonth} style={styles.navBtn} hitSlop={12}>
              <Feather name="chevron-right" size={20} color={colors.foreground} />
            </Pressable>
          </View>

          {/* Weekday Headers */}
          <View style={styles.weekRow}>
            {DAYS_OF_WEEK.map((d) => (
              <Text key={d} style={[styles.weekDay, { color: colors.mutedForeground }]}>
                {d}
              </Text>
            ))}
          </View>

          {/* Day Grid */}
          <View style={styles.grid}>
            {cells.map((day, i) => {
              if (!day) return <View key={`e-${i}`} style={styles.dayCell} />;
              const dateStr = formatDate(
                new Date(viewMonth.getFullYear(), viewMonth.getMonth(), day)
              );
              const isSelected = dateStr === selectedStr;
              const isToday = dateStr === todayStr;
              return (
                <Pressable
                  key={dateStr}
                  style={[
                    styles.dayCell,
                    isSelected && {
                      backgroundColor: colors.primary,
                      borderRadius: CELL_SIZE / 2,
                    },
                    !isSelected && isToday && {
                      borderWidth: 1.5,
                      borderColor: colors.primary,
                      borderRadius: CELL_SIZE / 2,
                    },
                  ]}
                  onPress={() =>
                    setSelected(
                      new Date(viewMonth.getFullYear(), viewMonth.getMonth(), day)
                    )
                  }
                >
                  <Text
                    style={[
                      styles.dayText,
                      {
                        color: isSelected
                          ? "#fff"
                          : isToday
                          ? colors.primary
                          : colors.foreground,
                        fontWeight: isSelected ? "700" : "400",
                      },
                    ]}
                  >
                    {day}
                  </Text>
                </Pressable>
              );
            })}
          </View>

          {/* Today shortcut */}
          <Pressable
            onPress={goToday}
            style={[styles.todayShortcut, { borderColor: colors.border }]}
          >
            <Feather name="calendar" size={13} color={colors.mutedForeground} />
            <Text style={[styles.todayText, { color: colors.mutedForeground }]}>
              Jump to today
            </Text>
          </Pressable>

          {/* Actions */}
          <View style={styles.actionRow}>
            <Pressable
              onPress={onClose}
              style={[styles.cancelBtn, { borderColor: colors.border }]}
            >
              <Text style={[styles.cancelText, { color: colors.mutedForeground }]}>
                Cancel
              </Text>
            </Pressable>
            <Pressable
              onPress={handleConfirm}
              style={[styles.confirmBtn, { backgroundColor: colors.primary }]}
            >
              <Text style={styles.confirmText}>Confirm</Text>
            </Pressable>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const CELL_SIZE = 38;

const styles = StyleSheet.create({
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: "rgba(0,0,0,0.52)",
  },
  centeredContainer: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    paddingHorizontal: 20,
  },
  picker: {
    width: "100%",
    borderRadius: 20,
    borderWidth: 1,
    padding: 20,
    gap: 14,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.18,
    shadowRadius: 24,
    elevation: 16,
  },
  pickerLabel: {
    fontSize: 12,
    fontWeight: "600",
    letterSpacing: 0.4,
    textTransform: "uppercase",
  },
  navRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  navBtn: { padding: 4 },
  monthLabel: { fontSize: 17, fontWeight: "700" },
  weekRow: {
    flexDirection: "row",
    justifyContent: "space-around",
  },
  weekDay: {
    width: CELL_SIZE,
    textAlign: "center",
    fontSize: 11,
    fontWeight: "600",
  },
  grid: {
    flexDirection: "row",
    flexWrap: "wrap",
    justifyContent: "space-around",
    rowGap: 4,
  },
  dayCell: {
    width: CELL_SIZE,
    height: CELL_SIZE,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 1,
  },
  dayText: { fontSize: 14 },
  todayShortcut: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 6,
    paddingVertical: 8,
    borderRadius: 10,
    borderWidth: 1,
  },
  todayText: { fontSize: 13, fontWeight: "600" },
  actionRow: { flexDirection: "row", gap: 10, marginTop: 2 },
  cancelBtn: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: "center",
    borderWidth: 1,
  },
  cancelText: { fontSize: 15, fontWeight: "600" },
  confirmBtn: {
    flex: 2,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: "center",
  },
  confirmText: { fontSize: 15, fontWeight: "700", color: "#fff" },
});
