import React, { createContext, useContext, useState, useEffect } from 'react';
import { initializeApp, getApps, deleteApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';

const FirebaseContext = createContext(null);

export const useFirebase = () => useContext(FirebaseContext);

export const FirebaseProvider = ({ children }) => {
  const [config, setConfig] = useState(() => {
    // Check for Environment Variables (Coolify)
    if (import.meta.env.VITE_FIREBASE_API_KEY && import.meta.env.VITE_FIREBASE_PROJECT_ID) {
      return {
        apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
        authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
        projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
        storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
        messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
        appId: import.meta.env.VITE_FIREBASE_APP_ID
      };
    }
    const saved = localStorage.getItem('cf_firebase_config');
    return saved ? JSON.parse(saved) : null;
  });
  
  const [db, setDb] = useState(null);
  const [auth, setAuth] = useState(null);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    if (config) {
      try {
        // Clean up previous apps if any
        const apps = getApps();
        if (apps.length) {
          // In development, Vite might re-run this. 
          // We can just use the existing one or re-init.
        }
        
        const app = initializeApp(config);
        setDb(getFirestore(app));
        setAuth(getAuth(app));
        setIsInitialized(true);
      } catch (error) {
        console.error("Firebase Initialization Error:", error);
        localStorage.removeItem('cf_firebase_config');
        setConfig(null);
      }
    } else {
      setIsInitialized(false);
      setDb(null);
      setAuth(null);
    }
  }, [config]);

  const saveConfig = (newConfig) => {
    localStorage.setItem('cf_firebase_config', JSON.stringify(newConfig));
    setConfig(newConfig);
  };

  const clearConfig = () => {
    localStorage.removeItem('cf_firebase_config');
    setConfig(null);
  };

  return (
    <FirebaseContext.Provider value={{ db, auth, isInitialized, saveConfig, clearConfig, config }}>
      {children}
    </FirebaseContext.Provider>
  );
};
