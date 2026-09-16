import React from 'react';
import { Terminal, CheckCircle, AlertOctagon, RotateCcw, X, ShieldAlert } from 'lucide-react';
import { ExecutionLog } from '../types';

interface ExecutionModalProps {
  log: ExecutionLog | null;
  onClose: () => void;
  onRollback?: (id: string) => void;
}

export const ExecutionModal: React.FC<ExecutionModalProps> = ({
  log,
  onClose,
  onRollback,
}) => {
  if (!log) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="bg-[#0e1422] border border-[#1f2b45] rounded-2xl w-full max-w-lg overflow-hidden shadow-2xl flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="px-5 py-3.5 border-b border-[#1b263e] flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className={`p-1.5 rounded-lg ${log.success ? 'bg-emerald-500/10 text-emerald-400' : 'bg-rose-500/10 text-rose-400'}`}>
              <Terminal className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-100">Command Execution Output</h3>
              <p className="text-[11px] text-slate-400 font-mono">Timestamp: {log.timestamp}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 space-y-4 overflow-y-auto">
          {/* Status banner */}
          <div className={`p-3 rounded-xl border flex items-center justify-between text-xs ${
            log.success
              ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-300'
              : 'bg-rose-500/10 border-rose-500/20 text-rose-300'
          }`}>
            <div className="flex items-center gap-2">
              {log.success ? (
                <CheckCircle className="w-4 h-4 text-emerald-400" />
              ) : (
                <AlertOctagon className="w-4 h-4 text-rose-400" />
              )}
              <span className="font-semibold">
                {log.success ? 'Execution Successful' : 'Execution Failed'} (Exit Code {log.exitCode})
              </span>
            </div>
            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-900/60 border border-slate-700">
              UID 1000 (System)
            </span>
          </div>

          {/* Invoked command */}
          <div>
            <label className="text-[10px] uppercase font-mono text-slate-400">Invoked Shell Command</label>
            <div className="mt-1 p-2.5 rounded-lg bg-[#080d17] border border-slate-800 font-mono text-xs text-cyan-300 overflow-x-auto select-all">
              $ {log.command}
            </div>
          </div>

          {/* Standard output terminal */}
          <div>
            <label className="text-[10px] uppercase font-mono text-slate-400">Process Standard Output (STDOUT)</label>
            <div className="mt-1 p-3 rounded-lg bg-[#070a12] border border-slate-800/80 font-mono text-xs text-slate-300 whitespace-pre-wrap max-h-48 overflow-y-auto select-all">
              {log.output}
            </div>
          </div>

          {/* Rollback safety */}
          {log.canRollback && log.rollbackCommand && (
            <div className="p-3 rounded-xl bg-cyan-950/20 border border-cyan-800/30 text-xs">
              <div className="flex items-center justify-between">
                <span className="text-cyan-300 font-medium">Automatic Rollback Available</span>
                {log.rolledBack ? (
                  <span className="text-[10px] font-mono text-emerald-400 px-2 py-0.5 rounded bg-emerald-950/40 border border-emerald-800">
                    Rolled Back
                  </span>
                ) : (
                  onRollback && (
                    <button
                      onClick={() => onRollback(log.id)}
                      className="flex items-center gap-1 text-[11px] font-semibold px-2.5 py-1 rounded bg-cyan-500/20 hover:bg-cyan-500/30 text-cyan-300 border border-cyan-500/40 transition"
                    >
                      <RotateCcw className="w-3 h-3" />
                      Restore Previous State
                    </button>
                  )
                )}
              </div>
              <p className="text-[11px] text-slate-400 mt-1 font-mono">
                Command: {log.rollbackCommand}
              </p>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-5 py-3 bg-[#0a0f1c] border-t border-[#1b263e] flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-200 transition"
          >
            Dismiss
          </button>
        </div>
      </div>
    </div>
  );
};
