import AsyncStorage from "@react-native-async-storage/async-storage";
import * as Notifications from "expo-notifications";
import { useCallback, useEffect } from "react";
import { Platform } from "react-native";

const NOTIF_IDS_KEY = "@cf_notif_ids";

// Notification IDs map: { [bookingId]: notifId }
type NotifMap = Record<string, string>;

async function getNotifMap(): Promise<NotifMap> {
  try {
    const raw = await AsyncStorage.getItem(NOTIF_IDS_KEY);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

async function saveNotifMap(map: NotifMap): Promise<void> {
  await AsyncStorage.setItem(NOTIF_IDS_KEY, JSON.stringify(map));
}

async function requestPermissions(): Promise<boolean> {
  if (Platform.OS === "web") return false;
  const { status: existing } = await Notifications.getPermissionsAsync();
  if (existing === "granted") return true;
  const { status } = await Notifications.requestPermissionsAsync();
  return status === "granted";
}

export function setupNotificationHandler() {
  if (Platform.OS === "web") return;
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldShowBanner: true,
      shouldShowList: true,
      shouldPlaySound: true,
      shouldSetBadge: false,
    }),
  });
}

export function useNotifications() {
  useEffect(() => {
    requestPermissions().catch(console.warn);
  }, []);

  /**
   * Schedule a notification 1 day before the booking.
   * If booking is today or already past, schedule for 1 hour from now as a reminder.
   */
  const scheduleBookingNotification = useCallback(
    async (
      bookingId: string,
      clientName: string,
      dateStr: string,
      timeStr?: string
    ) => {
      if (Platform.OS === "web") return;
      try {
        const granted = await requestPermissions();
        if (!granted) return;

        // Parse booking date
        const [y, m, d] = dateStr.split("-").map(Number);
        const bookingDate = new Date(y, m - 1, d);
        // Notify 1 day before at 9am
        const notifDate = new Date(y, m - 1, d - 1, 9, 0, 0);

        const now = new Date();
        // If already past, use 1 hour from now
        const triggerDate = notifDate > now ? notifDate : new Date(now.getTime() + 60 * 60 * 1000);

        const timeLabel = timeStr ? ` at ${timeStr}` : "";
        const notifId = await Notifications.scheduleNotificationAsync({
          content: {
            title: "📅 Upcoming Booking",
            body: `${clientName} is booked for tomorrow${timeLabel}`,
            data: { bookingId },
          },
          trigger: {
            type: Notifications.SchedulableTriggerInputTypes.DATE,
            date: triggerDate,
          },
        });

        // Store mapping
        const map = await getNotifMap();
        map[bookingId] = notifId;
        await saveNotifMap(map);
      } catch (e) {
        console.warn("Failed to schedule notification", e);
      }
    },
    []
  );

  /**
   * Cancel a previously scheduled notification for a booking.
   */
  const cancelBookingNotification = useCallback(async (bookingId: string) => {
    if (Platform.OS === "web") return;
    try {
      const map = await getNotifMap();
      const notifId = map[bookingId];
      if (notifId) {
        await Notifications.cancelScheduledNotificationAsync(notifId);
        delete map[bookingId];
        await saveNotifMap(map);
      }
    } catch (e) {
      console.warn("Failed to cancel notification", e);
    }
  }, []);

  /**
   * Cancel all scheduled notifications (e.g. when deleting a client).
   */
  const cancelAllNotificationsForClient = useCallback(
    async (bookingIds: string[]) => {
      for (const id of bookingIds) {
        await cancelBookingNotification(id);
      }
    },
    [cancelBookingNotification]
  );

  return {
    scheduleBookingNotification,
    cancelBookingNotification,
    cancelAllNotificationsForClient,
  };
}
