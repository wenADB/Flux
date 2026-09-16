import React from 'react';
import { Zap, CheckCircle2, RotateCcw, AlertCircle, Shield, ArrowRight, Play } from 'lucide-react';
import { OptimizationCommand } from '../types';

interface OptimizerScreenProps {
  commands: OptimizationCommand[];
  onApplyCommand: (cmd: OptimizationCommand) => void;
  onRollbackCommand: (cmd: OptimizationCommand) => void;
  isExecuting: boolean;
}

export const OptimizerScreen: React.FC<OptimizerScreenProps> = ({
  commands,
  onApplyCommand,
  onRollbackCommand,
  isExecuting,
}) => {
  return (
    <div className="space-y-5 pb-24 animate-fadeIn">
      {/* Header Info */}
      <div className="bg-[#0f1523] border border-[#1b263e] rounded-2xl p-5">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400">
            <Zap className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-base font-bold text-slate-100">Kernel & Memory Optimization Engine</h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Targeted system tweaks safely tuned via rootless Shizuku IPC with automatic rollback capabilities.
            </p>
          </div>
        </div>
      </div>

      {/* Tweaks List */}
      <div className="space-y-3">
        {commands.map((cmd) => {
          const isApplied = cmd.applied;
          return (
            <div
              key={cmd.id}
              className={`rounded-xl border transition-all p-4 ${
                isApplied
                  ? 'bg-[#11192b] border-cyan-500/40 shadow-sm shadow-cyan-950/20'
                  : 'bg-[#0e1423] border-[#1b263d] hover:border-slate-700'
              }`}
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-mono font-bold text-cyan-400 uppercase">
                      {cmd.category}
                    </span>
                    <span className={`text-[10px] font-mono px-2 py-0.5 rounded uppercase font-semibold ${
                      cmd.riskLevel === 'safe'
                        ? 'bg-emerald-950/60 text-emerald-400 border border-emerald-800/40'
                        : cmd.riskLevel === 'moderate'
                        ? 'bg-amber-950/60 text-amber-400 border border-amber-800/40'
                        : 'bg-rose-950/60 text-rose-400 border border-rose-800/40'
                    }`}>
                      {cmd.riskLevel}
                    </span>
                    {isApplied && (
                      <span className="flex items-center gap-1 text-[10px] font-mono text-emerald-400 font-semibold">
                        <CheckCircle2 className="w-3 h-3" /> ACTIVE
                      </span>
                    )}
                  </div>
                  <h4 className="text-sm font-bold text-slate-100">{cmd.title}</h4>
                  <p className="text-xs text-slate-400">{cmd.description}</p>
                </div>

                <div className="flex items-center gap-2 self-end sm:self-center shrink-0">
                  {isApplied ? (
                    <button
                      onClick={() => onRollbackCommand(cmd)}
                      disabled={isExecuting}
                      className="px-3.5 py-1.5 rounded-lg bg-slate-800/80 hover:bg-slate-700 text-slate-300 font-mono text-xs flex items-center gap-1.5 border border-slate-700 transition"
                    >
                      <RotateCcw className="w-3.5 h-3.5 text-cyan-400" />
                      <span>Rollback</span>
                    </button>
                  ) : (
                    <button
                      onClick={() => onApplyCommand(cmd)}
                      disabled={isExecuting}
                      className="px-4 py-1.5 rounded-lg bg-cyan-500 hover:bg-cyan-400 text-black font-semibold text-xs flex items-center gap-1.5 shadow-md shadow-cyan-500/20 active:scale-95 transition"
                    >
                      <Play className="w-3.5 h-3.5 fill-current" />
                      <span>Execute</span>
                    </button>
                  )}
                </div>
              </div>

              {/* Code command drawer preview */}
              <div className="mt-3 pt-3 border-t border-slate-800/70">
                <div className="p-2 rounded bg-[#070b13] border border-slate-800 font-mono text-[11px] text-slate-300 overflow-x-auto flex items-center justify-between gap-2">
                  <span className="truncate text-cyan-400">$ {cmd.command}</span>
                  <span className="text-[10px] text-slate-400 shrink-0 font-sans">
                    {cmd.requiresShizuku ? 'Requires Shizuku' : 'Standard Shell'}
                  </span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
