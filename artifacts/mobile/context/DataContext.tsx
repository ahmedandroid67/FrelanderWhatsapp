import AsyncStorage from "@react-native-async-storage/async-storage";
import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";

export type ClientStatus = "Lead" | "Quoted" | "Booked" | "Completed" | "Paid";
export type PaymentStatus = "unpaid" | "partial" | "paid";

export interface MessageTemplate {
  id: string;
  name: string;
  content: string;
  emoji: string;
  isDefault: boolean;
}

const DEFAULT_TEMPLATES: MessageTemplate[] = [
  {
    id: "tpl_payment_reminder",
    name: "Payment Reminder",
    content: "Hi {name}, just a reminder that your payment of {amount} is overdue. Please let me know when you can settle this. Thank you!",
    emoji: "💳",
    isDefault: true,
  },
  {
    id: "tpl_booking_confirm",
    name: "Booking Confirmation",
    content: "Hi {name}, your booking for {service} is confirmed for {date}. Looking forward to seeing you!",
    emoji: "📅",
    isDefault: true,
  },
  {
    id: "tpl_followup",
    name: "Follow-up",
    content: "Hi {name}, just checking in! Hope you're happy with everything. Feel free to reach out anytime.",
    emoji: "👋",
    isDefault: true,
  }
];

export interface Client {
  id: string;
  name: string;
  phone: string;
  serviceType: string;
  notes: string;
  status: ClientStatus;
  createdAt: string;
}

export interface Booking {
  id: string;
  clientId: string;
  date: string;
  time: string;
  location: string;
  notes: string;
}

export interface Payment {
  id: string;
  clientId: string;
  totalAmount: number;
  paidAmount: number;
  dueDate: string;
  status: PaymentStatus;
}

export interface Invoice {
  id: string;
  clientId: string;
  amount: number;
  description: string;
  createdAt: string;
}

interface DataContextType {
  clients: Client[];
  bookings: Booking[];
  payments: Payment[];
  invoices: Invoice[];
  templates: MessageTemplate[];
  isLoading: boolean;
  addClient: (data: Omit<Client, "id" | "createdAt">) => Promise<Client>;
  updateClient: (id: string, updates: Partial<Client>) => Promise<void>;
  deleteClient: (id: string) => Promise<void>;
  addBooking: (data: Omit<Booking, "id">) => Promise<Booking>;
  updateBooking: (id: string, updates: Partial<Booking>) => Promise<void>;
  deleteBooking: (id: string) => Promise<void>;
  setPayment: (
    clientId: string,
    data: Omit<Payment, "id" | "status" | "clientId">
  ) => Promise<Payment>;
  updatePayment: (id: string, updates: Partial<Payment>) => Promise<void>;
  addInvoice: (data: Omit<Invoice, "id" | "createdAt">) => Promise<Invoice>;
  deleteInvoice: (id: string) => Promise<void>;
  getClientById: (id: string) => Client | undefined;
  getBookingsForClient: (clientId: string) => Booking[];
  getPaymentForClient: (clientId: string) => Payment | undefined;
  getInvoicesForClient: (clientId: string) => Invoice[];
  addTemplate: (data: Omit<MessageTemplate, "id" | "isDefault">) => Promise<MessageTemplate>;
  updateTemplate: (id: string, updates: Partial<MessageTemplate>) => Promise<void>;
  deleteTemplate: (id: string) => Promise<void>;
  resetTemplates: () => Promise<void>;
}

const DataContext = createContext<DataContextType | null>(null);

const KEYS = {
  clients: "@cf_clients",
  bookings: "@cf_bookings",
  payments: "@cf_payments",
  invoices: "@cf_invoices",
  templates: "@cf_templates",
};

function genId(): string {
  // Use crypto.randomUUID when available, fallback to timestamp+random
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return (
    Date.now().toString(36) +
    Math.random().toString(36).substring(2, 9) +
    Math.random().toString(36).substring(2, 9)
  );
}

function computeStatus(total: number, paid: number): PaymentStatus {
  if (paid <= 0) return "unpaid";
  if (paid >= total) return "paid";
  return "partial";
}


