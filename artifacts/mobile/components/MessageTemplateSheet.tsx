import { Feather } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import React, { useEffect, useRef, useState } from "react";
import {
  Alert,
  Animated,
  Keyboard,
  Linking,
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
import { useColors } from "@/hooks/useColors";
import { useData, MessageTemplate } from "@/context/DataContext";
import { formatMessage } from "@/lib/messages";
import { useTranslation } from "react-i18next";

const USE_NATIVE_DRIVER = Platform.OS !== "web";

type SheetView = "list" | "preview" | "edit" | "new";

export interface TemplateVars {
  name?: string;
  phone: string;
  amount?: string;
  date?: string;
  service?: string;
}

interface MessageTemplateSheetProps {
  visible: boolean;
  onClose: () => void;
  vars: TemplateVars;
  suggestPayment?: boolean;
  suggestBooking?: boolean;
}

async function openWhatsApp(phone: string, message: string) {
  const cleaned = phone.replace(/\D/g, "");
  const native = `whatsapp://send?phone=${cleaned}&text=${encodeURIComponent(message)}`;
  const web = `https://wa.me/${cleaned}?text=${encodeURIComponent(message)}`;
  try {
    const ok = await Linking.canOpenURL(native);
    await Linking.openURL(ok ? native : web);
  } catch {
    await Linking.openURL(web);
  }
}

const EMOJI_OPTIONS = ["💳", "📅", "👋", "🧾", "📸", "🎉", "✅", "📩", "🔔", "💬"];

export function MessageTemplateSheet({
  visible,
  onClose,
  vars,
  suggestPayment = false,
  suggestBooking = false,
}: MessageTemplateSheetProps) {
  const { t } = useTranslation();
  const colors = useColors();
  const { templates, addTemplate, updateTemplate, deleteTemplate } = useData();
  const slideAnim = useRef(new Animated.Value(700)).current;

  const [view, setView] = useState<SheetView>("list");
  const [selected, setSelected] = useState<MessageTemplate | null>(null);
  const [editName, setEditName] = useState("");
  const [editContent, setEditContent] = useState("");
  const [editEmoji, setEditEmoji] = useState("💬");
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (visible) {
      setView("list");
      setSelected(null);
      Animated.spring(slideAnim, {
        toValue: 0,
        useNativeDriver: USE_NATIVE_DRIVER,
        damping: 22,
        stiffness: 200,
      }).start();
    } else {
      Animated.timing(slideAnim, {
        toValue: 700,
        duration: 200,
        useNativeDriver: USE_NATIVE_DRIVER,
      }).start();
    }
  }, [visible]);

  const handleSelectTemplate = (tpl: MessageTemplate) => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    setSelected(tpl);
    setView("preview");
  };

  const handleSend = async () => {
    if (!selected) return;
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    const message = formatMessage(selected.content, vars);
    await openWhatsApp(vars.phone, message);
    onClose();
  };

  const handleStartEdit = (tpl: MessageTemplate) => {
    setSelected(tpl);
    setEditName(tpl.name);
    setEditContent(tpl.content);
    setEditEmoji(tpl.emoji);
    setView("edit");
  };

  const handleStartNew = () => {
    setSelected(null);
    setEditName("");
    setEditContent("");
    setEditEmoji("💬");
    setView("new");
  };

  const handleSaveEdit = async () => {
    if (!editName.trim() || !editContent.trim()) return;
    setIsSaving(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    try {
      if (selected) {
        await updateTemplate(selected.id, {
          name: editName.trim(),
          content: editContent.trim(),
          emoji: editEmoji,
        });
      } else {
        await addTemplate({
          name: editName.trim(),
          content: editContent.trim(),
          emoji: editEmoji,
        });
      }
      setView("list");
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteTemplate = (tpl: MessageTemplate) => {
    Alert.alert(
      t("delete"),
      t("deleteClientConfirm", { name: tpl.name }),
      [
        { text: t("cancel"), style: "cancel" },
        {
          text: t("delete"),
          style: "destructive",
          onPress: async () => {
            await deleteTemplate(tpl.id);
            setView("list");
          },
        },
      ]
    );
  };

  const goBack = () => {
    if (view === "preview") setView("list");
    else if (view === "edit" || view === "new") {
      setView(selected ? "preview" : "list");
    }
  };

  const previewText = selected ? formatMessage(selected.content, vars) : "";

  const suggested = templates.filter((t) => {
    if (suggestPayment && t.id === "tpl_payment_reminder") return true;
    if (suggestBooking && t.id === "tpl_booking_confirm") return true;
    return false;
  });

  const rest = templates.filter(
    (t) => !suggested.find((s) => s.id === t.id)
  );

  if (!visible) return null;

  return (
    <Modal
      transparent
      visible={visible}
      animationType="none"
      statusBarTranslucent
      onRequestClose={view === "list" ? onClose : goBack}
    >
      <TouchableWithoutFeedback
        onPress={() => {
          if (view === "list") {
            Keyboard.dismiss();
            onClose();
          }
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
          },
        ]}
      >
        <KeyboardAwareScrollView
          bottomOffset={Platform.OS === "ios" ? 0 : 20}
          keyboardShouldPersistTaps="handled"
          bounces={false}
          showsVerticalScrollIndicator={false}
          contentContainerStyle={[
            { flexGrow: 1 },
            { paddingBottom: Platform.OS === "ios" ? 44 : 28 },
          ]}
          style={{ flex: 1 }}
        >
        {/* Handle + header */}
        <View style={[styles.handle, { backgroundColor: colors.border }]} />
        <View style={styles.sheetHeader}>
          {view !== "list" ? (
            <Pressable onPress={goBack} style={styles.backBtn} hitSlop={8}>
              <Feather name="arrow-left" size={20} color={colors.foreground} />
            </Pressable>
          ) : (
            <View style={styles.waChip}>
              <Feather name="message-circle" size={14} color="#25D366" />
              <Text style={styles.waChipText}>WhatsApp</Text>
            </View>
          )}
          <Text style={[styles.sheetTitle, { color: colors.foreground }]}>
            {view === "list"
              ? t("sendWhatsApp")
              : view === "preview"
              ? selected?.name ?? ""
              : view === "new"
              ? t("messageTemplates")
              : t("edit")}
          </Text>
          <Pressable onPress={onClose} hitSlop={8}>
            <Feather name="x" size={20} color={colors.mutedForeground} />
          </Pressable>
        </View>

        {/* ── LIST VIEW ── */}
        {view === "list" && (
          <ScrollView
            style={styles.listScroll}
            showsVerticalScrollIndicator={false}
            keyboardShouldPersistTaps="handled"
          >
            {/* Suggested */}
            {suggested.length > 0 && (
              <View style={styles.section}>
                <Text
                  style={[styles.sectionLabel, { color: colors.mutedForeground }]}
                >
                  Suggested for this client
                </Text>
                {suggested.map((tpl) => (
                  <TemplateRow
                    key={tpl.id}
                    tpl={tpl}
                    isSuggested
                    onSelect={() => handleSelectTemplate(tpl)}
                    onEdit={() => handleStartEdit(tpl)}
                    colors={colors}
                  />
                ))}
              </View>
            )}

            {/* All templates */}
            <View style={styles.section}>
              {suggested.length > 0 && (
                <Text
                  style={[styles.sectionLabel, { color: colors.mutedForeground }]}
                >
                  All templates
                </Text>
              )}
              {rest.map((tpl) => (
                <TemplateRow
                  key={tpl.id}
                  tpl={tpl}
                  isSuggested={false}
                  onSelect={() => handleSelectTemplate(tpl)}
                  onEdit={() => handleStartEdit(tpl)}
                  colors={colors}
                />
              ))}
            </View>

            {/* Add new */}
            <Pressable
              onPress={handleStartNew}
              style={[
                styles.newTemplateBtn,
                { borderColor: colors.border },
              ]}
            >
              <Feather name="plus" size={16} color={colors.primary} />
              <Text style={[styles.newTemplateTxt, { color: colors.primary }]}>
                New Template
              </Text>
            </Pressable>

            <View style={{ height: 8 }} />
          </ScrollView>
        )}

        {/* ── PREVIEW VIEW ── */}
        {view === "preview" && selected && (
          <View style={styles.previewContainer}>
            <View
              style={[
                styles.previewBubble,
                { backgroundColor: colors.muted, borderColor: colors.border },
              ]}
            >
              <Text style={styles.previewEmoji}>{selected.emoji}</Text>
              <Text style={[styles.previewText, { color: colors.foreground }]}>
                {previewText}
              </Text>
            </View>

            {/* Filled variables */}
            <View style={styles.varsRow}>
              {vars.name && (
                <VarChip label="name" value={vars.name} colors={colors} />
              )}
              {vars.amount && (
                <VarChip label="amount" value={vars.amount} colors={colors} />
              )}
              {vars.date && (
                <VarChip label="date" value={vars.date} colors={colors} />
              )}
              {vars.service && (
                <VarChip label="service" value={vars.service} colors={colors} />
              )}
            </View>

            <View style={styles.previewActions}>
              <Pressable
                onPress={() => handleStartEdit(selected)}
                style={[
                  styles.editBtn,
                  { borderColor: colors.border },
                ]}
              >
                <Feather name="edit-2" size={15} color={colors.mutedForeground} />
                <Text
                  style={[styles.editBtnText, { color: colors.mutedForeground }]}
                >
                  Edit
                </Text>
              </Pressable>
              <Pressable
                onPress={handleSend}
                style={styles.sendBtn}
              >
                <Feather name="send" size={16} color="#fff" />
                <Text style={styles.sendBtnText}>Send via WhatsApp</Text>
              </Pressable>
            </View>
          </View>
        )}

        {/* ── EDIT / NEW VIEW ── */}
        {(view === "edit" || view === "new") && (
          <ScrollView
            style={styles.editScroll}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            {/* Emoji picker */}
            <Text style={[styles.fieldLabel, { color: colors.mutedForeground }]}>
              Emoji
            </Text>
            <ScrollView
              horizontal
              showsHorizontalScrollIndicator={false}
              style={styles.emojiRow}
              contentContainerStyle={{ gap: 8 }}
            >
              {EMOJI_OPTIONS.map((e) => (
                <Pressable
                  key={e}
                  onPress={() => setEditEmoji(e)}
                  style={[
                    styles.emojiOption,
                    {
                      backgroundColor:
                        editEmoji === e
                          ? colors.primary + "22"
                          : colors.muted,
                      borderColor:
                        editEmoji === e ? colors.primary : "transparent",
                    },
                  ]}
                >
                  <Text style={styles.emojiText}>{e}</Text>
                </Pressable>
              ))}
            </ScrollView>

            {/* Name */}
            <Text style={[styles.fieldLabel, { color: colors.mutedForeground }]}>
              Template Name
            </Text>
            <TextInput
              style={[
                styles.fieldInput,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: colors.border,
                },
              ]}
              value={editName}
              onChangeText={setEditName}
              placeholder="e.g. Payment Reminder"
              placeholderTextColor={colors.mutedForeground}
              returnKeyType="next"
            />

            {/* Content */}
            <Text style={[styles.fieldLabel, { color: colors.mutedForeground }]}>
              Message
            </Text>
            <TextInput
              style={[
                styles.fieldInput,
                styles.fieldTextarea,
                {
                  backgroundColor: colors.muted,
                  color: colors.foreground,
                  borderColor: colors.border,
                },
              ]}
              value={editContent}
              onChangeText={setEditContent}
              placeholder="Hi {name}, your payment of {amount} is due..."
              placeholderTextColor={colors.mutedForeground}
              multiline
              textAlignVertical="top"
            />

            {/* Variable hints */}
            <View style={styles.varHints}>
              <Text style={[styles.varHintLabel, { color: colors.mutedForeground }]}>
                Variables:
              </Text>
              {["{name}", "{amount}", "{date}", "{service}"].map((v) => (
                <Pressable
                  key={v}
                  onPress={() => setEditContent((c) => c + v)}
                  style={[
                    styles.varHintChip,
                    { backgroundColor: colors.primary + "14" },
                  ]}
                >
                  <Text style={[styles.varHintText, { color: colors.primary }]}>
                    {v}
                  </Text>
                </Pressable>
              ))}
            </View>

            {/* Actions */}
            <View style={styles.editActions}>
              {view === "edit" && selected && !selected.isDefault && (
                <Pressable
                  onPress={() => handleDeleteTemplate(selected)}
                  style={[
                    styles.deleteBtn,
                    { borderColor: colors.destructive + "60" },
                  ]}
                >
                  <Feather name="trash-2" size={15} color={colors.destructive} />
                </Pressable>
              )}
              <Pressable
                onPress={handleSaveEdit}
                disabled={isSaving || !editName.trim() || !editContent.trim()}
                style={({ pressed }) => [
                  styles.saveEditBtn,
                  { backgroundColor: colors.primary },
                  pressed && { opacity: 0.85 },
                  (isSaving || !editName.trim() || !editContent.trim()) && {
                    opacity: 0.5,
                  },
                ]}
              >
                <Feather name="check" size={16} color="#fff" />
                <Text style={styles.saveEditBtnText}>
                  {isSaving ? "Saving..." : "Save Template"}
                </Text>
              </Pressable>
            </View>

            <View style={{ height: 20 }} />
          </ScrollView>
        )}
        </KeyboardAwareScrollView>
      </Animated.View>
    </Modal>
  );
}

