import { MessageTemplates } from "@/context/DataContext";

interface MessageData {
  name?: string;
  service?: string;
  amount?: string;
  date?: string;
  time?: string;
}

export function formatMessage(template: string, data: MessageData): string {
  let msg = template;
  msg = msg.replace(/\{name\}/g, data.name || "");
  msg = msg.replace(/\{service\}/g, data.service || "");
  msg = msg.replace(/\{amount\}/g, data.amount || "");
  msg = msg.replace(/\{date\}/g, data.date || "");
  msg = msg.replace(/\{time\}/g, data.time || "");
  return msg;
}
