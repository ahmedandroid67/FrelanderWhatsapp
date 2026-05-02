import AsyncStorage from "@react-native-async-storage/async-storage";
import { useCallback, useEffect, useState } from "react";

const TEMPLATES_KEY = "@cf_templates";

export interface MessageTemplate {
  id: string;
  name: string;
  content: string;
  emoji: string;
  isDefault: boolean;
  createdAt: string;
}

export const DEFAULT_TEMPLATES: MessageTemplate[] = [
  {
    id: "tpl_payment_reminder",
    name: "Payment Reminder",
    content:
      "Hi {name}, just a reminder that your payment of {amount} is due. Please let me know when you can settle this. Thank you!",
    emoji: "💳",
    isDefault: true,
    createdAt: new Date().toISOString(),
  },
  {
    id: "tpl_booking_confirm",
    name: "Booking Confirmation",
    content:
      "Hi {name}, your booking is confirmed for {date}. Looking forward to seeing you — let me know if you have any questions!",
    emoji: "📅",
    isDefault: true,
    createdAt: new Date().toISOString(),
  },
  {
    id: "tpl_followup",
    name: "Follow-up",
    content:
      "Hi {name}, just checking in! Hope you're happy with everything. Feel free to reach out anytime.",
    emoji: "👋",
    isDefault: true,
    createdAt: new Date().toISOString(),
  },
  {
    id: "tpl_invoice",
    name: "Invoice",
    content:
      "Hi {name}, here's your invoice for {service} — total {amount}. Thank you so much for your business!",
    emoji: "🧾",
    isDefault: true,
    createdAt: new Date().toISOString(),
  },
];

function genId(): string {
  return "tpl_" + Date.now().toString() + Math.random().toString(36).substr(2, 6);
}

export function fillTemplate(
  content: string,
  vars: { name?: string; amount?: string; date?: string; service?: string }
): string {
  return content
    .replace(/\{name\}/g, vars.name ?? "{name}")
    .replace(/\{amount\}/g, vars.amount ?? "{amount}")
    .replace(/\{date\}/g, vars.date ?? "{date}")
    .replace(/\{service\}/g, vars.service ?? "{service}");
}

export function useTemplates() {
  const [templates, setTemplates] = useState<MessageTemplate[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const stored = await AsyncStorage.getItem(TEMPLATES_KEY);
        if (stored) {
          const parsed: MessageTemplate[] = JSON.parse(stored);
          setTemplates(parsed);
        } else {
          setTemplates(DEFAULT_TEMPLATES);
          await AsyncStorage.setItem(
            TEMPLATES_KEY,
            JSON.stringify(DEFAULT_TEMPLATES)
          );
        }
      } catch {
        setTemplates(DEFAULT_TEMPLATES);
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, []);

  const save = async (updated: MessageTemplate[]) => {
    setTemplates(updated);
    await AsyncStorage.setItem(TEMPLATES_KEY, JSON.stringify(updated));
  };

  const addTemplate = useCallback(
    async (data: Pick<MessageTemplate, "name" | "content" | "emoji">) => {
      const template: MessageTemplate = {
        ...data,
        id: genId(),
        isDefault: false,
        createdAt: new Date().toISOString(),
      };
      await save([...templates, template]);
      return template;
    },
    [templates]
  );

  const updateTemplate = useCallback(
    async (id: string, updates: Partial<MessageTemplate>) => {
      await save(
        templates.map((t) => (t.id === id ? { ...t, ...updates } : t))
      );
    },
    [templates]
  );

  const deleteTemplate = useCallback(
    async (id: string) => {
      await save(templates.filter((t) => t.id !== id));
    },
    [templates]
  );

  const resetDefaults = useCallback(async () => {
    await save(DEFAULT_TEMPLATES);
  }, []);

  return {
    templates,
    isLoading,
    addTemplate,
    updateTemplate,
    deleteTemplate,
    resetDefaults,
  };
}
