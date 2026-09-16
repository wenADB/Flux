import React, { useState, useEffect } from 'react';
import { TopBar } from './components/TopBar';
import { BottomNav, TabKey } from './components/BottomNav';
import { ShizukuModal } from './components/ShizukuModal';
import { ExecutionModal } from './components/ExecutionModal';
import { DashboardScreen } from './screens/DashboardScreen';
import { OptimizerScreen } from './screens/OptimizerScreen';
import { GameSpaceScreen } from './screens/GameSpaceScreen';
import { ArtScreen } from './screens/ArtScreen';
import { AppsScreen } from './screens/AppsScreen';
import { HistoryScreen } from './screens/HistoryScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { AboutScreen } from './screens/AboutScreen';
import { fluxEngine } from './services/fluxEngine';
import { ExecutionLog, OptimizationCommand, AppPackage } from './types';
import { ArrowLeft } from 'lucide-react';

export const App: React.FC = () => {
  const [telemetry, setTelemetry] = useState(fluxEngine.getTelemetry());
  const [profile, setProfile] = useState(fluxEngine.getProfile());
  const [shizukuStatus, setShizukuStatus] = useState(fluxEngine.getShizukuStatus());
  const [tweaks, setTweaks] = useState(fluxEngine.getTweaks());
  const [packages, setPackages] = useState(fluxEngine.getPackages());
  const [commands, setCommands] = useState(fluxEngine.getCommands());
  const [logs, setLogs] = useState(fluxEngine.getLogs());

  const [currentTab, setCurrentTab] = useState<TabKey>('dashboard');
  const [activeOverlay, setActiveOverlay] = useState<'settings' | 'about' | null>(null);

  const [isShizukuModalOpen, setIsShizukuModalOpen] = useState(false);
  const [activeExecutionLog, setActiveExecutionLog] = useState<ExecutionLog | null>(null);
  const [isOptimizing, setIsOptimizing] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  useEffect(() => {
    const unsub = fluxEngine.subscribe(() => {
      setTelemetry(fluxEngine.getTelemetry());
      setProfile(fluxEngine.getProfile());
      setShizukuStatus(fluxEngine.getShizukuStatus());
      setTweaks(fluxEngine.getTweaks());
      setPackages(fluxEngine.getPackages());
      setCommands(fluxEngine.getCommands());
      setLogs(fluxEngine.getLogs());
    });
    return unsub;
  }, []);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage(null);
    }, 3500);
  };

  const handleQuickBoost = async () => {
    setIsOptimizing(true);
    const freed = await fluxEngine.runFullOptimization();
    setIsOptimizing(false);
    showToast(`⚡ Boost complete: Reclaimed ~${freed} MB of cached RAM & dirty buffers!`);
  };

  const handleApplyCommand = async (cmd: OptimizationCommand) => {
    setIsOptimizing(true);
    const log = await fluxEngine.executeShizukuCommand(cmd.title, cmd.command, cmd.rollbackCommand);
    setIsOptimizing(false);
    cmd.applied = true;
    setActiveExecutionLog(log);
    showToast(`Tweak applied: ${cmd.title}`);
  };

  const handleRollbackCommand = async (cmd: OptimizationCommand) => {
    setIsOptimizing(true);
    const log = await fluxEngine.executeShizukuCommand(`Rollback: ${cmd.title}`, cmd.rollbackCommand);
    setIsOptimizing(false);
    cmd.applied = false;
    setActiveExecutionLog(log);
    showToast(`Reverted: ${cmd.title}`);
  };

  const handleBatchArt = async (filter: string) => {
    setIsOptimizing(true);
    const log = await fluxEngine.executeShizukuCommand(
      `ART Dexopt compilation [${filter}]`,
      `cmd package compile -m ${filter} -a`
    );
    setIsOptimizing(false);
    setActiveExecutionLog(log);
    showToast(`ART dexopt batch compilation complete for all apps!`);
  };

  const handleCompileApp = async (pkgName: string) => {
    setIsOptimizing(true);
    await fluxEngine.optimizeApp(pkgName);
    setIsOptimizing(false);
    showToast(`Compiled AOT bytecode for ${pkgName}`);
  };

  const handleFreezeApp = async (pkgName: string) => {
    await fluxEngine.freezeApp(pkgName);
    showToast(`Toggled freeze state for ${pkgName}`);
  };

  const handleLaunchGameMode = async (pkg: AppPackage) => {
    setIsOptimizing(true);
    fluxEngine.setProfile('gaming');
    const log = await fluxEngine.executeShizukuCommand(
      `Esports Game Boost: ${pkg.appName}`,
      `cmd power set-fixed-performance-mode-enabled true && settings put system min_refresh_rate ${tweaks.refreshRateLock} && am start -n ${pkg.packageName}/.MainActivity`
    );
    setIsOptimizing(false);
    setActiveExecutionLog(log);
    showToast(`⚡ Esports Game Boost engaged for ${pkg.appName}!`);
  };

  return (
    <div className="min-h-screen bg-[#0a0d14] text-slate-100 flex flex-col selection:bg-cyan-500/20">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed top-16 left-1/2 -translate-x-1/2 z-50 bg-[#121c2e] border border-cyan-500/50 text-cyan-300 px-4 py-2.5 rounded-xl shadow-2xl text-xs font-semibold flex items-center gap-2 animate-fadeIn">
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Top Bar Header */}
      <TopBar
        shizukuStatus={shizukuStatus}
        currentProfile={profile}
        cpuTemp={telemetry.cpuTempC}
        onOpenShizuku={() => setIsShizukuModalOpen(true)}
        onOpenSettings={() => setActiveOverlay('settings')}
        onOpenAbout={() => setActiveOverlay('about')}
      />

      {/* Main Container */}
      <main className="flex-1 w-full max-w-4xl mx-auto px-4 pt-4">
        {activeOverlay ? (
          <div className="space-y-4">
            <button
              onClick={() => setActiveOverlay(null)}
              className="flex items-center gap-2 text-xs font-mono text-cyan-400 hover:text-cyan-300 font-semibold px-2 py-1 rounded-lg bg-slate-900/80 border border-slate-800 transition"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Back to Main Dashboard</span>
            </button>

            {activeOverlay === 'settings' ? (
              <SettingsScreen
                shizukuStatus={shizukuStatus}
                tweaks={tweaks}
                onUpdateTweak={(key, val) => fluxEngine.updateTweak(key, val)}
                onOpenShizuku={() => setIsShizukuModalOpen(true)}
                onResetEngine={() => {
                  localStorage.clear();
                  window.location.reload();
                }}
              />
            ) : (
              <AboutScreen telemetry={telemetry} />
            )}
          </div>
        ) : (
          <>
            {currentTab === 'dashboard' && (
              <DashboardScreen
                telemetry={telemetry}
                profile={profile}
                onSelectProfile={(p) => fluxEngine.setProfile(p)}
                onQuickBoost={handleQuickBoost}
                isOptimizing={isOptimizing}
              />
            )}

            {currentTab === 'optimizer' && (
              <OptimizerScreen
                commands={commands}
                onApplyCommand={handleApplyCommand}
                onRollbackCommand={handleRollbackCommand}
                isExecuting={isOptimizing}
              />
            )}

            {currentTab === 'gamespace' && (
              <GameSpaceScreen
                packages={packages}
                tweaks={tweaks}
                onUpdateTweak={(key, val) => fluxEngine.updateTweak(key, val)}
                onLaunchGameMode={handleLaunchGameMode}
              />
            )}

            {currentTab === 'art' && (
              <ArtScreen
                packages={packages}
                onCompileApp={handleCompileApp}
                onBatchCompile={handleBatchArt}
                isCompiling={isOptimizing}
              />
            )}

            {currentTab === 'apps' && (
              <AppsScreen
                packages={packages}
                onFreezeApp={handleFreezeApp}
                onOptimizeApp={handleCompileApp}
              />
            )}

            {currentTab === 'history' && (
              <HistoryScreen
                logs={logs}
                onRollback={(id) => fluxEngine.rollbackLog(id)}
                onClearLogs={() => fluxEngine.clearLogs()}
                onViewLogDetails={(log) => setActiveExecutionLog(log)}
              />
            )}
          </>
        )}
      </main>

      {/* Shizuku Privileged Bridge Modal */}
      <ShizukuModal
        isOpen={isShizukuModalOpen}
        status={shizukuStatus}
        onClose={() => setIsShizukuModalOpen(false)}
        onStatusChange={(st) => fluxEngine.setShizukuStatus(st)}
      />

      {/* Execution Terminal Output Modal */}
      <ExecutionModal
        log={activeExecutionLog}
        onClose={() => setActiveExecutionLog(null)}
        onRollback={(id) => fluxEngine.rollbackLog(id)}
      />

      {/* Bottom Navigation */}
      {!activeOverlay && (
        <BottomNav
          currentTab={currentTab}
          onSelectTab={(tab) => setCurrentTab(tab)}
        />
      )}
    </div>
  );
};
