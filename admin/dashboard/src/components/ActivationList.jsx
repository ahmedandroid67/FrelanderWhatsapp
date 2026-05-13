import React, { useState, useEffect } from 'react';
import { useFirebase } from '../context/FirebaseContext';
import { collection, query, orderBy, onSnapshot, limit, doc, deleteDoc, updateDoc } from 'firebase/firestore';
import { List, Calendar, Hash, Smartphone, ShieldCheck, Trash2, Edit2, Check, X } from 'lucide-react';
import { motion } from 'framer-motion';

const ActivationList = () => {
  const { db } = useFirebase();
  const [activations, setActivations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState(null);
  const [editData, setEditData] = useState({ label: '', deviceId: '' });

  useEffect(() => {
    if (!db) return;

    const q = query(
      collection(db, 'activations'),
      orderBy('createdAt', 'desc'),
      limit(50)
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const data = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setActivations(data);
      setLoading(false);
    }, (error) => {
      console.error("Listener error:", error);
      setLoading(false);
    });

    return () => unsubscribe();
  }, [db]);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this activation?')) return;
    try {
      await deleteDoc(doc(db, 'activations', id));
    } catch (err) {
      alert('Delete failed');
    }
  };

  const startEdit = (act) => {
    setEditingId(act.id);
    setEditData({ label: act.label || '', deviceId: act.activatedDeviceId || act.deviceId || '' });
  };

  const handleUpdate = async (id) => {
    try {
      await updateDoc(doc(db, 'activations', id), {
        label: editData.label.trim(),
        activatedDeviceId: editData.deviceId.trim().toUpperCase(),
        deviceId: editData.deviceId.trim().toUpperCase()
      });
      setEditingId(null);
    } catch (err) {
      alert('Update failed');
    }
  };

  const formatDate = (ts) => {
    if (!ts) return 'Unknown';
    const date = ts.toDate ? ts.toDate() : new Date(ts);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-[15px] font-semibold text-white">Recent activations</h2>
        <div className="count-pill">
          {activations.length} total
        </div>
      </div>

      <div className="table-container">
        <table className="admin-table">
          <colgroup>
            <col style={{ width: '22%' }} />
            <col style={{ width: '20%' }} />
            <col style={{ width: '22%' }} />
            <col style={{ width: '20%' }} />
            <col style={{ width: '10%' }} />
            <col style={{ width: '6%' }} />
          </colgroup>
          <thead>
            <tr>
              <th>Code</th>
              <th>Label</th>
              <th>Device ID</th>
              <th>Date</th>
              <th>Status</th>
              <th className="text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="6" className="py-8 text-center text-slate-500">Loading...</td></tr>
            ) : activations.length === 0 ? (
              <tr><td colSpan="6" className="py-8 text-center text-slate-500">No activations found.</td></tr>
            ) : (
              activations.map((act) => (
                <tr key={act.id}>
                  <td>
                    <span className="code-value text-white">{act.code}</span>
                  </td>
                  
                  <td>
                    {editingId === act.id ? (
                      <input 
                        className="input-minimal mono py-0 h-[28px] text-xs"
                        value={editData.label}
                        onChange={e => setEditData({...editData, label: e.target.value})}
                      />
                    ) : (act.label || <span className="em-dash">&mdash;</span>)}
                  </td>
                  
                  <td>
                    {editingId === act.id ? (
                      <input 
                        className="input-minimal mono py-0 h-[28px] text-xs"
                        value={editData.deviceId}
                        onChange={e => setEditData({...editData, deviceId: e.target.value})}
                      />
                    ) : (
                      <span className="mono text-[12px]">{act.activatedDeviceId || act.deviceId || <span className="em-dash">&mdash;</span>}</span>
                    )}
                  </td>

                  <td>
                    <span className="date-value">{formatDate(act.createdAt)}</span>
                  </td>

                  <td>
                    <span className="status-pill">
                      {act.used ? 'Activated' : 'Unused'}
                    </span>
                  </td>

                  <td className="text-right">
                    <div className="flex justify-end gap-2">
                      {editingId === act.id ? (
                        <>
                          <button onClick={() => handleUpdate(act.id)} className="action-btn edit" title="Save">
                            <Check size={14} />
                          </button>
                          <button onClick={() => setEditingId(null)} className="action-btn" title="Cancel">
                            <X size={14} />
                          </button>
                        </>
                      ) : (
                        <>
                          <button onClick={() => startEdit(act)} className="action-btn edit" title="Edit">
                            <Edit2 size={14} />
                          </button>
                          <button onClick={() => handleDelete(act.id)} className="action-btn delete" title="Delete">
                            <Trash2 size={14} />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ActivationList;
