import { DeviceTelemetry, PerformanceProfile, ShizukuStatus, AppPackage, ExecutionLog, SystemTweaks, OptimizationCommand } from '../types';

const INITIAL_TELEMETRY: DeviceTelemetry = {
  cpuUsage: 28,
  cpuFreqMhz: 2419,
  cpuGovernor: 'schedutil',
  cpuTempC: 38.5,
  gpuUsage: 14,
  gpuFreqMhz: 587,
  ramUsedMb: 5240,
  ramTotalMb: 8192,
  zramUsedMb: 1120,
  zramTotalMb: 4096,
  batteryTempC: 33.2,
  batteryLevel: 78,
  batteryVoltageMv: 4120,
  batteryDrainMa: 420,
  fpsTarget: 120,
  currentRefreshRate: 120,
  thermalThrottling: false,
  activeProcesses: 64,
};

const INITIAL_TWEAKS: SystemTweaks = {
  windowAnimationScale: 0.5,
  transitionAnimationScale: 0.5,
  animatorDurationScale: 0.5,
  aggressiveDoze: true,
  refreshRateLock: 120,
  touchPollingRate: 240,
  forceDark: true,
  zramCompression: 'zstd',
  killBgAppsOnGameLaunch: true,
  bypassChargingAlert: false,
  disableGpuThrottling: false,
  dexoptMode: 'speed-profile',
};

export const INITIAL_PACKAGES: AppPackage[] = [
  {
    packageName: 'com.miHoYo.GenshinImpact',
    appName: 'Genshin Impact',
    iconName: 'Gamepad2',
    isSystem: false,
    category: 'game',
    status: 'optimized',
    memoryMb: 1840,
    cpuPercent: 34,
    artCompileStatus: 'speed',
    gameMode: { enabled: true, fpsLock: 60, downscale: 0.9 },
  },
  {
    packageName: 'com.activision.callofduty.shooter',
    appName: 'Call of Duty: Warzone Mobile',
    iconName: 'Crosshair',
    isSystem: false,
    category: 'game',
    status: 'running',
    memoryMb: 1420,
    cpuPercent: 22,
    artCompileStatus: 'speed-profile',
    gameMode: { enabled: true, fpsLock: 120, downscale: 1.0 },
  },
  {
    packageName: 'com.spotify.music',
    appName: 'Spotify',
    iconName: 'Music',
    isSystem: false,
    category: 'media',
    status: 'cached',
    memoryMb: 240,
    cpuPercent: 1.2,
    artCompileStatus: 'speed-profile',
  },
  {
    packageName: 'com.instagram.android',
    appName: 'Instagram',
    iconName: 'Camera',
    isSystem: false,
    category: 'social',
    status: 'cached',
    memoryMb: 380,
    cpuPercent: 0.8,
    artCompileStatus: 'speed-profile',
  },
  {
    packageName: 'com.android.chrome',
    appName: 'Google Chrome',
    iconName: 'Globe',
    isSystem: false,
    category: 'utility',
    status: 'running',
    memoryMb: 610,
    cpuPercent: 3.5,
    artCompileStatus: 'speed',
  },
  {
    packageName: 'com.google.android.youtube',
    appName: 'YouTube',
    iconName: 'Tv',
    isSystem: false,
    category: 'media',
    status: 'cached',
    memoryMb: 320,
    cpuPercent: 1.1,
    artCompileStatus: 'speed-profile',
  },
  {
    packageName: 'com.android.systemui',
    appName: 'System UI',
    iconName: 'Cpu',
    isSystem: true,
    category: 'system',
    status: 'running',
    memoryMb: 450,
    cpuPercent: 4.8,
    artCompileStatus: 'speed',
  },
  {
    packageName: 'com.whatsapp',
    appName: 'WhatsApp',
    iconName: 'MessageSquare',
    isSystem: false,
    category: 'social',
    status: 'running',
    memoryMb: 290,
    cpuPercent: 1.5,
    artCompileStatus: 'speed-profile',
  }
];

