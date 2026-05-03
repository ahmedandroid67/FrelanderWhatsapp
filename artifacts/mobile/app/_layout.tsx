import {
  Inter_400Regular,
  Inter_500Medium,
  Inter_600SemiBold,
  Inter_700Bold,
  useFonts,
} from "@expo-google-fonts/inter";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Stack } from "expo-router";
import * as SplashScreen from "expo-splash-screen";
import React, { useEffect } from "react";
import { AppState, AppStateStatus } from "react-native";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { KeyboardProvider } from "react-native-keyboard-controller";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { useTranslation } from "react-i18next";

import { ErrorBoundary } from "@/components/ErrorBoundary";
import { LockScreen } from "@/components/LockScreen";
import { SetupPinScreen } from "@/components/SetupPinScreen";
import { AuthProvider, useAuth } from "@/context/AuthContext";
import { DataProvider } from "@/context/DataContext";
import { setupNotificationHandler } from "@/hooks/useNotifications";
import { initI18n } from "@/lib/i18n";

// Initialize notification handler once at startup
setupNotificationHandler();

SplashScreen.preventAutoHideAsync();

const queryClient = new QueryClient();

function RootLayoutNav() {
  const { t } = useTranslation();
  return (
    <Stack>
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
      <Stack.Screen
        name="client/form"
        options={{ title: t("client"), presentation: "modal" }}
      />
      <Stack.Screen name="client/[id]" options={{ title: t("client") }} />
      <Stack.Screen
        name="booking/form"
        options={{ title: t("addBooking"), presentation: "modal" }}
      />
      <Stack.Screen
        name="payment/[clientId]"
        options={{ title: t("payment"), presentation: "modal" }}
      />
      <Stack.Screen
        name="security"
        options={{ title: t("security"), presentation: "modal" }}
      />
    </Stack>
  );
}

function AppGate() {
  const { state, lock } = useAuth();

  useEffect(() => {
    const sub = AppState.addEventListener(
      "change",
      (nextState: AppStateStatus) => {
        if (nextState === "background" || nextState === "inactive") {
          lock();
        }
      }
    );
    return () => sub.remove();
  }, [lock]);

  if (state === "loading") return null;
  if (state === "setup") return <SetupPinScreen />;
  if (state === "locked") return <LockScreen />;

  return <RootLayoutNav />;
}

export default function RootLayout() {
  const [fontsLoaded, fontError] = useFonts({
    Inter_400Regular,
    Inter_500Medium,
    Inter_600SemiBold,
    Inter_700Bold,
  });
  const [i18nInitialized, setI18nInitialized] = React.useState(false);

  useEffect(() => {
    initI18n().then(() => setI18nInitialized(true));
  }, []);

  useEffect(() => {
    if ((fontsLoaded || fontError) && i18nInitialized) {
      SplashScreen.hideAsync();
    }
  }, [fontsLoaded, fontError, i18nInitialized]);

  if ((!fontsLoaded && !fontError) || !i18nInitialized) return null;

  return (
    <SafeAreaProvider>
      <ErrorBoundary>
        <QueryClientProvider client={queryClient}>
          <AuthProvider>
            <DataProvider>
              <GestureHandlerRootView>
                <KeyboardProvider>
                  <AppGate />
                </KeyboardProvider>
              </GestureHandlerRootView>
            </DataProvider>
          </AuthProvider>
        </QueryClientProvider>
      </ErrorBoundary>
    </SafeAreaProvider>
  );
}