export function DataProvider({ children }: { children: React.ReactNode }) {
  // Refs hold current data synchronously (no stale closures in callbacks)
  const clientsRef = useRef<Client[]>([]);
  const bookingsRef = useRef<Booking[]>([]);
  const paymentsRef = useRef<Payment[]>([]);
  const invoicesRef = useRef<Invoice[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Render state — triggers re-renders
  const [clients, setClientsRender] = useState<Client[]>([]);
  const [bookings, setBookingsRender] = useState<Booking[]>([]);
  const [payments, setPaymentsRender] = useState<Payment[]>([]);
  const [invoices, setInvoicesRender] = useState<Invoice[]>([]);
  const [templates, setTemplates] = useState<MessageTemplate[]>(DEFAULT_TEMPLATES);

  useEffect(() => {
    async function load() {
      try {
        const [c, b, p, i] = await Promise.all([
          AsyncStorage.getItem(KEYS.clients),
          AsyncStorage.getItem(KEYS.bookings),
          AsyncStorage.getItem(KEYS.payments),
          AsyncStorage.getItem(KEYS.invoices),
        ]);
        const parsedClients = c ? (JSON.parse(c) as Client[]) : [];
        const parsedBookings = b ? (JSON.parse(b) as Booking[]) : [];
        const parsedPayments = p ? (JSON.parse(p) as Payment[]) : [];
        const parsedInvoices = i ? (JSON.parse(i) as Invoice[]) : [];

        clientsRef.current = parsedClients;
        bookingsRef.current = parsedBookings;
        paymentsRef.current = parsedPayments;
        invoicesRef.current = parsedInvoices;

        setClientsRender(parsedClients);
        setBookingsRender(parsedBookings);
        setPaymentsRender(parsedPayments);
        setInvoicesRender(parsedInvoices);

        const t = await AsyncStorage.getItem(KEYS.templates);
        if (t) setTemplates(JSON.parse(t));
        else {
          await AsyncStorage.setItem(KEYS.templates, JSON.stringify(DEFAULT_TEMPLATES));
        }
      } catch (e) {
        console.error("Failed to load data", e);
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, []);

  // Atomic updater: mutates ref, persists, then triggers render
  const updateClients = useCallback(
    async (updater: (prev: Client[]) => Client[]) => {
      const next = updater(clientsRef.current);
      clientsRef.current = next;
      setClientsRender(next);
      await AsyncStorage.setItem(KEYS.clients, JSON.stringify(next));
      return next;
    },
    []
  );

  const updateBookings = useCallback(
    async (updater: (prev: Booking[]) => Booking[]) => {
      const next = updater(bookingsRef.current);
      bookingsRef.current = next;
      setBookingsRender(next);
      await AsyncStorage.setItem(KEYS.bookings, JSON.stringify(next));
      return next;
    },
    []
  );

  const updatePayments = useCallback(
    async (updater: (prev: Payment[]) => Payment[]) => {
      const next = updater(paymentsRef.current);
      paymentsRef.current = next;
      setPaymentsRender(next);
      await AsyncStorage.setItem(KEYS.payments, JSON.stringify(next));
      return next;
    },
    []
  );

  const updateInvoices = useCallback(
    async (updater: (prev: Invoice[]) => Invoice[]) => {
      const next = updater(invoicesRef.current);
      invoicesRef.current = next;
      setInvoicesRender(next);
      await AsyncStorage.setItem(KEYS.invoices, JSON.stringify(next));
      return next;
    },
    []
  );

  const addClient = useCallback(
    async (data: Omit<Client, "id" | "createdAt">): Promise<Client> => {
      const client: Client = {
        ...data,
        id: genId(),
        createdAt: new Date().toISOString(),
      };
      await updateClients((prev) => [...prev, client]);
      return client;
    },
    [updateClients]
  );

  const updateClient = useCallback(
    async (id: string, updates: Partial<Client>) => {
      await updateClients((prev) =>
        prev.map((c) => (c.id === id ? { ...c, ...updates } : c))
      );
    },
    [updateClients]
  );

  const deleteClient = useCallback(
    async (id: string) => {
      await Promise.all([
        updateClients((prev) => prev.filter((c) => c.id !== id)),
        updateBookings((prev) => prev.filter((b) => b.clientId !== id)),
        updatePayments((prev) => prev.filter((p) => p.clientId !== id)),
        updateInvoices((prev) => prev.filter((i) => i.clientId !== id)),
      ]);
    },
    [updateClients, updateBookings, updatePayments, updateInvoices]
  );

  const addBooking = useCallback(
    async (data: Omit<Booking, "id">): Promise<Booking> => {
      const booking: Booking = { ...data, id: genId() };
      await updateBookings((prev) => [...prev, booking]);
      return booking;
    },
    [updateBookings]
  );

  const updateBooking = useCallback(
    async (id: string, updates: Partial<Booking>) => {
      await updateBookings((prev) =>
        prev.map((b) => (b.id === id ? { ...b, ...updates } : b))
      );
    },
    [updateBookings]
  );

  const deleteBooking = useCallback(
    async (id: string) => {
      await updateBookings((prev) => prev.filter((b) => b.id !== id));
    },
    [updateBookings]
  );

  const setPayment = useCallback(
    async (
      clientId: string,
      data: Omit<Payment, "id" | "status" | "clientId">
    ): Promise<Payment> => {
      const status = computeStatus(data.totalAmount, data.paidAmount);
      const existing = paymentsRef.current.find((p) => p.clientId === clientId);
      if (existing) {
        const updated = { ...existing, ...data, status };
        await updatePayments((prev) =>
          prev.map((p) => (p.clientId === clientId ? updated : p))
        );
        return updated;
      }
      const payment: Payment = { id: genId(), clientId, ...data, status };
      await updatePayments((prev) => [...prev, payment]);
      return payment;
    },
    [updatePayments]
  );

  const updatePayment = useCallback(
    async (id: string, updates: Partial<Payment>) => {
      await updatePayments((prev) =>
        prev.map((p) => {
          if (p.id !== id) return p;
          const updated = { ...p, ...updates };
          updated.status = computeStatus(updated.totalAmount, updated.paidAmount);
          return updated;
        })
      );
    },
    [updatePayments]
  );

  const addInvoice = useCallback(
    async (data: Omit<Invoice, "id" | "createdAt">): Promise<Invoice> => {
      const invoice: Invoice = {
        ...data,
        id: genId(),
        createdAt: new Date().toISOString(),
      };
      await updateInvoices((prev) => [...prev, invoice]);
      return invoice;
    },
    [updateInvoices]
  );

  const deleteInvoice = useCallback(
    async (id: string) => {
      await updateInvoices((prev) => prev.filter((i) => i.id !== id));
    },
    [updateInvoices]
  );
  
  const addTemplate = useCallback(async (data: Omit<MessageTemplate, "id" | "isDefault">) => {
    const tpl: MessageTemplate = { ...data, id: genId(), isDefault: false };
    const next = [...templates, tpl];
    setTemplates(next);
    await AsyncStorage.setItem(KEYS.templates, JSON.stringify(next));
    return tpl;
  }, [templates]);

  const updateTemplate = useCallback(async (id: string, updates: Partial<MessageTemplate>) => {
    const next = templates.map(t => t.id === id ? { ...t, ...updates } : t);
    setTemplates(next);
    await AsyncStorage.setItem(KEYS.templates, JSON.stringify(next));
  }, [templates]);

  const deleteTemplate = useCallback(async (id: string) => {
    const next = templates.filter(t => t.id !== id);
    setTemplates(next);
    await AsyncStorage.setItem(KEYS.templates, JSON.stringify(next));
  }, [templates]);

  const resetTemplates = useCallback(async () => {
    setTemplates(DEFAULT_TEMPLATES);
    await AsyncStorage.setItem(KEYS.templates, JSON.stringify(DEFAULT_TEMPLATES));
  }, []);

  const getClientById = useCallback(
    (id: string) => clients.find((c) => c.id === id),
    [clients]
  );
  const getBookingsForClient = useCallback(
    (clientId: string) => bookings.filter((b) => b.clientId === clientId),
    [bookings]
  );
  const getPaymentForClient = useCallback(
    (clientId: string) => payments.find((p) => p.clientId === clientId),
    [payments]
  );
  const getInvoicesForClient = useCallback(
    (clientId: string) => invoices.filter((i) => i.clientId === clientId),
    [invoices]
  );

  return (
    <DataContext.Provider
      value={{
        clients,
        bookings,
        payments,
        invoices,
        isLoading,
        addClient,
        updateClient,
        deleteClient,
        addBooking,
        updateBooking,
        deleteBooking,
        setPayment,
        updatePayment,
        addInvoice,
        deleteInvoice,
        getClientById,
        getBookingsForClient,
        getPaymentForClient,
        getInvoicesForClient,
        addTemplate,
        updateTemplate,
        deleteTemplate,
        resetTemplates,
      }}
    >
      {children}
    </DataContext.Provider>
  );
}

export function useData() {
  const ctx = useContext(DataContext);
  if (!ctx) throw new Error("useData must be used within DataProvider");
  return ctx;
}
