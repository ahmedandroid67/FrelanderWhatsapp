import React, { useState } from 'react';
import { useFirebase } from '../context/FirebaseContext';
import { Settings, ShieldCheck, Zap } from 'lucide-react';
import { motion } from 'framer-motion';

const SetupWizard = () => {
  const { saveConfig } = useFirebase();
  const [rawConfig, setRawConfig] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    try {
      // Try to parse the config. User might paste the whole firebaseConfig object.
      let configObj;
      if (rawConfig.trim().startsWith('{')) {
        configObj = JSON.parse(rawConfig);
      } else {
        // Handle potential copy-paste from JS snippet
        const match = rawConfig.match(/\{[\s\S]*\}/);
        if (match) {
          // Dangerous but common for these tools: using Function to parse JS object literal
          configObj = new Function(`return ${match[0]}`)();
        }
      }

      if (configObj && configObj.apiKey && configObj.projectId) {
        saveConfig(configObj);
      } else {
        setError('Invalid Firebase configuration. Make sure it includes apiKey and projectId.');
      }
    } catch (err) {
      setError('Failed to parse configuration. Please paste a valid JSON object.');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="glass-card max-w-lg w-full"
      >
        <div className="flex items-center gap-3 mb-6">
          <div className="p-3 bg-indigo-500/20 rounded-lg text-indigo-400">
            <Settings size={24} />
          </div>
          <div>
            <h1 className="text-xl font-bold">Connect Firebase</h1>
            <p className="text-sm text-slate-400">Link this dashboard to your project</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label">Firebase SDK Config (JSON)</label>
            <textarea
              className="input-field min-h-[200px] font-mono text-xs"
              placeholder='{ "apiKey": "...", "authDomain": "...", "projectId": "...", ... }'
              value={rawConfig}
              onChange={(e) => setRawConfig(e.target.value)}
              required
            />
            <p className="text-[10px] text-slate-500 mt-2">
              Found in: Firebase Console &rarr; Project Settings &rarr; General &rarr; Your Apps
            </p>
          </div>

          {error && <p className="text-sm text-red-400 bg-red-400/10 p-2 rounded">{error}</p>}

          <button type="submit" className="btn-primary w-full flex items-center justify-center gap-2">
            <Zap size={18} />
            Initialize Dashboard
          </button>
        </form>

        <div className="mt-8 pt-6 border-t border-white/5 space-y-3">
          <div className="flex gap-3 text-xs text-slate-400">
            <ShieldCheck size={14} className="text-emerald-400 shrink-0" />
            <p>Settings are stored locally in your browser and never sent to our servers.</p>
          </div>
        </div>
      </motion.div>
    </div>
  );
};

export default SetupWizard;