export const OPTIMIZATION_COMMANDS: OptimizationCommand[] = [
  {
    id: 'trim_caches',
    category: 'Memory & Storage',
    title: 'Drop FS Caches & Trim Inodes',
    description: 'Frees dirty pages and system buffer caches without killing foreground tasks.',
    command: 'echo 3 > /proc/sys/vm/drop_caches && sync',
    rollbackCommand: '# Cached buffers automatically repopulate on demand',
    applied: false,
    requiresShizuku: true,
    riskLevel: 'safe',
  },
  {
    id: 'zram_zstd',
    category: 'Memory & Swap',
    title: 'Enable zRAM zstd High Compression',
    description: 'Switches kernel RAM swap algorithm to modern zstd for ~35% higher compression ratio.',
    command: 'swapoff /dev/block/zram0 && echo zstd > /sys/block/zram0/comp_algorithm && swapon /dev/block/zram0',
    rollbackCommand: 'echo lzo-rle > /sys/block/zram0/comp_algorithm',
    applied: false,
    requiresShizuku: true,
    riskLevel: 'moderate',
  },
  {
    id: 'animation_scales',
    category: 'Display & Responsiveness',
    title: 'Set Animations to 0.5x Snappiness',
    description: 'Halves transition and animator latency across the Android window manager.',
    command: 'settings put global window_animation_scale 0.5 && settings put global transition_animation_scale 0.5 && settings put global animator_duration_scale 0.5',
    rollbackCommand: 'settings put global window_animation_scale 1.0 && settings put global transition_animation_scale 1.0 && settings put global animator_duration_scale 1.0',
    applied: true,
    requiresShizuku: false,
    riskLevel: 'safe',
  },
  {
    id: 'aggressive_doze',
    category: 'Battery & Power',
    title: 'Force Aggressive Deep Doze',
    description: 'Forces instant device idle state when display turns off, halting standby wakelocks.',
    command: 'dumpsys deviceidle force-idle deep',
    rollbackCommand: 'dumpsys deviceidle unforce',
    applied: true,
    requiresShizuku: true,
    riskLevel: 'safe',
  },
  {
    id: 'thermal_mitigation',
    category: 'Thermal & Clocks',
    title: 'Thermal Mitigation & Governor Balance',
    description: 'Stabilizes CPU cluster clock stepping to avoid abrupt thermal throttling spikes during gaming.',
    command: 'cmd power set-fixed-performance-mode-enabled true',
    rollbackCommand: 'cmd power set-fixed-performance-mode-enabled false',
    applied: false,
    requiresShizuku: true,
    riskLevel: 'moderate',
  },
  {
    id: 'art_speed_profile',
    category: 'ART Compiler',
    title: 'Cloud ART Profile Optimization (Dexopt)',
    description: 'Triggers Android Runtime Ahead-Of-Time dex2oat compilation for frequently used methods.',
    command: 'cmd package compile -m speed-profile -a',
    rollbackCommand: '# Non-reversible without app reinstallation or compile -r',
    applied: false,
    requiresShizuku: true,
    riskLevel: 'safe',
  }
];

class FluxEngineService {
  private telemetry: DeviceTelemetry = { ...INITIAL_TELEMETRY };
  private profile: PerformanceProfile = 'balanced';
  private shizukuStatus: ShizukuStatus = 'PERMISSION_GRANTED';
  private tweaks: SystemTweaks = { ...INITIAL_TWEAKS };
  private packages: AppPackage[] = [...INITIAL_PACKAGES];
  private commands: OptimizationCommand[] = [...OPTIMIZATION_COMMANDS];
  private logs: ExecutionLog[] = [];
  private listeners: (() => void)[] = [];

  constructor() {
    this.loadFromStorage();
    this.startTelemetryLoop();
  }

