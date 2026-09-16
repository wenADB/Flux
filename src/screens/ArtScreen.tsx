import React, { useState } from 'react';
import { Cpu, CheckCircle2, Play, RefreshCw, AlertCircle, Sparkles, Layers } from 'lucide-react';
import { AppPackage } from '../types';

interface ArtScreenProps {
  packages: AppPackage[];
  onCompileApp: (pkgName: string) => void;
  onBatchCompile: (filter: string) => void;
  isCompiling: boolean;
}

export const ArtScreen: React.FC<ArtScreenProps> = ({
  packages,
  onCompileApp,
  onBatchCompile,
  isCompiling,
}) => {
  const [activeFilter, setActiveFilter] = useState<'speed-profile' | 'speed' | 'everything'>('speed-profile');

  const filters = [
    {
      key: 'speed-profile' as const,
      name: 'speed-profile',
      desc: 'Optimizes hot code paths using system usage telemetry. Best battery & performance balance.',
      badge: 'Recommended',
    },
    {
      key: 'speed' as const,
      name: 'speed',
      desc: 'Compiles all app bytecode ahead-of-time (AOT). Eliminates JIT compilation stuttering entirely.',
      badge: 'Maximum Speed',
    },
    {
      key: 'everything' as const,
      name: 'everything',
      desc: 'Aggressive compilation of all DEX code, classes & metadata. Requires high internal storage space.',
      badge: 'Heavy Disk Use',
    },
  ];

  return (
    <div className="space-y-5 pb-24 animate-fadeIn">
      {/* ART Header Card */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-[#0e1c28] via-[#0d1722] to-[#090f17] border border-cyan-500/30 p-5 shadow-xl shadow-cyan-950/20">
        <div className="flex items-center gap-3 relative z-10">
          <div className="p-3 rounded-xl bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">
            <Cpu className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold text-white tracking-tight">Android Runtime (ART) Dexopt Engine</h2>
              <span className="text-[10px] font-mono font-bold bg-cyan-950/80 border border-cyan-800/60 text-cyan-400 px-2 py-0.5 rounded">
                DEX2OAT
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">
              Force Ahead-Of-Time (AOT) machine compilation to eliminate Java/Kotlin JIT runtime frame drops.
            </p>
          </div>
        </div>

        {/* Batch trigger */}
        <div className="mt-4 pt-4 border-t border-slate-800/80 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="text-xs text-slate-300 font-mono">
            Selected Filter: <span className="text-cyan-400 font-bold">{activeFilter}</span>
          </div>
          <button
            onClick={() => onBatchCompile(activeFilter)}
            disabled={isCompiling}
            className="w-full sm:w-auto px-4 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-black font-bold text-xs flex items-center justify-center gap-2 shadow-md shadow-cyan-500/20 active:scale-95 transition disabled:opacity-50"
          >
            {isCompiling ? (
              <>
                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                <span>Compiling Package Bytecode...</span>
              </>
            ) : (
              <>
                <Sparkles className="w-3.5 h-3.5 fill-current" />
                <span>Compile All Installed Apps ({packages.length})</span>
              </>
            )}
          </button>
        </div>
      </div>

      {/* Compiler Mode Selector */}
      <div className="space-y-3">
        <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider font-mono">
          Compiler Compilation Filters
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          {filters.map((f) => {
            const isSelected = activeFilter === f.key;
            return (
              <button
                key={f.key}
                onClick={() => setActiveFilter(f.key)}
                className={`p-4 rounded-xl text-left border transition relative ${
                  isSelected
                    ? 'bg-[#111c2e] border-cyan-500/50 shadow-lg shadow-cyan-950/30 ring-1 ring-cyan-500/30'
                    : 'bg-[#0f1523] border-[#1b263e] hover:bg-[#121a2c]'
                }`}
              >
                <div className="flex items-center justify-between mb-1.5">
                  <span className="font-mono text-xs font-bold text-cyan-400">{f.name}</span>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-900 border border-slate-700 text-slate-300">
                    {f.badge}
                  </span>
                </div>
                <p className="text-xs text-slate-400">{f.desc}</p>
              </button>
            );
          })}
        </div>
      </div>

      {/* Per-App Compilation Status */}
      <div className="space-y-3">
        <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider font-mono">
          Installed Packages ({packages.length})
        </h3>

        <div className="space-y-2">
          {packages.map((pkg) => (
            <div
              key={pkg.packageName}
              className="bg-[#0f1523] border border-[#1b263e] rounded-xl p-3.5 flex items-center justify-between hover:border-slate-700 transition"
            >
              <div className="space-y-0.5">
                <div className="flex items-center gap-2">
                  <h4 className="text-xs font-bold text-slate-100">{pkg.appName}</h4>
                  <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-800 text-slate-400">
                    {pkg.packageName}
                  </span>
                </div>
                <div className="flex items-center gap-2 text-[11px] font-mono">
                  <span className="text-slate-400">ART Filter:</span>
                  <span className={`font-semibold ${
                    pkg.artCompileStatus === 'speed' || pkg.artCompileStatus === 'speed-profile'
                      ? 'text-emerald-400'
                      : 'text-amber-400'
                  }`}>
                    {pkg.artCompileStatus}
                  </span>
                </div>
              </div>

              <button
                onClick={() => onCompileApp(pkg.packageName)}
                disabled={isCompiling}
                className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-cyan-300 font-mono text-xs flex items-center gap-1.5 border border-slate-700 active:scale-95 transition"
              >
                <Play className="w-3 h-3 fill-current text-cyan-400" />
                <span>Optimize</span>
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
