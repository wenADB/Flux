import React from 'react';
import { Settings, Shield, Moon, Eye, AlertTriangle, RefreshCw, Key, Power } from 'lucide-react';
import { ShizukuStatus, SystemTweaks } from '../types';

interface SettingsScreenProps {
  shizukuStatus: ShizukuStatus;
  tweaks: SystemTweaks;
  onUpdateTweak: <K extends keyof SystemTweaks>(key: K, value: SystemTweaks[K]) => void;
  onOpenShizuku: () => void;
  onResetEngine: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  shizukuStatus,
  tweaks,
  onUpdateTweak,
  onOpenShizuku,
  onResetEngine,
}) => {
  return (
    <div className="space-y-5 pb-24 animate-fadeIn">
      <div className="bg-[#0f1523] border border-[#1b263e] rounded-2xl p-5">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400">
            <Settings className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-base font-bold text-slate-100">Engine Configuration & Safety</h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Control Shizuku daemon binding, safety fallbacks, and window animations.
            </p>
          </div>
        </div>
      </div>

      {/* Safety & Permissions Section */}
      <div className="bg-[#0f1523] border border-[#1b263e] rounded-2xl p-5 space-y-4">
        <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider font-mono flex items-center gap-2">
          <Shield className="w-4 h-4 text-emerald-400" /> Shizuku & Rootless Privileges
        </h3>

        <div className="flex items-center justify-between p-3.5 rounded-xl bg-[#141d30] border border-slate-800">
          <div className="space-y-0.5">
            <span className="text-xs font-semibold text-slate-200">Shizuku Binder Service</span>
            <p className="text-[11px] text-slate-400">Active status: {shizukuStatus}</p>
          </div>

          <button
            onClick={onOpenShizuku}
            className="px-3 py-1.5 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-black font-semibold text-xs font-mono transition"
          >
            Manage ADB
          </button>
        </div>

        <label className="flex items-center justify-between p-3.5 rounded-xl bg-[#141d30] border border-slate-800 cursor-pointer">
          <div className="space-y-0.5">
            <span className="text-xs font-semibold text-slate-200">Enforce Safe Execution Safeguards</span>
            <p className="text-[11px] text-slate-400">Blocks high-risk system commands that might soft-brick device UI.</p>
          </div>
          <input
            type="checkbox"
            defaultChecked={true}
            className="w-4 h-4 rounded text-cyan-500 bg-slate-900 border-slate-700"
          />
        </label>
      </div>

      {/* Window Animation Scales */}
      <div className="bg-[#0f1523] border border-[#1b263e] rounded-2xl p-5 space-y-4">
        <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider font-mono flex items-center gap-2">
          <Eye className="w-4 h-4 text-cyan-400" /> Android Window Manager Animations
        </h3>

        <div className="space-y-3">
          <div className="flex items-center justify-between text-xs">
            <span className="text-slate-300">Animation Speed Multiplier</span>
            <span className="font-mono text-cyan-400 font-bold">{tweaks.windowAnimationScale}x</span>
          </div>

          <div className="grid grid-cols-3 gap-2">
            {[0, 0.5, 1.0].map((scale) => (
              <button
                key={scale}
                onClick={() => {
                  onUpdateTweak('windowAnimationScale', scale);
                  onUpdateTweak('transitionAnimationScale', scale);
                  onUpdateTweak('animatorDurationScale', scale);
                }}
                className={`py-2 rounded-xl text-xs font-mono font-bold border transition ${
                  tweaks.windowAnimationScale === scale
                    ? 'bg-cyan-500/20 border-cyan-500 text-cyan-300'
                    : 'bg-[#141b2c] border-slate-800 text-slate-400 hover:text-slate-200'
                }`}
              >
                {scale === 0 ? 'Disabled (Instant)' : `${scale}x (Snappy)`}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Danger Zone */}
      <div className="bg-[#181119] border border-rose-900/30 rounded-2xl p-5 space-y-3">
        <h3 className="text-sm font-bold text-rose-300 uppercase tracking-wider font-mono flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-rose-400" /> Maintenance & Reset
        </h3>
        <p className="text-xs text-slate-400">
          Restore all engine profiles, clear cached telemetry metrics, and revert kernel overrides to factory defaults.
        </p>

        <button
          onClick={onResetEngine}
          className="px-4 py-2 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 font-semibold text-xs transition"
        >
          Reset All Tweaks & Cache
        </button>
      </div>
    </div>
  );
};
