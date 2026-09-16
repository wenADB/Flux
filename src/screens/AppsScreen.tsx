import React, { useState } from 'react';
import { Boxes, Snowflake, Play, Trash2, Cpu, Layers, AlertCircle, RefreshCw } from 'lucide-react';
import { AppPackage } from '../types';

interface AppsScreenProps {
  packages: AppPackage[];
  onFreezeApp: (pkgName: string) => void;
  onOptimizeApp: (pkgName: string) => void;
}

export const AppsScreen: React.FC<AppsScreenProps> = ({
  packages,
  onFreezeApp,
  onOptimizeApp,
}) => {
  const [filter, setFilter] = useState<'all' | 'running' | 'frozen'>('all');

  const filtered = packages.filter((p) => {
    if (filter === 'running') return p.status === 'running';
    if (filter === 'frozen') return p.status === 'frozen';
    return true;
  });

  return (
    <div className="space-y-5 pb-24 animate-fadeIn">
      {/* Header */}
      <div className="bg-[#0f1523] border border-[#1b263e] rounded-2xl p-5">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400">
            <Boxes className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-base font-bold text-slate-100">Background Process & App Manager</h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Inspect memory footprints, freeze wakelock abusers, and reclaim active RAM.
            </p>
          </div>
        </div>

        {/* Filter Pills */}
        <div className="flex items-center gap-2 mt-4 pt-3 border-t border-slate-800">
          {(['all', 'running', 'frozen'] as const).map((mode) => (
            <button
              key={mode}
              onClick={() => setFilter(mode)}
              className={`px-3 py-1 rounded-lg text-xs font-mono capitalize transition ${
                filter === mode
                  ? 'bg-cyan-500 text-black font-bold shadow-sm shadow-cyan-500/30'
                  : 'bg-[#141c2e] text-slate-400 hover:text-slate-200 border border-slate-800'
              }`}
            >
              {mode} ({mode === 'all' ? packages.length : packages.filter(p => p.status === mode).length})
            </button>
          ))}
        </div>
      </div>

      {/* App List */}
      <div className="space-y-2.5">
        {filtered.map((app) => {
          const isFrozen = app.status === 'frozen';
          return (
            <div
              key={app.packageName}
              className={`bg-[#0f1523] border rounded-xl p-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 transition ${
                isFrozen
                  ? 'border-blue-500/30 bg-[#0c1424]/70'
                  : 'border-[#1b263e] hover:border-slate-700'
              }`}
            >
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <h4 className="text-xs font-bold text-slate-100">{app.appName}</h4>
                  <span className={`text-[10px] font-mono px-1.5 py-0.2 rounded font-semibold uppercase ${
                    isFrozen
                      ? 'bg-blue-950 text-blue-300 border border-blue-800'
                      : app.status === 'running'
                      ? 'bg-emerald-950 text-emerald-300 border border-emerald-800'
                      : 'bg-slate-800 text-slate-400'
                  }`}>
                    {app.status}
                  </span>
                  {app.isSystem && (
                    <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-purple-950 text-purple-300 border border-purple-800">
                      System
                    </span>
                  )}
                </div>
                <div className="flex items-center gap-3 text-[11px] font-mono text-slate-400">
                  <span className="flex items-center gap-1">
                    <Layers className="w-3 h-3 text-purple-400" /> {app.memoryMb} MB
                  </span>
                  <span className="flex items-center gap-1">
                    <Cpu className="w-3 h-3 text-cyan-400" /> {app.cpuPercent}% CPU
                  </span>
                  <span className="hidden sm:inline text-slate-500">{app.packageName}</span>
                </div>
              </div>

              <div className="flex items-center gap-2 self-end sm:self-center">
                <button
                  onClick={() => onOptimizeApp(app.packageName)}
                  className="px-2.5 py-1 rounded-lg bg-[#141c2e] hover:bg-[#1a253d] text-cyan-300 border border-slate-700 font-mono text-[11px] transition"
                  title="Dexopt compile"
                >
                  Dexopt
                </button>
                <button
                  onClick={() => onFreezeApp(app.packageName)}
                  className={`px-3 py-1 rounded-lg font-mono text-[11px] font-semibold flex items-center gap-1 border transition ${
                    isFrozen
                      ? 'bg-blue-500/20 text-blue-300 border-blue-500/40 hover:bg-blue-500/30'
                      : 'bg-slate-800 text-slate-300 border-slate-700 hover:bg-slate-700'
                  }`}
                >
                  <Snowflake className="w-3 h-3" />
                  <span>{isFrozen ? 'Defrost' : 'Freeze'}</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
