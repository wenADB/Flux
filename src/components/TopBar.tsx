import React, { useState } from 'react';
import { Shield, ShieldAlert, ShieldCheck, Settings, Info, Cpu, Activity, Zap } from 'lucide-react';
import { ShizukuStatus, PerformanceProfile } from '../types';

interface TopBarProps {
  shizukuStatus: ShizukuStatus;
  currentProfile: PerformanceProfile;
  cpuTemp: number;
  onOpenShizuku: () => void;
  onOpenSettings: () => void;
  onOpenAbout: () => void;
}

export const TopBar: React.FC<TopBarProps> = ({
  shizukuStatus,
  currentProfile,
  cpuTemp,
  onOpenShizuku,
  onOpenSettings,
  onOpenAbout,
}) => {
  const getStatusBadge = () => {
    switch (shizukuStatus) {
      case 'PERMISSION_GRANTED':
        return {
          icon: <ShieldCheck className="w-4 h-4 text-emerald-400" />,
          text: 'Shizuku Active',
          bg: 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300',
        };
      case 'PERMISSION_MISSING':
        return {
          icon: <ShieldAlert className="w-4 h-4 text-amber-400" />,
          text: 'Auth Required',
          bg: 'bg-amber-500/10 border-amber-500/30 text-amber-300',
        };
      default:
        return {
          icon: <Shield className="w-4 h-4 text-rose-400" />,
          text: 'Disconnected',
          bg: 'bg-rose-500/10 border-rose-500/30 text-rose-300',
        };
    }
  };

  const status = getStatusBadge();

  return (
    <header className="sticky top-0 z-40 bg-[#0d121f]/90 backdrop-blur-md border-b border-[#1b253b] px-4 py-3 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-cyan-500 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
            <Zap className="w-5 h-5 text-black fill-current" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-extrabold tracking-widest text-lg bg-gradient-to-r from-cyan-400 to-blue-400 bg-clip-text text-transparent">
                FLUX
              </span>
              <span className="text-[10px] font-mono uppercase px-1.5 py-0.5 rounded bg-cyan-950/60 border border-cyan-800/40 text-cyan-400 font-semibold">
                ROOTLESS v2.4
              </span>
            </div>
            <p className="text-[11px] text-slate-400 leading-tight">Android Performance Engine</p>
          </div>
        </div>
      </div>

      <div className="flex items-center gap-2">
        {/* Shizuku Privileged IPC badge */}
        <button
          onClick={onOpenShizuku}
          className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium border transition-all hover:brightness-110 active:scale-95 ${status.bg}`}
          title="Shizuku / ADB Connection Manager"
        >
          {status.icon}
          <span className="hidden sm:inline">{status.text}</span>
        </button>

        {/* Live Hardware Temp Chip */}
        <div className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-[#151c2e] border border-slate-700/50 text-xs font-mono text-slate-300">
          <Activity className="w-3.5 h-3.5 text-cyan-400 animate-pulse" />
          <span>{cpuTemp}°C</span>
        </div>

        {/* Action icons */}
        <button
          onClick={onOpenSettings}
          className="p-2 rounded-lg bg-[#141b2d] hover:bg-[#1b253d] text-slate-400 hover:text-slate-200 border border-slate-800 transition"
          title="Engine Settings"
        >
          <Settings className="w-4 h-4" />
        </button>
        <button
          onClick={onOpenAbout}
          className="p-2 rounded-lg bg-[#141b2d] hover:bg-[#1b253d] text-slate-400 hover:text-slate-200 border border-slate-800 transition"
          title="Hardware Telemetry & About"
        >
          <Info className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
};
