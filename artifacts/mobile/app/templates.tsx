import { Feather } from "@expo/vector-icons";
import { router } from "expo-router";
import React, { useState } from "react";
import {
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
  Alert,
} from "react-native";
import { KeyboardAwareScrollView } from "react-native-keyboard-controller";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useData, MessageTemplate } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";
import { useTranslation } from "react-i18next";
import { formatMessage } from "@/lib/messages";

export default function TemplatesScreen() {
  const { t } = useTranslation();
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { templates, updateTemplate, deleteTemplate, addTemplate, resetTemplates } = useData();

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [editContent, setEditContent] = useState("");
  const [showNew, setShowNew] = useState(false);

  const topPad = Platform.OS === "web" ? 67 : 0;

  const startEdit = (tpl: MessageTemplate) => {
    setEditingId(tpl.id);
    setEditName(tpl.name);
    setEditContent(tpl.content);
    setShowNew(false);
  };

  const handleSave = async () => {
    if (!editName.trim() || !editContent.trim()) return;
    if (editingId) {
      await updateTemplate(editingId, { name: editName, content: editContent });
      setEditingId(null);
    } else {
      await addTemplate({ name: editName, content: editContent, emoji: "💬" });
      setShowNew(false);
    }
    setEditName("");
    setEditContent("");
  };

  const handleDelete = (tpl: MessageTemplate) => {
    Alert.alert(t("delete"), t("deleteClientConfirm", { name: tpl.name }), [
      { text: t("cancel"), style: "cancel" },
      {
        text: t("delete"),
        style: "destructive",
        onPress: async () => {
          await deleteTemplate(tpl.id);
        },
      },
    ]);
  };

  const renderEditForm = () => (
    <View style={[styles.editForm, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <Text style={[styles.fieldLabel, { color: colors.mutedForeground }]}>{t("name")}</Text>
      <TextInput
        style={[styles.input, { backgroundColor: colors.muted, color: colors.foreground, borderColor: colors.border }]}
        value={editName}
        onChangeText={setEditName}
        placeholder="Template Name"
        placeholderTextColor={colors.mutedForeground}
      />
      <Text style={[styles.fieldLabel, { color: colors.mutedForeground, marginTop: 12 }]}>{t("messageTemplates")}</Text>
      <TextInput
        style={[styles.input, styles.textarea, { backgroundColor: colors.muted, color: colors.foreground, borderColor: colors.border }]}
        value={editContent}
        onChangeText={setEditContent}
        multiline
        placeholder="Message content..."
        placeholderTextColor={colors.mutedForeground}
      />
      <Text style={[styles.previewLabel, { color: colors.mutedForeground, marginTop: 12 }]}>{t("preview")}:</Text>
      <View style={[styles.previewBox, { backgroundColor: colors.background }]}>
        <Text style={[styles.previewText, { color: colors.foreground }]}>
          {formatMessage(editContent, { name: "John", service: "Photo", amount: "$100", date: "Today" })}
        </Text>
      </View>
      <View style={styles.formActions}>
        <Pressable onPress={() => { setEditingId(null); setShowNew(false); }} style={styles.cancelBtn}>
          <Text style={{ color: colors.mutedForeground }}>{t("cancel")}</Text>
        </Pressable>
        <Pressable onPress={handleSave} style={[styles.saveBtn, { backgroundColor: colors.primary }]}>
          <Text style={{ color: "#fff", fontWeight: "bold" }}>{t("save")}</Text>
        </Pressable>
      </View>
    </View>
  );

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <KeyboardAwareScrollView
        style={styles.container}
        contentContainerStyle={[
          styles.content,
          { paddingTop: topPad + 16, paddingBottom: insets.bottom + 40 },
        ]}
      >
        <View style={[styles.infoBox, { backgroundColor: colors.primary + "12", borderColor: colors.primary + "30" }]}>
          <Feather name="info" size={16} color={colors.primary} />
          <Text style={[styles.infoText, { color: colors.foreground }]}>
            {t("variableGuide")}
          </Text>
        </View>

        {(editingId || showNew) ? renderEditForm() : (
          <Pressable 
            onPress={() => setShowNew(true)}
            style={[styles.addBtn, { borderColor: colors.primary }]}
          >
            <Feather name="plus" size={18} color={colors.primary} />
            <Text style={{ color: colors.primary, fontWeight: "700" }}>{t("messageTemplates")}</Text>
          </Pressable>
        )}

        {templates.map((tpl) => (
          <View key={tpl.id} style={[styles.tplCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <View style={styles.tplHeader}>
              <View style={styles.tplTitleRow}>
                <Text style={styles.tplEmoji}>{tpl.emoji}</Text>
                <Text style={[styles.tplName, { color: colors.foreground }]}>{tpl.name}</Text>
              </View>
              <View style={styles.tplActions}>
                <Pressable onPress={() => startEdit(tpl)} style={styles.actionIcon}>
                  <Feather name="edit-2" size={16} color={colors.mutedForeground} />
                </Pressable>
                {!tpl.isDefault && (
                  <Pressable onPress={() => handleDelete(tpl)} style={styles.actionIcon}>
                    <Feather name="trash-2" size={16} color={colors.destructive} />
                  </Pressable>
                )}
              </View>
            </View>
            <Text style={[styles.tplContent, { color: colors.mutedForeground }]} numberOfLines={2}>
              {tpl.content}
            </Text>
          </View>
        ))}

        <Pressable
          onPress={() => {
            Alert.alert(t("resetToDefault"), "", [
              { text: t("cancel"), style: "cancel" },
              { text: t("reset"), style: "destructive", onPress: resetTemplates }
            ]);
          }}
          style={styles.resetBtn}
        >
          <Text style={{ color: colors.mutedForeground, fontSize: 13 }}>{t("resetToDefault")}</Text>
        </Pressable>
      </KeyboardAwareScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: 16, gap: 16 },
  infoBox: {
    flexDirection: "row",
    padding: 14,
    borderRadius: 12,
    borderWidth: 1,
    gap: 12,
    alignItems: "center",
    marginBottom: 8,
  },
  infoText: { fontSize: 13, flex: 1 },
  addBtn: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    padding: 16,
    borderRadius: 12,
    borderWidth: 1.5,
    borderStyle: "dashed",
    gap: 8,
  },
  tplCard: {
    borderRadius: 16,
    borderWidth: 1,
    padding: 16,
    gap: 8,
  },
  tplHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  tplTitleRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
  },
  tplEmoji: { fontSize: 20 },
  tplName: { fontSize: 15, fontWeight: "700" },
  tplActions: { flexDirection: "row", gap: 12 },
  actionIcon: { padding: 4 },
  tplContent: { fontSize: 13, lineHeight: 18 },
  editForm: {
    borderRadius: 16,
    borderWidth: 1,
    padding: 16,
    gap: 4,
  },
  fieldLabel: { fontSize: 12, fontWeight: "600" },
  input: {
    borderRadius: 10,
    borderWidth: 1,
    padding: 12,
    fontSize: 14,
    marginTop: 4,
  },
  textarea: { minHeight: 100, textAlignVertical: "top" },
  previewLabel: { fontSize: 11, fontWeight: "700", textTransform: "uppercase" },
  previewBox: { padding: 12, borderRadius: 8, marginTop: 4 },
  previewText: { fontSize: 13, fontStyle: "italic" },
  formActions: {
    flexDirection: "row",
    justifyContent: "flex-end",
    gap: 12,
    marginTop: 16,
  },
  cancelBtn: { padding: 12 },
  saveBtn: {
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 10,
  },
  resetBtn: {
    alignItems: "center",
    marginTop: 20,
  }
});