function TemplateRow({
  tpl,
  isSuggested,
  onSelect,
  onEdit,
  colors,
}: {
  tpl: MessageTemplate;
  isSuggested: boolean;
  onSelect: () => void;
  onEdit: () => void;
  colors: ReturnType<typeof useColors>;
}) {
  return (
    <View
      style={[
        styles.tplRow,
        {
          backgroundColor: isSuggested
            ? colors.primary + "0A"
            : colors.muted + "80",
          borderColor: isSuggested ? colors.primary + "40" : colors.border,
        },
      ]}
    >
      <Pressable style={styles.tplMain} onPress={onSelect}>
        <Text style={styles.tplEmoji}>{tpl.emoji}</Text>
        <View style={styles.tplText}>
          <View style={styles.tplNameRow}>
            <Text style={[styles.tplName, { color: colors.foreground }]}>
              {tpl.name}
            </Text>
            {isSuggested && (
              <View
                style={[
                  styles.suggestPill,
                  { backgroundColor: colors.primary },
                ]}
              >
                <Text style={styles.suggestPillText}>Suggested</Text>
              </View>
            )}
          </View>
          <Text
            style={[styles.tplPreview, { color: colors.mutedForeground }]}
            numberOfLines={1}
          >
            {tpl.content}
          </Text>
        </View>
        <Feather
          name="chevron-right"
          size={16}
          color={colors.mutedForeground}
        />
      </Pressable>
      <Pressable onPress={onEdit} style={styles.tplEditBtn} hitSlop={8}>
        <Feather name="edit-2" size={14} color={colors.mutedForeground} />
      </Pressable>
    </View>
  );
}

