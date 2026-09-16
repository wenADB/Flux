import React from 'react';
import { History, CheckCircle2, AlertOctagon, RotateCcw, Download, Trash2, Terminal, Code2 } from 'lucide-react';
import { ExecutionLog } from '../types';

interface HistoryScreenProps {
  logs: ExecutionLog[];
  onRollback: (id: string) => void;
  onClearLogs: () => void;
  onViewLogDetails: (log: ExecutionLog) => void;
}

export const HistoryScreen: React.FC<HistoryScreenProps> = ({
  logs,
  onRollback,
  onClearLogs,
  onViewLogDetails,
}) => {
  const exportJson = () => {
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(logs, null, 2));
    const dlAnchor = document.createElement('a');
    dlAnchor.setAttribute('href', dataStr);
    dlAnchor.setAttribute('download', `flux_audit_log_${Date.now()}.json`);
    dlAnchor.click();
  };

  return (
    <div className="space-y-5 pb-24 animate-fadeIn">
      {/* Header */}
      <div className="bg-[#0f1523] border border-[#1b263e] rounded-2xl p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-cyan-500/10 text-cyan-400">
            <History className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-base font-bold text-slate-100">Execution Audit Log & Backups</h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Immutable journal of every shell command dispatched through Shizuku IPC.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 self-end sm:self-center">
          <button
            onClick={exportJson}
            disabled={logs.length === 0}
            className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-mono text-slate-200 border border-slate-700 flex items-center gap-1.5 transition disabled:opacity-40"
          >
            <Download className="w-3.5 h-3.5 text-cyan-400" />
            <span>Export JSON</span>
          </button>
          <button
            onClick={onClearLogs}
            disabled={logs.length === 0}
            className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-rose-400 border border-slate-700 transition disabled:opacity-40"
            title="Clear Audit Journal"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Logs List */}
      {logs.length === 0 ? (
        <div className="p-12 text-center bg-[#0d1320] border border-dashed border-slate-800 rounded-2xl space-y-2">
          <Terminal className="w-8 h-8 text-slate-600 mx-auto" />
          <p className="text-xs text-slate-400">No commands have been executed yet in this session.</p>
        </div>
      ) : (
        <div className="space-y-2.5">
          {logs.map((item) => (
            <div
              key={item.id}
              className="bg-[#0f1523] border border-[#1b263e] rounded-xl p-3.5 hover:border-slate-700 transition space-y-2"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  {item.success ? (
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                  ) : (
                    <AlertOctagon className="w-4 h-4 text-rose-400 shrink-0" />
                  )}
                  <span className="text-xs font-mono font-bold text-slate-200">
                    Exit {item.exitCode}
                  </span>
                  <span className="text-[11px] font-mono text-slate-500">
                    {item.timestamp}
                  </span>
                  {item.rolledBack && (
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-950/60 border border-emerald-800 text-emerald-400 font-semibold">
                      Rolled Back
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-2 self-end sm:self-center">
                  <button
                    onClick={() => onViewLogDetails(item)}
                    className="text-[11px] font-mono text-cyan-400 hover:underline flex items-center gap-1"
                  >
                    View Details
                  </button>
                  {item.canRollback && !item.rolledBack && (
                    <button
                      onClick={() => onRollback(item.id)}
                      className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 font-mono text-[11px] flex items-center gap-1 border border-slate-700 transition"
                    >
                      <RotateCcw className="w-3 h-3 text-cyan-400" />
                      <span>Rollback</span>
                    </button>
                  )}
                </div>
              </div>

              {/* Command text preview */}
              <div className="p-2 rounded bg-[#080d16] border border-slate-800 font-mono text-[11px] text-cyan-300 overflow-x-auto truncate">
                $ {item.command}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
