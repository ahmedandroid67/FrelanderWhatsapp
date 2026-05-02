import AsyncStorage from "@react-native-async-storage/async-storage";
import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";

export type ClientStatus = "Lead" | "Quoted" | "Booked" | "Completed" | "Paid";
export type PaymentStatus = "unpaid" | "partial" | "paid";

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
}

const DataContext = createContext<DataContextType | null>(null);

const KEYS = {
  clients: "@cf_clients",
  bookings: "@cf_bookings",
  payments: "@cf_payments",
  invoices: "@cf_invoices",
};

function genId(): string {
  return Date.now().toString() + Math.random().toString(36).substr(2, 9);
}

function computeStatus(total: number, paid: number): PaymentStatus {
  if (paid <= 0) return "unpaid";
  if (paid >= total) return "paid";
  return "partial";
}

export function DataProvider({ children }: { children: React.ReactNode }) {
  const [clients, setClients] = useState<Client[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [c, b, p, i] = await Promise.all([
          AsyncStorage.getItem(KEYS.clients),
          AsyncStorage.getItem(KEYS.bookings),
          AsyncStorage.getItem(KEYS.payments),
          AsyncStorage.getItem(KEYS.invoices),
        ]);
        if (c) setClients(JSON.parse(c));
        if (b) setBookings(JSON.parse(b));
        if (p) setPayments(JSON.parse(p));
        if (i) setInvoices(JSON.parse(i));
      } catch (e) {
        console.error("Failed to load data", e);
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, []);

  async function saveClients(updated: Client[]) {
    setClients(updated);
    await AsyncStorage.setItem(KEYS.clients, JSON.stringify(updated));
  }
  async function saveBookings(updated: Booking[]) {
    setBookings(updated);
    await AsyncStorage.setItem(KEYS.bookings, JSON.stringify(updated));
  }
  async function savePayments(updated: Payment[]) {
    setPayments(updated);
    await AsyncStorage.setItem(KEYS.payments, JSON.stringify(updated));
  }
  async function saveInvoices(updated: Invoice[]) {
    setInvoices(updated);
    await AsyncStorage.setItem(KEYS.invoices, JSON.stringify(updated));
  }

  const addClient = useCallback(
    async (data: Omit<Client, "id" | "createdAt">): Promise<Client> => {
      const client: Client = {
        ...data,
        id: genId(),
        createdAt: new Date().toISOString(),
      };
      await saveClients([...clients, client]);
      return client;
    },
    [clients]
  );

  const updateClient = useCallback(
    async (id: string, updates: Partial<Client>) => {
      await saveClients(
        clients.map((c) => (c.id === id ? { ...c, ...updates } : c))
      );
    },
    [clients]
  );

  const deleteClient = useCallback(
    async (id: string) => {
      await saveClients(clients.filter((c) => c.id !== id));
      await saveBookings(bookings.filter((b) => b.clientId !== id));
      await savePayments(payments.filter((p) => p.clientId !== id));
      await saveInvoices(invoices.filter((i) => i.clientId !== id));
    },
    [clients, bookings, payments, invoices]
  );

  const addBooking = useCallback(
    async (data: Omit<Booking, "id">): Promise<Booking> => {
      const booking: Booking = { ...data, id: genId() };
      await saveBookings([...bookings, booking]);
      return booking;
    },
    [bookings]
  );

  const updateBooking = useCallback(
    async (id: string, updates: Partial<Booking>) => {
      await saveBookings(
        bookings.map((b) => (b.id === id ? { ...b, ...updates } : b))
      );
    },
    [bookings]
  );

  const deleteBooking = useCallback(
    async (id: string) => {
      await saveBookings(bookings.filter((b) => b.id !== id));
    },
    [bookings]
  );

  const setPayment = useCallback(
    async (
      clientId: string,
      data: Omit<Payment, "id" | "status" | "clientId">
    ): Promise<Payment> => {
      const status = computeStatus(data.totalAmount, data.paidAmount);
      const existing = payments.find((p) => p.clientId === clientId);
      if (existing) {
        const updated = { ...existing, ...data, status };
        await savePayments(
          payments.map((p) => (p.clientId === clientId ? updated : p))
        );
        return updated;
      }
      const payment: Payment = { id: genId(), clientId, ...data, status };
      await savePayments([...payments, payment]);
      return payment;
    },
    [payments]
  );

  const updatePayment = useCallback(
    async (id: string, updates: Partial<Payment>) => {
      await savePayments(
        payments.map((p) => {
          if (p.id !== id) return p;
          const updated = { ...p, ...updates };
          updated.status = computeStatus(updated.totalAmount, updated.paidAmount);
          return updated;
        })
      );
    },
    [payments]
  );

  const addInvoice = useCallback(
    async (data: Omit<Invoice, "id" | "createdAt">): Promise<Invoice> => {
      const invoice: Invoice = {
        ...data,
        id: genId(),
        createdAt: new Date().toISOString(),
      };
      await saveInvoices([...invoices, invoice]);
      return invoice;
    },
    [invoices]
  );

  const deleteInvoice = useCallback(
    async (id: string) => {
      await saveInvoices(invoices.filter((i) => i.id !== id));
    },
    [invoices]
  );

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
