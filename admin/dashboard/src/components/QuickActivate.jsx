import React, { useState } from 'react';
import { useFirebase } from '../context/FirebaseContext';
import { collection, addDoc, serverTimestamp } from 'firebase/firestore';
import { Zap, Copy, Check, Smartphone } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const QuickActivate = () => {
  const { db } = useFirebase();
  const [deviceId, setDeviceId] = useState('');
  const [label, setLabel] = useState('');
  const [generatedCode, setGeneratedCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  const generateCode = () => {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    let code = '';
    for (let i = 0; i < 6; i++) {
      code += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return code;
  };

  const handleActivate = async (e) => {
    e.preventDefault();
    if (!deviceId) return;
    
    setLoading(true);
    try {
      const code = generateCode();
      await addDoc(collection(db, 'activations'), {
        code: code,
        used: true,
        usedAt: serverTimestamp(),
        activatedDeviceId: deviceId.trim().toUpperCase(),
        deviceId: deviceId.trim().toUpperCase(),
        label: label.trim(),
        createdAt: serverTimestamp(),
        notes: 'Generated via Admin Dashboard'
      });
      setGeneratedCode(code);
      setLabel('');
    } catch (error) {
      console.error("Activation error:", error);
      alert("Failed to activate. Check your Firebase permissions.");
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(generatedCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="card-secondary">
      <label className="label-tiny">New Activation</label>
      
      <form onSubmit={handleActivate} className="flex items-center gap-2">
        <div className="flex-1">
          <input
            className="input-minimal mono"
            placeholder="Device ID (CF-...)"
            value={deviceId}
            onChange={(e) => setDeviceId(e.target.value)}
            required
          />
        </div>
        <div className="flex-1">
          <input
            className="input-minimal"
            placeholder="Owner Label"
            value={label}
            onChange={(e) => setLabel(e.target.value)}
          />
        </div>
        <button 
          type="submit" 
          className="btn-activate"
          disabled={loading}
        >
          <Zap size={14} fill="currentColor" />
          <span>{loading ? 'Activating...' : 'Activate now'}</span>
        </button>
      </form>

      <AnimatePresence>
        {generatedCode && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="mt-4 p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-lg flex items-center justify-between"
          >
            <div className="flex items-center gap-3">
              <span className="text-[10px] uppercase tracking-widest text-emerald-500 font-bold">Code:</span>
              <span className="text-xl mono font-bold text-white tracking-widest">{generatedCode}</span>
            </div>
            <button 
              onClick={copyToClipboard}
              className="action-btn edit p-1"
              title="Copy Code"
            >
              {copied ? <Check size={16} /> : <Copy size={16} />}
            </button>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default QuickActivate;