  private loadFromStorage() {
    try {
      const savedProfile = localStorage.getItem('flux_profile');
      if (savedProfile) this.profile = savedProfile as PerformanceProfile;

      const savedTweaks = localStorage.getItem('flux_tweaks');
      if (savedTweaks) this.tweaks = { ...this.tweaks, ...JSON.parse(savedTweaks) };

      const savedLogs = localStorage.getItem('flux_logs');
      if (savedLogs) this.logs = JSON.parse(savedLogs);
      else {
        // Initial audit log entries
        this.logs = [
          {
            id: 'log_boot_01',
            timestamp: new Date(Date.now() - 3600000).toLocaleTimeString(),
            command: 'shizuku check-permission --uid 1000',
            output: 'UID 1000 granted privilege IPC android.permission.DUMP',
            exitCode: 0,
            success: true,
            canRollback: false,
          },
          {
            id: 'log_boot_02',
            timestamp: new Date(Date.now() - 3500000).toLocaleTimeString(),
            command: 'settings put global window_animation_scale 0.5',
            output: 'Success: Global settings updated',
            exitCode: 0,
            success: true,
            canRollback: true,
            rollbackCommand: 'settings put global window_animation_scale 1.0',
          }
        ];
      }
    } catch (e) {
      console.warn('Could not read storage', e);
    }
  }

  private saveToStorage() {
    try {
      localStorage.setItem('flux_profile', this.profile);
      localStorage.setItem('flux_tweaks', JSON.stringify(this.tweaks));
      localStorage.setItem('flux_logs', JSON.stringify(this.logs.slice(0, 50)));
    } catch (e) {
      console.warn('Could not save storage', e);
    }
  }

  public subscribe(cb: () => void) {
    this.listeners.push(cb);
    return () => {
      this.listeners = this.listeners.filter(l => l !== cb);
    };
  }

  private notify() {
    this.listeners.forEach(cb => cb());
  }

  private startTelemetryLoop() {
    setInterval(() => {
      // Dynamic realistic telemetry drift depending on active profile
      const cpuBase = this.profile === 'gaming' ? 65 : this.profile === 'performance' ? 45 : this.profile === 'battery' ? 15 : 28;
      const cpuJitter = (Math.random() - 0.5) * 12;
      const newCpu = Math.max(8, Math.min(98, Math.round(cpuBase + cpuJitter)));

      const tempBase = this.profile === 'gaming' ? 44.5 : this.profile === 'performance' ? 41 : this.profile === 'battery' ? 34 : 37.5;
      const tempJitter = (Math.random() - 0.5) * 1.5;
      const newTemp = parseFloat((tempBase + tempJitter).toFixed(1));

      const ramUsed = this.profile === 'gaming' ? 6100 : this.profile === 'battery' ? 4200 : 5180;
      const ramJitter = Math.round((Math.random() - 0.5) * 150);

      this.telemetry = {
        ...this.telemetry,
        cpuUsage: newCpu,
        cpuTempC: newTemp,
        ramUsedMb: ramUsed + ramJitter,
        batteryTempC: parseFloat((newTemp - 4.5 + Math.random() * 0.4).toFixed(1)),
        batteryDrainMa: this.profile === 'gaming' ? 840 : this.profile === 'battery' ? 260 : 430 + Math.round((Math.random() - 0.5) * 40),
        thermalThrottling: newTemp > 45,
        gpuUsage: this.profile === 'gaming' ? Math.round(70 + Math.random() * 20) : Math.round(10 + Math.random() * 15),
      };

      this.notify();
    }, 2000);
  }

  // Getters
  public getTelemetry(): DeviceTelemetry { return this.telemetry; }
  public getProfile(): PerformanceProfile { return this.profile; }
  public getShizukuStatus(): ShizukuStatus { return this.shizukuStatus; }
  public getTweaks(): SystemTweaks { return this.tweaks; }
  public getPackages(): AppPackage[] { return this.packages; }
  public getCommands(): OptimizationCommand[] { return this.commands; }
  public getLogs(): ExecutionLog[] { return this.logs; }

  // Actions
  public setProfile(profile: PerformanceProfile) {
    this.profile = profile;
    const profileCmdMap: Record<PerformanceProfile, string> = {
      battery: 'cmd power set-mode 1 && dumpsys deviceidle force-idle',
      balanced: 'cmd power set-mode 0',
      performance: 'cmd power set-fixed-performance-mode-enabled true',
      extreme: 'echo performance > /sys/devices/system/cpu/cpufreq/policy0/scaling_governor',
      gaming: 'cmd power set-fixed-performance-mode-enabled true && settings put system min_refresh_rate 120',
    };

    this.executeShizukuCommand(
      `Apply profile: ${profile.toUpperCase()}`,
      profileCmdMap[profile],
      'cmd power set-mode 0'
    );
    this.saveToStorage();
    this.notify();
  }

