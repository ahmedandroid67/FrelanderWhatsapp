import * as LocalAuthentication from "expo-local-authentication";
import * as SecureStore from "expo-secure-store";
import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { Platform } from "react-native";

const PIN_KEY = "cf_pin";
const LOCK_KEY = "cf_locked";

export type AuthState = "loading" | "setup" | "locked" | "unlocked";

interface AuthContextType {
  state: AuthState;
  hasBiometrics: boolean;
  hasPin: boolean;
  setupPin: (pin: string) => Promise<void>;
  unlockWithPin: (pin: string) => Promise<boolean>;
  unlockWithBiometrics: () => Promise<boolean>;
  lock: () => void;
  clearAuth: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

async function getStoredPin(): Promise<string | null> {
  if (Platform.OS === "web") return null;
  try {
    return await SecureStore.getItemAsync(PIN_KEY);
  } catch {
    return null;
  }
}

async function storePin(pin: string): Promise<void> {
  if (Platform.OS === "web") return;
  await SecureStore.setItemAsync(PIN_KEY, pin, {
    keychainAccessible: SecureStore.WHEN_UNLOCKED,
  });
}

async function checkBiometrics(): Promise<boolean> {
  if (Platform.OS === "web") return false;
  try {
    const compatible = await LocalAuthentication.hasHardwareAsync();
    if (!compatible) return false;
    const enrolled = await LocalAuthentication.isEnrolledAsync();
    return enrolled;
  } catch {
    return false;
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>("loading");
  const [hasBiometrics, setHasBiometrics] = useState(false);
  const [hasPin, setHasPin] = useState(false);

  useEffect(() => {
    async function init() {
      if (Platform.OS === "web") {
        setState("unlocked");
        return;
      }
      const [bio, pin] = await Promise.all([checkBiometrics(), getStoredPin()]);
      setHasBiometrics(bio);
      setHasPin(!!pin);
      if (!pin) {
        setState("setup");
      } else {
        setState("locked");
      }
    }
    init();
  }, []);

  const setupPin = useCallback(async (pin: string) => {
    await storePin(pin);
    setHasPin(true);
    setState("unlocked");
  }, []);

  const unlockWithPin = useCallback(async (pin: string): Promise<boolean> => {
    const stored = await getStoredPin();
    if (stored === pin) {
      setState("unlocked");
      return true;
    }
    return false;
  }, []);

  const unlockWithBiometrics = useCallback(async (): Promise<boolean> => {
    if (Platform.OS === "web") return false;
    try {
      const result = await LocalAuthentication.authenticateAsync({
        promptMessage: "Unlock ClientFlow",
        fallbackLabel: "Use PIN",
        cancelLabel: "Cancel",
        disableDeviceFallback: false,
      });
      if (result.success) {
        setState("unlocked");
        return true;
      }
      return false;
    } catch {
      return false;
    }
  }, []);

  const lock = useCallback(() => {
    setState("locked");
  }, []);

  const clearAuth = useCallback(async () => {
    if (Platform.OS !== "web") {
      await SecureStore.deleteItemAsync(PIN_KEY);
    }
    setHasPin(false);
    setState("setup");
  }, []);

  return (
    <AuthContext.Provider
      value={{
        state,
        hasBiometrics,
        hasPin,
        setupPin,
        unlockWithPin,
        unlockWithBiometrics,
        lock,
        clearAuth,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
