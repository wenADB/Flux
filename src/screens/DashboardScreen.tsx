import React from 'react';
import {
  Cpu,
  Zap,
  Activity,
  Battery,
  Gauge,
  Thermometer,
  Layers,
  Flame,
  ArrowUpRight,
  ShieldCheck,
  RefreshCw,
  Sparkles
} from 'lucide-react';
import { DeviceTelemetry, PerformanceProfile } from '../types';

interface DashboardScreenProps {
  telemetry: DeviceTelemetry;
  profile: PerformanceProfile;
  onSelectProfile: (p: PerformanceProfile) => void;
  onQuickBoost: () => void;
  isOptimizing: boolean;
}

export const DashboardScreen: React.FC<DashboardScreenProps> = ({
  telemetry,
  profile,
  onSelectProfile,
  onQuickBoost,
  isOptimizing,
}) => {
  const ramPercent = Math.round((telemetry.ramUsedMb / telemetry.ramTotalMb) * 100);
  const zramPercent = Math.round((telemetry.zramUsedMb / telemetry.zramTotalMb) * 100);

  const profiles: { key: PerformanceProfile; title: string; desc: string; icon: any; color: string }[] = [
    { key: 'battery', title: 'Battery Saver', desc: 'Lower clocks, deep idle', icon: Battery, color: 'text-emerald-400 border-emerald-500/30' },
    { key: 'balanced', title: 'Balanced', desc: 'Dynamic scheduler tuning', icon: Gauge, color: 'text-cyan-400 border-cyan-500/30' },
    { key: 'performance', title: 'Performance', desc: 'Boosted cluster clocks', icon: Zap, color: 'text-blue-400 border-blue-500/30' },
    { key: 'gaming', title: 'Game Space', desc: 'Fixed clocks & 120Hz lock', icon: Flame, color: 'text-rose-400 border-rose-500/30' },
  ];

  return (
    <div className="space-y-5 pb-24 animate-fadeIn">
      {/* Hero Quick Optimization Card */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-[#121a2d] via-[#101726] to-[#0c111e] border border-cyan-500/20 p-5 shadow-xl shadow-cyan-950/20">
        <div className="absolute top-0 right-0 w-64 h-64 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none -mr-16 -mt-16" />

        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 relative z-10">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-mono font-semibold uppercase tracking-wider text-cyan-400">
                System Status • Nominal
              </span>
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
            </div>
            <h2 className="text-xl font-bold text-white tracking-tight">Android Kernel Telemetry</h2>
            <p className="text-xs text-slate-400 max-w-sm mt-0.5">
              Rootless optimization engine using Shizuku privileged IPC. Real-time CPU, GPU & Memory scaling.
            </p>
          </div>

          <button
            onClick={onQuickBoost}
            disabled={isOptimizing}
            className="w-full sm:w-auto px-5 py-3 rounded-xl bg-gradient-to-r from-cyan-400 to-blue-500 hover:from-cyan-300 hover:to-blue-400 active:scale-95 text-black font-bold text-xs flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/25 transition disabled:opacity-50"
          >
            {isOptimizing ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin text-black" />
                <span>Optimizing Kernel Caches...</span>
              </>
            ) : (
              <>
                <Sparkles className="w-4 h-4 text-black fill-current" />
                <span>One-Tap RAM & Cache Boost</span>
              </>
            )}
          </button>
        </div>

        {/* Real-Time Metrics Strip */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-5 pt-4 border-t border-slate-800/80">
          <div className="bg-[#0b101c]/80 rounded-xl p-3 border border-slate-800/70">
            <div className="flex items-center justify-between text-slate-400 text-xs mb-1">
              <span className="flex items-center gap-1.5"><Cpu className="w-3.5 h-3.5 text-cyan-400" /> CPU Load</span>
              <span className="font-mono text-cyan-400">{telemetry.cpuUsage}%</span>
            </div>
            <div className="w-full bg-slate-800/80 rounded-full h-1.5 overflow-hidden">
              <div
                className="bg-cyan-400 h-full rounded-full transition-all duration-500"
                style={{ width: `${telemetry.cpuUsage}%` }}
              />
            </div>
            <div className="flex justify-between text-[10px] text-slate-400 font-mono mt-1.5">
              <span>{telemetry.cpuFreqMhz} MHz</span>
              <span>{telemetry.cpuGovernor}</span>
            </div>
          </div>

          <div className="bg-[#0b101c]/80 rounded-xl p-3 border border-slate-800/70">
            <div className="flex items-center justify-between text-slate-400 text-xs mb-1">
              <span className="flex items-center gap-1.5"><Activity className="w-3.5 h-3.5 text-blue-400" /> GPU Load</span>
              <span className="font-mono text-blue-400">{telemetry.gpuUsage}%</span>
            </div>
            <div className="w-full bg-slate-800/80 rounded-full h-1.5 overflow-hidden">
              <div
                className="bg-blue-400 h-full rounded-full transition-all duration-500"
                style={{ width: `${telemetry.gpuUsage}%` }}
              />
            </div>
            <div className="flex justify-between text-[10px] text-slate-400 font-mono mt-1.5">
              <span>Adreno 740</span>
              <span>{telemetry.gpuFreqMhz} MHz</span>
            </div>
          </div>

          <div className="bg-[#0b101c]/80 rounded-xl p-3 border border-slate-800/70">
            <div className="flex items-center justify-between text-slate-400 text-xs mb-1">
              <span className="flex items-center gap-1.5"><Layers className="w-3.5 h-3.5 text-purple-400" /> LPDDR5X</span>
              <span className="font-mono text-purple-400">{ramPercent}%</span>
            </div>
            <div className="w-full bg-slate-800/80 rounded-full h-1.5 overflow-hidden">
              <div
                className="bg-purple-400 h-full rounded-full transition-all duration-500"
                style={{ width: `${ramPercent}%` }}
              />
            </div>
            <div className="flex justify-between text-[10px] text-slate-400 font-mono mt-1.5">
              <span>{(telemetry.ramUsedMb / 1024).toFixed(1)} GB</span>
              <span>{(telemetry.ramTotalMb / 1024).toFixed(0)} GB</span>
            </div>
          </div>

          <div className="bg-[#0b101c]/80 rounded-xl p-3 border border-slate-800/70">
            <div className="flex items-center justify-between text-slate-400 text-xs mb-1">
              <span className="flex items-center gap-1.5"><Thermometer className="w-3.5 h-3.5 text-rose-400" /> Thermals</span>
              <span className={`font-mono ${telemetry.cpuTempC > 42 ? 'text-rose-400' : 'text-emerald-400'}`}>
                {telemetry.cpuTempC}°C
              </span>
            </div>
            <div className="w-full bg-slate-800/80 rounded-full h-1.5 overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-500 ${
                  telemetry.cpuTempC > 42 ? 'bg-rose-500' : 'bg-emerald-400'
                }`}
                style={{ width: `${Math.min(100, (telemetry.cpuTempC / 60) * 100)}%` }}
              />
            </div>
            <div className="flex justify-between text-[10px] text-slate-400 font-mono mt-1.5">
              <span>Battery: {telemetry.batteryTempC}°C</span>
              <span>{telemetry.thermalThrottling ? 'THROTTLED' : 'COOL'}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Performance Profile Selection */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider font-mono">
            Performance Engine Profiles
          </h3>
          <span className="text-[11px] text-cyan-400 font-mono">Kernel Scheduler Mode</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {profiles.map((p) => {
            const Icon = p.icon;
            const isSelected = profile === p.key;
            return (
              <button
                key={p.key}
                onClick={() => onSelectProfile(p.key)}
                className={`p-4 rounded-xl text-left border transition-all relative overflow-hidden ${
                  isSelected
                    ? `bg-[#131b2d] ${p.color} ring-1 ring-cyan-500/50 shadow-lg shadow-cyan-950/40`
                    : 'bg-[#0f1523] border-[#1a243a] text-slate-400 hover:bg-[#121929] hover:text-slate-200'
                }`}
              >
                {isSelected && (
                  <div className="absolute top-2 right-2 flex items-center gap-1 text-[10px] font-mono font-bold text-cyan-400 bg-cyan-950/60 px-2 py-0.5 rounded border border-cyan-800/50">
                    <ShieldCheck className="w-3 h-3" /> ACTIVE
                  </div>
                )}
                <Icon className={`w-5 h-5 mb-2.5 ${isSelected ? 'text-cyan-400' : 'text-slate-400'}`} />
                <h4 className="text-sm font-bold text-slate-100">{p.title}</h4>
                <p className="text-xs text-slate-400 mt-0.5">{p.desc}</p>
              </button>
            );
          })}
        </div>
      </div>

      {/* Secondary Telemetry Details */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {/* Battery & Power */}
        <div className="bg-[#0f1523] border border-[#1b263e] rounded-xl p-4 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-300 flex items-center gap-1.5 font-mono uppercase">
              <Battery className="w-4 h-4 text-emerald-400" /> Battery Telemetry
            </span>
            <span className="text-xs font-mono text-emerald-400 font-semibold">{telemetry.batteryLevel}%</span>
          </div>

          <div className="grid grid-cols-3 gap-2 text-center pt-1">
            <div className="bg-[#090e18] p-2.5 rounded-lg border border-slate-800">
              <div className="text-[10px] text-slate-400">Voltage</div>
              <div className="text-xs font-mono font-semibold text-slate-200 mt-0.5">
                {(telemetry.batteryVoltageMv / 1000).toFixed(2)} V
              </div>
            </div>
            <div className="bg-[#090e18] p-2.5 rounded-lg border border-slate-800">
              <div className="text-[10px] text-slate-400">Current Drain</div>
              <div className="text-xs font-mono font-semibold text-amber-400 mt-0.5">
                -{telemetry.batteryDrainMa} mA
              </div>
            </div>
            <div className="bg-[#090e18] p-2.5 rounded-lg border border-slate-800">
              <div className="text-[10px] text-slate-400">Target Refresh</div>
              <div className="text-xs font-mono font-semibold text-cyan-400 mt-0.5">
                {telemetry.currentRefreshRate} Hz
              </div>
            </div>
          </div>
        </div>

        {/* zRAM Compression Swap */}
        <div className="bg-[#0f1523] border border-[#1b263e] rounded-xl p-4 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-300 flex items-center gap-1.5 font-mono uppercase">
              <Layers className="w-4 h-4 text-purple-400" /> zRAM Swap Kernel Space
            </span>
            <span className="text-xs font-mono text-purple-400 font-semibold">{zramPercent}% utilized</span>
          </div>

          <div className="w-full bg-slate-800/80 rounded-full h-2 overflow-hidden">
            <div
              className="bg-purple-400 h-full rounded-full transition-all duration-500"
              style={{ width: `${zramPercent}%` }}
            />
          </div>

          <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
            <span>Allocated: {(telemetry.zramUsedMb / 1024).toFixed(1)} / {(telemetry.zramTotalMb / 1024).toFixed(1)} GB</span>
            <span className="text-cyan-400">zstd algorithm</span>
          </div>
        </div>
      </div>
    </div>
  );
};
