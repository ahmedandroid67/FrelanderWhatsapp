import React from 'react';
import { useFirebase } from './context/FirebaseContext';
import SetupWizard from './components/SetupWizard';
import QuickActivate from './components/QuickActivate';
import ActivationList from './components/ActivationList';
import { LogOut, LayoutDashboard, Database } from 'lucide-react';
import logo from './assets/logo.png';

function App() {
  const { isInitialized, clearConfig, config } = useFirebase();

  if (!isInitialized) {
    return <SetupWizard />;
  }

  return (
    <div className="max-w-5xl mx-auto p-6 lg:p-12 min-h-screen">
      {/* Header */}
      <header className="flex items-center justify-between mb-10">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-3">
            <img src={logo} alt="ClientFlow" className="w-10 h-10 object-contain" />
            <h1 className="header-title text-white">ClientFlow Admin</h1>
          </div>
          <div className="project-pill">
            <span className="dot-green"></span>
            <span>{config?.projectId}</span>
          </div>
        </div>
        
        <button 
          onClick={clearConfig}
          className="btn-disconnect"
        >
          <LogOut size={14} />
          <span>Disconnect</span>
        </button>
      </header>

      <main className="space-y-8">
        <QuickActivate />
        <div className="space-y-4">
          <ActivationList />
        </div>
      </main>

      <footer className="mt-20 py-8 border-t border-white/5 text-center text-slate-500 text-xs">
        <p>&copy; 2026 ClientFlow Agnostic Admin Dashboard</p>
        <p className="mt-1">Portable License Management System</p>
      </footer>
    </div>
  );
}

export default App;