  public setShizukuStatus(status: ShizukuStatus) {
    this.shizukuStatus = status;
    this.notify();
  }

  public updateTweak<K extends keyof SystemTweaks>(key: K, value: SystemTweaks[K]) {
    this.tweaks[key] = value;
    this.saveToStorage();
    this.notify();
  }

  public async executeShizukuCommand(
    title: string,
    command: string,
    rollbackCommand?: string
  ): Promise<ExecutionLog> {
    const startTime = new Date().toLocaleTimeString();
    
    // Simulate ADB command execution
    await new Promise(r => setTimeout(r, 600));

    const success = this.shizukuStatus === 'PERMISSION_GRANTED' || !command.startsWith('echo') && !command.startsWith('swapoff');
    const exitCode = success ? 0 : 13;
    const output = success
      ? `[Success] ${command}\n→ Returned status 0 (PID ${Math.floor(1000 + Math.random() * 9000)})`
      : `[Error: Permission Denied] Privileged shell required (exit status ${exitCode})`;

    const log: ExecutionLog = {
      id: 'log_' + Date.now(),
      timestamp: startTime,
      command,
      output,
      exitCode,
      success,
      canRollback: !!rollbackCommand,
      rollbackCommand,
    };

    this.logs = [log, ...this.logs];
    this.saveToStorage();
    this.notify();
    return log;
  }

  public async rollbackLog(id: string): Promise<boolean> {
    const target = this.logs.find(l => l.id === id);
    if (!target || !target.rollbackCommand || target.rolledBack) return false;

    await this.executeShizukuCommand(`Rollback: ${target.command}`, target.rollbackCommand);
    target.rolledBack = true;
    this.saveToStorage();
    this.notify();
    return true;
  }

  public clearLogs() {
    this.logs = [];
    this.saveToStorage();
    this.notify();
  }

  public async optimizeApp(pkgName: string): Promise<boolean> {
    const app = this.packages.find(p => p.packageName === pkgName);
    if (!app) return false;

    await this.executeShizukuCommand(
      `ART compile speed-profile: ${pkgName}`,
      `cmd package compile -m speed-profile -f ${pkgName}`
    );
    app.artCompileStatus = 'speed-profile';
    app.status = 'optimized';
    this.notify();
    return true;
  }

  public async freezeApp(pkgName: string): Promise<boolean> {
    const app = this.packages.find(p => p.packageName === pkgName);
    if (!app) return false;

    const isFrozen = app.status === 'frozen';
    const cmd = isFrozen
      ? `pm enable ${pkgName}`
      : `am force-stop ${pkgName} && pm disable-user --user 0 ${pkgName}`;

    await this.executeShizukuCommand(
      `${isFrozen ? 'Unfreeze' : 'Freeze'} app: ${pkgName}`,
      cmd,
      isFrozen ? `pm disable-user --user 0 ${pkgName}` : `pm enable ${pkgName}`
    );

    app.status = isFrozen ? 'cached' : 'frozen';
    this.notify();
    return true;
  }

  public async runFullOptimization(): Promise<number> {
    // 1. Drop caches
    await this.executeShizukuCommand('Quick Drop Caches', 'echo 3 > /proc/sys/vm/drop_caches && sync');
    // 2. Kill background cached processes
    await this.executeShizukuCommand('Trim Inactive RAM', 'am kill-all');
    // 3. Set snappy animations
    await this.executeShizukuCommand(
      'Apply 0.5x animations',
      'settings put global window_animation_scale 0.5 && settings put global transition_animation_scale 0.5'
    );

    // Update state
    this.telemetry.ramUsedMb = Math.max(3800, this.telemetry.ramUsedMb - 1100);
    this.telemetry.cpuUsage = Math.max(12, this.telemetry.cpuUsage - 15);
    this.notify();
    return 1100;
  }
}

export const fluxEngine = new FluxEngineService();
