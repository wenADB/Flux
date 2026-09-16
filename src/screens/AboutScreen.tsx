import React from 'react';
import { Info, Cpu, Smartphone, ShieldCheck, Heart, Sparkles, Terminal } from 'lucide-react';
import { DeviceTelemetry } from '../types';

interface AboutScreenProps {
  telemetry: DeviceTelemetry;
}

export const AboutScreen: React.FC<AboutScreenProps> = ({ telemetry }) => {
  const specs = [
    { label: 'Device Platform', value: 'Android 15 (VanillaIceCream)' },
    { label: 'Kernel Version', value: 'Linux 6.1.75-android15-11-gec6e7' },
    { label: 'Target SoC Architecture', value: 'Qualcomm Snapdragon 8 Gen 2 (ARM64-v8a)' },
    { label: 'CPU Cluster', value: '1x Cortex-X3 + 4x Cortex-A715/A710 + 3x Cortex-A510' },
    { label: 'GPU Subsystem', value: 'Adreno 740 Vulkan 1.3 / OpenGL ES 3.2' },
    { label: 'Memory Standard', value: '8192 MB LPDDR5X (Quad-Channel)' },
    { label: 'Privilege Model', value: 'Shizuku Android IPC (Rootless, UID 1000 system)' },
    { label: 'FLUX Engine Build', value: 'v2.4.0-release (Native IPC Bridge)' },
  ];

  return (
    <div className="space-y-5 pb-24 animate-fadeIn">
      {/* Hero */}
      <div className="bg-gradient-to-br from-[#121c2d] to-[#0c121e] border border-cyan-500/20 rounded-2xl p-6 text-center space-y-2">
        <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-cyan-400 to-blue-500 flex items-center justify-center mx-auto shadow-lg shadow-cyan-500/20 text-black font-extrabold text-xl">
          F
        </div>
        <h2 className="text-xl font-extrabold text-white tracking-wider">FLUX ENGINE</h2>
        <p className="text-xs text-slate-400 max-w-md mx-auto">
          The non-root Android performance & system optimization engine powered by Shizuku IPC.
        </p>
      </div>

      {/* Hardware Diagnostics Spec Sheet */}
      <div className="bg-[#0f1523] border border-[#1b263e] rounded-2xl p-5 space-y-3">
        <h3 className="text-sm font-bold text-slate-200 uppercase tracking-wider font-mono flex items-center gap-2">
          <Smartphone className="w-4 h-4 text-cyan-400" /> Host Hardware & Kernel Specs
        </h3>

        <div className="divide-y divide-slate-800">
          {specs.map((item, idx) => (
            <div key={idx} className="py-2.5 flex flex-col sm:flex-row sm:items-center justify-between gap-1 text-xs">
              <span className="text-slate-400 font-medium">{item.label}</span>
              <span className="font-mono text-slate-200 font-semibold">{item.value}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Rootless Security Disclaimer */}
      <div className="bg-[#0d1320] border border-slate-800 rounded-2xl p-5 space-y-2 text-xs text-slate-400">
        <h4 className="font-semibold text-slate-200 flex items-center gap-1.5 font-mono">
          <ShieldCheck className="w-4 h-4 text-emerald-400" /> Zero-Knox / Zero-Warranty Violation
        </h4>
        <p>
          FLUX operates strictly within the official Android system service IPC framework provided by Shizuku and wireless ADB debugging. It does not patch boot images, modify `/system` or `/vendor` partitions, or trip Knox/SafetyNet attestation.
        </p>
      </div>
    </div>
  );
};
