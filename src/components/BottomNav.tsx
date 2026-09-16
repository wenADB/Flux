import React from 'react';
import {
  LayoutDashboard,
  Zap,
  Gamepad2,
  Sliders,
  Terminal,
  Cpu,
  History,
  Boxes,
} from 'lucide-react';

export type TabKey = 'dashboard' | 'optimizer' | 'gamespace' | 'art' | 'history' | 'apps';

interface BottomNavProps {
  currentTab: TabKey;
  onSelectTab: (tab: TabKey) => void;
}

export const BottomNav: React.FC<BottomNavProps> = ({ currentTab, onSelectTab }) => {
  const tabs = [
    { key: 'dashboard' as TabKey, label: 'Monitor', icon: LayoutDashboard },
    { key: 'optimizer' as TabKey, label: 'Optimizer', icon: Zap },
    { key: 'gamespace' as TabKey, label: 'Game Space', icon: Gamepad2 },
    { key: 'art' as TabKey, label: 'ART Dexopt', icon: Cpu },
    { key: 'apps' as TabKey, label: 'Apps', icon: Boxes },
    { key: 'history' as TabKey, label: 'Audit Log', icon: History },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#0c101c]/95 backdrop-blur-lg border-t border-[#1a233a] px-2 py-1.5 flex justify-around items-center">
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const isActive = currentTab === tab.key;
        return (
          <button
            key={tab.key}
            onClick={() => onSelectTab(tab.key)}
            className={`flex flex-col items-center justify-center py-1 px-2.5 rounded-xl transition-all duration-200 relative ${
              isActive
                ? 'text-cyan-400 font-semibold scale-105'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            {isActive && (
              <span className="absolute -top-1 w-6 h-1 rounded-full bg-cyan-400 shadow-sm shadow-cyan-400" />
            )}
            <Icon className={`w-5 h-5 mb-0.5 ${isActive ? 'stroke-[2.5]' : 'stroke-2'}`} />
            <span className="text-[10px] tracking-tight">{tab.label}</span>
          </button>
        );
      })}
    </nav>
  );
};
