export type PerformanceProfile = 'battery' | 'balanced' | 'performance' | 'extreme' | 'gaming';

export type ShizukuStatus = 'PERMISSION_GRANTED' | 'PERMISSION_MISSING' | 'SERVICE_STOPPED';

export interface DeviceTelemetry {
  cpuUsage: number; // 0 - 100%
  cpuFreqMhz: number;
  cpuGovernor: string;
  cpuTempC: number;
  gpuUsage: number; // 0 - 100%
  gpuFreqMhz: number;
  ramUsedMb: number;
  ramTotalMb: number;
  zramUsedMb: number;
  zramTotalMb: number;
  batteryTempC: number;
  batteryLevel: number;
  batteryVoltageMv: number;
  batteryDrainMa: number;
  fpsTarget: number;
  currentRefreshRate: number;
  thermalThrottling: boolean;
  activeProcesses: number;
}

export interface AppPackage {
  packageName: string;
  appName: string;
  iconName: string;
  isSystem: boolean;
  category: 'game' | 'social' | 'system' | 'media' | 'utility';
  status: 'running' | 'cached' | 'frozen' | 'optimized';
  memoryMb: number;
  cpuPercent: number;
  artCompileStatus: 'speed-profile' | 'speed' | 'verify' | 'everything' | 'none';
  gameMode?: {
    enabled: boolean;
    fpsLock: number;
    downscale: number;
  };
}

export interface OptimizationCommand {
  id: string;
  category: string;
  title: string;
  description: string;
  command: string;
  rollbackCommand: string;
  applied: boolean;
  requiresShizuku: boolean;
  riskLevel: 'safe' | 'moderate' | 'advanced';
}

export interface ExecutionLog {
  id: string;
  timestamp: string;
  command: string;
  output: string;
  exitCode: number;
  success: boolean;
  canRollback: boolean;
  rollbackCommand?: string;
  rolledBack?: boolean;
}

export interface SystemTweaks {
  windowAnimationScale: number; // 0, 0.5, 1.0
  transitionAnimationScale: number;
  animatorDurationScale: number;
  aggressiveDoze: boolean;
  refreshRateLock: number; // 60, 90, 120, 144
  touchPollingRate: number; // 120, 240, 360
  forceDark: boolean;
  zramCompression: 'lz4' | 'zstd' | 'lzo';
  killBgAppsOnGameLaunch: boolean;
  bypassChargingAlert: boolean;
  disableGpuThrottling: boolean;
  dexoptMode: 'speed-profile' | 'speed' | 'everything';
}
