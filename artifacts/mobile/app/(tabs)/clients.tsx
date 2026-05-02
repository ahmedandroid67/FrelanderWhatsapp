import { Feather } from "@expo/vector-icons";
import { router } from "expo-router";
import React, { useMemo, useState } from "react";
import {
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { ClientCard } from "@/components/ClientCard";
import { EmptyState } from "@/components/EmptyState";
import type { ClientStatus } from "@/context/DataContext";
import { useData } from "@/context/DataContext";
import { useColors } from "@/hooks/useColors";

const FILTERS: (ClientStatus | "All")[] = [
  "All",
  "Lead",
  "Quoted",
  "Booked",
  "Completed",
  "Paid",
];

export default function ClientsScreen() {
  const colors = useColors();
  const insets = useSafeAreaInsets();
  const { clients, payments } = useData();
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<ClientStatus | "All">("All");

  const topPad = Platform.OS === "web" ? 67 : 0;
  const botPad = Platform.OS === "web" ? 34 : 0;

  const paymentMap = useMemo(() => {
    const map: Record<string, "unpaid" | "partial" | "paid"> = {};
    payments.forEach((p) => {
      map[p.clientId] = p.status;
    });
    return map;
  }, [payments]);

  const filtered = useMemo(
    () =>
      clients
        .filter((c) => {
          const q = search.toLowerCase();
          const matchSearch =
            c.name.toLowerCase().includes(q) ||
            c.phone.includes(q) ||
            c.serviceType.toLowerCase().includes(q);
          const matchFilter = filter === "All" || c.status === filter;
          return matchSearch && matchFilter;
        })
        .sort(
          (a, b) =>
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        ),
    [clients, search, filter]
  );

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <View
        style={[
          styles.header,
          {
            paddingTop: topPad + 12,
            borderBottomColor: colors.border,
          },
        ]}
      >
        <View
          style={[
            styles.searchBar,
            { backgroundColor: colors.muted, borderColor: colors.border },
          ]}
        >
          <Feather name="search" size={16} color={colors.mutedForeground} />
          <TextInput
            style={[styles.searchInput, { color: colors.foreground }]}
            placeholder="Search clients..."
            placeholderTextColor={colors.mutedForeground}
            value={search}
            onChangeText={setSearch}
          />
          {search.length > 0 && (
            <Pressable onPress={() => setSearch("")}>
              <Feather name="x" size={16} color={colors.mutedForeground} />
            </Pressable>
          )}
        </View>
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          style={styles.filterRow}
          contentContainerStyle={styles.filterContent}
        >
          {FILTERS.map((s) => (
            <Pressable
              key={s}
              onPress={() => setFilter(s)}
              style={[
                styles.filterChip,
                {
                  backgroundColor:
                    filter === s ? colors.primary : colors.muted,
                  borderColor:
                    filter === s ? colors.primary : colors.border,
                },
              ]}
            >
              <Text
                style={[
                  styles.filterText,
                  {
                    color:
                      filter === s ? "#fff" : colors.mutedForeground,
                  },
                ]}
              >
                {s}
              </Text>
            </Pressable>
          ))}
        </ScrollView>
      </View>

      <ScrollView
        style={styles.list}
        contentContainerStyle={[
          styles.listContent,
          { paddingBottom: insets.bottom + botPad + 100 },
        ]}
        showsVerticalScrollIndicator={false}
      >
        {filtered.length === 0 ? (
          <EmptyState
            icon="users"
            title={search ? "No clients found" : "No clients yet"}
            subtitle={
              search
                ? "Try a different search term"
                : "Tap + to add your first client"
            }
          />
        ) : (
          filtered.map((client) => (
            <ClientCard
              key={client.id}
              client={client}
              paymentStatus={paymentMap[client.id]}
            />
          ))
        )}
      </ScrollView>

      <Pressable
        onPress={() => router.push("/client/form")}
        style={({ pressed }) => [
          styles.fab,
          {
            backgroundColor: colors.primary,
            bottom: insets.bottom + botPad + 20,
          },
          pressed && { opacity: 0.85, transform: [{ scale: 0.95 }] },
        ]}
      >
        <Feather name="plus" size={26} color="#fff" />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: {
    borderBottomWidth: 1,
    paddingHorizontal: 16,
    paddingBottom: 12,
  },
  searchBar: {
    flexDirection: "row",
    alignItems: "center",
    borderRadius: 12,
    borderWidth: 1,
    paddingHorizontal: 12,
    paddingVertical: 10,
    gap: 8,
    marginBottom: 10,
  },
  searchInput: { flex: 1, fontSize: 15 },
  filterRow: { marginHorizontal: -16 },
  filterContent: { paddingHorizontal: 16, gap: 8, flexDirection: "row" },
  filterChip: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 20,
    borderWidth: 1,
  },
  filterText: { fontSize: 13, fontWeight: "500" },
  list: { flex: 1 },
  listContent: { padding: 16 },
  fab: {
    position: "absolute",
    right: 20,
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: "center",
    justifyContent: "center",
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 6,
  },
});
