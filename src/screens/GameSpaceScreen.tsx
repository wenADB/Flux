import React, { useState } from 'react';
import { Gamepad2, Flame, Sliders, ShieldCheck, Zap, Crosshair, Plus, Sparkles, Check } from 'lucide-react';
import { AppPackage, SystemTweaks } from '../types';

interface GameSpaceScreenProps {
  packages: AppPackage[];
  tweaks: SystemTweaks;
  onUpdateTweak: <K extends keyof SystemTweaks>(key: K, value: SystemTweaks[K]) => void;
  onLaunchGameMode: (pkg: AppPackage) => void;
}

export const GameSpaceScreen: React.FC<GameSpaceScreenProps> = ({
  packages,
  tweaks,
  onUpdateTweak,
  onLaunchGameMode,
}) => {
  const games = packages.filter((p) => p.category === 'game' || p.gameMode?.enabled);
  const [selectedHz, setSelectedHz] = useState<number>(tweaks.refreshRateLock);
  const [touchPolling, setTouchPolling] = useState<number>(tweaks.touchPollingRate);

  const handleRefreshRate = (hz: number) => {
    setSelectedHz(hz);
    onUpdateTweak('refreshRateLock', hz);
  };

  const handleTouchRate = (rate: number) => {
    setTouchPolling(rate);
    onUpdateTweak('touchPollingRate', rate);
  };

  return (
    <div className="space-y-5 pb-24 animate-fadeIn">
      {/* Game Space Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-[#1c112b] via-[#141026] to-[#0c0a17] border border-purple-500/30 p-5 shadow-xl shadow-purple-950/20">
        <div className="absolute top-0 right-0 w-64 h-64 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="flex items-center gap-3 relative z-10">
          <div className="p-3 rounded-xl bg-purple-500/20 text-purple-400 border border-purple-500/40">
            <Flame className="w-6 h-6 fill-current" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold text-white tracking-tight">Game Space Performance Suite</h2>
              <span className="text-[10px] font-mono font-bold bg-purple-900/60 border border-purple-700/50 text-purple-300 px-2 py-0.5 rounded">
                ESPORTS READY
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Lock refresh rates, overclock touch response polling, and suppress thermal power drops.
            </p>
          </div>
        </div>
      </div>

      {/* Global Gaming Tweaks */}
      <div className="bg-[#0f1523] border border-[#1b263e] rounded-2xl p-5 space-y-4">
        <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider font-mono flex items-center gap-2">
          <Sliders className="w-4 h-4 text-cyan-400" /> Display & Input Hardware Locks
        </h3>

        {/* Refresh Rate Lock */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span className="text-slate-300 font-medium">Display Refresh Rate Lock</span>
            <span className="font-mono text-cyan-400 font-bold">{selectedHz} Hz Locked</span>
          </div>
          <div className="grid grid-cols-4 gap-2">
            {[60, 90, 120, 144].map((hz) => (
              <button
                key={hz}
                onClick={() => handleRefreshRate(hz)}
                className={`py-2 rounded-xl text-xs font-mono font-bold border transition ${
                  selectedHz === hz
                    ? 'bg-cyan-500/20 border-cyan-500 text-cyan-300 shadow-md shadow-cyan-950/40'
                    : 'bg-[#141b2c] border-slate-800 text-slate-400 hover:text-slate-200'
                }`}
              >
                {hz} Hz
              </button>
            ))}
          </div>
        </div>

        {/* Touch Sampling Rate */}
        <div className="space-y-2 pt-2 border-t border-slate-800">
          <div className="flex items-center justify-between text-xs">
            <span className="text-slate-300 font-medium">Touch Digitizer Sampling Rate</span>
            <span className="font-mono text-purple-400 font-bold">{touchPolling} Hz (Ultra-Response)</span>
          </div>
          <div className="grid grid-cols-3 gap-2">
            {[120, 240, 360].map((rate) => (
              <button
                key={rate}
                onClick={() => handleTouchRate(rate)}
                className={`py-2 rounded-xl text-xs font-mono font-bold border transition ${
                  touchPolling === rate
                    ? 'bg-purple-500/20 border-purple-500 text-purple-300 shadow-md shadow-purple-950/40'
                    : 'bg-[#141b2c] border-slate-800 text-slate-400 hover:text-slate-200'
                }`}
              >
                {rate} Hz Polling
              </button>
            ))}
          </div>
        </div>

        {/* Toggles */}
        <div className="space-y-2 pt-2 border-t border-slate-800">
          <label className="flex items-center justify-between p-2.5 rounded-xl bg-[#141b2c] border border-slate-800 cursor-pointer hover:border-slate-700">
            <div className="space-y-0.5">
              <span className="text-xs font-medium text-slate-200">Bypass Charging (Direct Motherboard Power)</span>
              <p className="text-[11px] text-slate-400">Routes USB power directly to SoC to eliminate battery heat.</p>
            </div>
            <input
              type="checkbox"
              checked={tweaks.bypassChargingAlert}
              onChange={(e) => onUpdateTweak('bypassChargingAlert', e.target.checked)}
              className="w-4 h-4 rounded text-cyan-500 focus:ring-0 focus:ring-offset-0 bg-slate-900 border-slate-700"
            />
          </label>

          <label className="flex items-center justify-between p-2.5 rounded-xl bg-[#141b2c] border border-slate-800 cursor-pointer hover:border-slate-700">
            <div className="space-y-0.5">
              <span className="text-xs font-medium text-slate-200">Aggressive Background Kill on Launch</span>
              <p className="text-[11px] text-slate-400">Terminates social and sync services during active game sessions.</p>
            </div>
            <input
              type="checkbox"
              checked={tweaks.killBgAppsOnGameLaunch}
              onChange={(e) => onUpdateTweak('killBgAppsOnGameLaunch', e.target.checked)}
              className="w-4 h-4 rounded text-cyan-500 focus:ring-0 focus:ring-offset-0 bg-slate-900 border-slate-700"
            />
          </label>
        </div>
      </div>

      {/* Installed Games List */}
      <div className="space-y-3">
        <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider font-mono">
          Configured Games ({games.length})
        </h3>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {games.map((g) => (
            <div
              key={g.packageName}
              className="bg-[#0f1523] border border-[#1b263e] rounded-xl p-4 flex items-center justify-between hover:border-purple-500/40 transition"
            >
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-purple-500/10 border border-purple-500/30 flex items-center justify-center text-purple-400">
                  <Gamepad2 className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-slate-100">{g.appName}</h4>
                  <p className="text-[11px] font-mono text-slate-400">
                    Target: {g.gameMode?.fpsLock || 60} FPS • {g.memoryMb} MB RAM
                  </p>
                </div>
              </div>

              <button
                onClick={() => onLaunchGameMode(g)}
                className="px-3 py-1.5 rounded-lg bg-gradient-to-r from-purple-500 to-indigo-600 hover:from-purple-400 hover:to-indigo-500 text-white font-semibold text-xs flex items-center gap-1.5 shadow-md shadow-purple-950/40 active:scale-95 transition"
              >
                <Zap className="w-3.5 h-3.5 fill-current" />
                <span>Boost</span>
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
