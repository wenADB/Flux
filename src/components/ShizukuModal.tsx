import React, { useState } from 'react';
import { X, ShieldCheck, ShieldAlert, Wifi, Terminal, Key, CheckCircle, RefreshCw, AlertTriangle } from 'lucide-react';
import { ShizukuStatus } from '../types';

interface ShizukuModalProps {
  isOpen: boolean;
  status: ShizukuStatus;
  onClose: () => void;
  onStatusChange: (status: ShizukuStatus) => void;
}

export const ShizukuModal: React.FC<ShizukuModalProps> = ({
  isOpen,
  status,
  onClose,
  onStatusChange,
}) => {
  const [pairingPort, setPairingPort] = useState('37921');
  const [pairingCode, setPairingCode] = useState('684129');
  const [isPairing, setIsPairing] = useState(false);
  const [pairSuccess, setPairSuccess] = useState(false);

  if (!isOpen) return null;

  const handlePair = () => {
    setIsPairing(true);
    setTimeout(() => {
      setIsPairing(false);
      setPairSuccess(true);
      onStatusChange('PERMISSION_GRANTED');
      setTimeout(() => {
        setPairSuccess(false);
      }, 3000);
    }, 1200);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-fadeIn">
      <div className="bg-[#0f1523] border border-[#1f2b45] rounded-2xl w-full max-w-md overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="px-5 py-4 border-b border-[#1b263e] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="p-2 rounded-lg bg-cyan-500/10 text-cyan-400">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-100">Shizuku Privileged Bridge</h3>
              <p className="text-xs text-slate-400">Android System IPC (UID 1000)</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 space-y-4">
          {/* Status banner */}
          <div className={`p-3.5 rounded-xl border flex items-center justify-between ${
            status === 'PERMISSION_GRANTED'
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
              : 'bg-amber-500/10 border-amber-500/30 text-amber-300'
          }`}>
            <div className="flex items-center gap-2.5">
              {status === 'PERMISSION_GRANTED' ? (
                <ShieldCheck className="w-5 h-5 text-emerald-400" />
              ) : (
                <ShieldAlert className="w-5 h-5 text-amber-400" />
              )}
              <div>
                <p className="text-xs font-semibold">
                  {status === 'PERMISSION_GRANTED' ? 'Privileged IPC Active' : 'Shizuku Authorization Missing'}
                </p>
                <p className="text-[11px] opacity-80">
                  {status === 'PERMISSION_GRANTED'
                    ? 'Commands execute with system service permissions.'
                    : 'Requires ADB wireless pairing or Shizuku app.'}
                </p>
              </div>
            </div>

            <button
              onClick={() => onStatusChange(status === 'PERMISSION_GRANTED' ? 'PERMISSION_MISSING' : 'PERMISSION_GRANTED')}
              className="text-[11px] px-2.5 py-1 rounded-md font-mono bg-slate-800 border border-slate-700 text-slate-200 hover:bg-slate-700"
            >
              Toggle
            </button>
          </div>

          {/* Quick Wireless ADB Pairing */}
          <div className="bg-[#141d30] border border-[#1e2a44] rounded-xl p-4 space-y-3">
            <div className="flex items-center gap-2 text-xs font-semibold text-cyan-300">
              <Wifi className="w-4 h-4" />
              <span>Rootless Wireless Debugging Pairing</span>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="text-[10px] text-slate-400 uppercase font-mono">Pairing Port</label>
                <input
                  type="text"
                  value={pairingPort}
                  onChange={(e) => setPairingPort(e.target.value)}
                  className="w-full mt-1 bg-[#0c1220] border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs font-mono text-slate-200 focus:outline-none focus:border-cyan-500"
                  placeholder="37xxx"
                />
              </div>
              <div>
                <label className="text-[10px] text-slate-400 uppercase font-mono">6-Digit Code</label>
                <input
                  type="text"
                  value={pairingCode}
                  onChange={(e) => setPairingCode(e.target.value)}
                  className="w-full mt-1 bg-[#0c1220] border border-slate-700 rounded-lg px-2.5 py-1.5 text-xs font-mono text-slate-200 focus:outline-none focus:border-cyan-500"
                  placeholder="123456"
                />
              </div>
            </div>

            <button
              onClick={handlePair}
              disabled={isPairing}
              className="w-full py-2 px-3 rounded-lg bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-black font-semibold text-xs flex items-center justify-center gap-2 transition disabled:opacity-50"
            >
              {isPairing ? (
                <>
                  <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                  <span>Handshaking with Android ADB daemon...</span>
                </>
              ) : pairSuccess ? (
                <>
                  <CheckCircle className="w-3.5 h-3.5 text-emerald-950" />
                  <span>Paired & Shizuku Bound Successfully!</span>
                </>
              ) : (
                <>
                  <Key className="w-3.5 h-3.5" />
                  <span>Pair & Grant IPC Privileges</span>
                </>
              )}
            </button>
          </div>

          {/* ADB Command Reference */}
          <div className="space-y-1.5">
            <div className="flex items-center gap-1.5 text-[11px] text-slate-400 font-mono">
              <Terminal className="w-3.5 h-3.5" />
              <span>Terminal alternative (PC / Termux):</span>
            </div>
            <pre className="p-2.5 bg-[#0a0f1c] border border-slate-800 rounded-lg text-[10px] font-mono text-cyan-400 overflow-x-auto select-all">
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
            </pre>
          </div>
        </div>

        {/* Footer */}
        <div className="px-5 py-3 bg-[#0a0f1c] border-t border-[#1b263e] flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-200 transition"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