function VarChip({
  label,
  value,
  colors,
}: {
  label: string;
  value: string;
  colors: ReturnType<typeof useColors>;
}) {
  return (
    <View
      style={[styles.varChip, { backgroundColor: colors.primary + "14" }]}
    >
      <Text style={[styles.varChipLabel, { color: colors.mutedForeground }]}>
        {label}:
      </Text>
      <Text style={[styles.varChipValue, { color: colors.primary }]}>
        {value}
      </Text>
    </View>
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
    maxHeight: "90%",
    borderTopLeftRadius: 26,
    borderTopRightRadius: 26,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.12,
    shadowRadius: 20,
    elevation: 20,
    paddingBottom: Platform.OS === "ios" ? 34 : 16,
  },
  handle: {
    width: 40,
    height: 4,
    borderRadius: 2,
    alignSelf: "center",
    marginTop: 12,
    marginBottom: 8,
  },
  sheetHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingHorizontal: 20,
    paddingBottom: 14,
  },
  backBtn: { padding: 2 },
  waChip: {
    flexDirection: "row",
    alignItems: "center",
    gap: 5,
    backgroundColor: "#25D36614",
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 20,
  },
  waChipText: { fontSize: 12, fontWeight: "700", color: "#25D366" },
  sheetTitle: { fontSize: 17, fontWeight: "800" },

  // List
  listScroll: { paddingHorizontal: 16 },
  section: { gap: 8, marginBottom: 16 },
  sectionLabel: { fontSize: 12, fontWeight: "600", letterSpacing: 0.4 },
  tplRow: {
    flexDirection: "row",
    alignItems: "center",
    borderRadius: 14,
    borderWidth: 1,
    overflow: "hidden",
  },
  tplMain: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    padding: 14,
    gap: 12,
  },
  tplEmoji: { fontSize: 24 },
  tplText: { flex: 1, gap: 2 },
  tplNameRow: { flexDirection: "row", alignItems: "center", gap: 8 },
  tplName: { fontSize: 14, fontWeight: "700" },
  tplPreview: { fontSize: 12, lineHeight: 16 },
  suggestPill: {
    paddingHorizontal: 7,
    paddingVertical: 2,
    borderRadius: 6,
  },
  suggestPillText: { fontSize: 9, fontWeight: "800", color: "#fff" },
  tplEditBtn: { paddingHorizontal: 14, paddingVertical: 18 },
  newTemplateBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 14,
    borderRadius: 14,
    borderWidth: 1.5,
    borderStyle: "dashed",
    gap: 8,
    marginBottom: 4,
  },
  newTemplateTxt: { fontSize: 14, fontWeight: "700" },

  // Preview
  previewContainer: {
    paddingHorizontal: 20,
    gap: 16,
    flex: 1,
  },
  previewBubble: {
    borderRadius: 16,
    borderWidth: 1,
    padding: 18,
    gap: 10,
  },
  previewEmoji: { fontSize: 28 },
  previewText: { fontSize: 15, lineHeight: 22 },
  varsRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 8,
  },
  varChip: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 8,
    gap: 4,
  },
  varChipLabel: { fontSize: 11 },
  varChipValue: { fontSize: 12, fontWeight: "700" },
  previewActions: {
    flexDirection: "row",
    gap: 10,
    marginTop: 4,
  },
  editBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 15,
    borderRadius: 14,
    borderWidth: 1,
    gap: 6,
    flex: 1,
  },
  editBtnText: { fontSize: 14, fontWeight: "600" },
  sendBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#25D366",
    paddingVertical: 15,
    borderRadius: 14,
    gap: 8,
    flex: 2,
  },
  sendBtnText: { fontSize: 15, fontWeight: "800", color: "#fff" },

  // Edit
  editScroll: { paddingHorizontal: 20 },
  fieldLabel: { fontSize: 12, fontWeight: "600", letterSpacing: 0.3, marginBottom: 6, marginTop: 14 },
  fieldInput: {
    borderRadius: 12,
    borderWidth: 1.5,
    paddingHorizontal: 14,
    paddingVertical: 13,
    fontSize: 15,
  },
  fieldTextarea: { minHeight: 120, paddingTop: 14 },
  emojiRow: { marginBottom: 4 },
  emojiOption: {
    width: 44,
    height: 44,
    borderRadius: 12,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1.5,
  },
  emojiText: { fontSize: 22 },
  varHints: {
    flexDirection: "row",
    flexWrap: "wrap",
    alignItems: "center",
    gap: 8,
    marginTop: 10,
  },
  varHintLabel: { fontSize: 12 },
  varHintChip: {
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 8,
  },
  varHintText: { fontSize: 12, fontWeight: "700" },
  editActions: {
    flexDirection: "row",
    gap: 10,
    marginTop: 20,
  },
  deleteBtn: {
    paddingHorizontal: 16,
    paddingVertical: 15,
    borderRadius: 14,
    borderWidth: 1,
    alignItems: "center",
    justifyContent: "center",
  },
  saveEditBtn: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 15,
    borderRadius: 14,
    gap: 8,
  },
  saveEditBtnText: { fontSize: 15, fontWeight: "700", color: "#fff" },
});
