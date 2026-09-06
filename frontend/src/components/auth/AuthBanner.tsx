import React, { useState, useEffect } from 'react';
import {
  Shield,
  Zap,
  Users,
  CheckCheck,
  Lock,
  ArrowRight,
} from 'lucide-react';
import inboxLogo from '@/assets/inbox-logo.png';

interface FeatureItem {
  id: string;
  tag: string;
  title: string;
  description: string;
  previewType: 'chat' | 'group' | 'sync';
}

const FEATURES: FeatureItem[] = [
  {
    id: 'e2ee',
    tag: 'Encrypted Monolith',
    title: 'Private by default, zero compromises.',
    description:
      'Messages, media, and credentials are protected end-to-end. No tracking, no data brokers, and instant account activation.',
    previewType: 'chat',
  },
  {
    id: 'performance',
    tag: 'Java 21 Virtual Threads',
    title: 'Sub-10 millisecond delivery.',
    description:
      'Engineered on non-blocking virtual threads and Upstash Redis pub/sub. Scale to tens of thousands of real-time connections seamlessly.',
    previewType: 'sync',
  },
  {
    id: 'collaboration',
    tag: 'Modern Spaces',
    title: 'Organized direct and group conversations.',
    description:
      'Create groups up to 100 participants, manage roles, share rich media via Cloudinary, and track real-time delivery receipts.',
    previewType: 'group',
  },
];

export const AuthBanner: React.FC = React.memo(() => {
  const [activeIdx, setActiveIdx] = useState(0);
  const [isPaused, setIsPaused] = useState(false);

  // Auto-advance tabs every 7 seconds, pause on hover
  useEffect(() => {
    if (isPaused) return;
    const timer = setInterval(() => {
      setActiveIdx((prev) => (prev + 1) % FEATURES.length);
    }, 7000);
    return () => clearInterval(timer);
  }, [isPaused]);

  const currentFeature = FEATURES[activeIdx];

  return (
    <div
      className="h-full w-full flex flex-col justify-between p-10 xl:p-14 text-slate-300 relative select-none"
      onMouseEnter={() => setIsPaused(true)}
      onMouseLeave={() => setIsPaused(false)}
    >
      {/* Subtle radial atmosphere (calm, dark, non-intrusive) */}
      <div className="absolute top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_80%_60%_at_20%_-10%,rgba(99,102,241,0.08),transparent)] pointer-events-none" />

      {/* Top Brand Anchor */}
      <div className="relative z-10">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-slate-900/90 border border-slate-800/80 rounded-xl shadow-sm">
            <img src={inboxLogo} alt="Inbox" className="w-6 h-6 object-contain" />
          </div>
          <div>
            <span className="text-base font-bold text-white tracking-tight">Inbox</span>
            <span className="text-xs text-slate-500 font-medium ml-2">Messaging Platform</span>
          </div>
        </div>
      </div>

      {/* Center Showcase: Calm, Elegant UI Preview */}
      <div className="relative z-10 my-auto py-8 max-w-lg">
        {/* Dynamic Tag */}
        <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-slate-900 border border-slate-800 text-[11px] font-mono text-indigo-300 mb-3">
          <span className="w-1.5 h-1.5 rounded-full bg-indigo-400" />
          <span>{currentFeature.tag}</span>
        </div>

        {/* Feature Headline & Body */}
        <h2 className="text-2xl xl:text-3xl font-bold text-white tracking-tight leading-snug">
          {currentFeature.title}
        </h2>
        <p className="text-sm text-slate-400 mt-2.5 leading-relaxed">
          {currentFeature.description}
        </p>

        {/* UI Snippet Card */}
        <div className="mt-6 p-4 rounded-2xl bg-slate-900/70 border border-slate-800/80 shadow-2xl backdrop-blur-sm">
          {currentFeature.previewType === 'chat' && (
            <div className="space-y-3 text-xs">
              <div className="flex items-center justify-between pb-2.5 border-b border-slate-800 text-[11px] text-slate-400">
                <div className="flex items-center gap-2">
                  <div className="w-5 h-5 rounded-full bg-slate-800 text-slate-300 flex items-center justify-center font-bold text-[10px]">
                    AR
                  </div>
                  <span className="text-slate-300 font-medium">Alex Rivera</span>
                </div>
                <div className="flex items-center gap-1 text-[10px] text-emerald-400">
                  <Lock className="w-3 h-3" />
                  <span>E2EE Active</span>
                </div>
              </div>

              <div className="space-y-2">
                <div className="flex flex-col items-start max-w-[82%]">
                  <div className="bg-slate-800/80 border border-slate-700/50 text-slate-200 px-3 py-2 rounded-2xl rounded-tl-sm leading-relaxed">
                    Ready for the release review? All service health checks passed.
                  </div>
                  <span className="text-[10px] text-slate-500 mt-1 ml-1">11:20 AM</span>
                </div>

                <div className="flex flex-col items-end">
                  <div className="bg-indigo-600 text-white px-3 py-2 rounded-2xl rounded-tr-sm leading-relaxed max-w-[82%]">
                    Looks great. Endpoints are responding in under 8ms.
                  </div>
                  <div className="flex items-center gap-1 text-[10px] text-slate-400 mt-1 mr-1">
                    <span>11:21 AM</span>
                    <CheckCheck className="w-3.5 h-3.5 text-cyan-300 inline" />
                  </div>
                </div>
              </div>
            </div>
          )}

          {currentFeature.previewType === 'sync' && (
            <div className="space-y-3 text-xs">
              <div className="flex items-center justify-between pb-2.5 border-b border-slate-800 text-[11px] text-slate-400">
                <span className="font-mono text-slate-300">Live Telemetry</span>
                <span className="text-emerald-400 flex items-center gap-1.5 font-medium">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                  Cluster Online
                </span>
              </div>

              <div className="grid grid-cols-3 gap-2 text-center pt-1">
                <div className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800">
                  <div className="text-[10px] text-slate-400 uppercase font-mono">Ping</div>
                  <div className="text-sm font-bold text-white font-mono mt-0.5">7.4 ms</div>
                </div>
                <div className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800">
                  <div className="text-[10px] text-slate-400 uppercase font-mono">Runtime</div>
                  <div className="text-sm font-bold text-white font-mono mt-0.5">GraalVM</div>
                </div>
                <div className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800">
                  <div className="text-[10px] text-slate-400 uppercase font-mono">Protocols</div>
                  <div className="text-sm font-bold text-white font-mono mt-0.5">WSS/TLS</div>
                </div>
              </div>
            </div>
          )}

          {currentFeature.previewType === 'group' && (
            <div className="space-y-3 text-xs">
              <div className="flex items-center justify-between pb-2.5 border-b border-slate-800 text-[11px] text-slate-400">
                <div className="flex items-center gap-2">
                  <Users className="w-3.5 h-3.5 text-indigo-400" />
                  <span className="text-slate-300 font-medium">Core Engineering</span>
                </div>
                <span className="text-slate-400 text-[10px]">18 members active</span>
              </div>

              <div className="space-y-2">
                <div className="flex items-start gap-2 max-w-[85%]">
                  <div className="w-5 h-5 rounded-full bg-violet-600 text-white flex items-center justify-center font-bold text-[10px] flex-shrink-0 mt-0.5">
                    M
                  </div>
                  <div>
                    <div className="bg-slate-800/80 border border-slate-700/50 text-slate-200 px-3 py-2 rounded-2xl rounded-tl-sm leading-relaxed">
                      Updated the conversation state and read-receipt pipeline.
                    </div>
                    <span className="text-[10px] text-slate-500 mt-1 ml-1 block">Marcus · 10:48 AM</span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Clean Interactive Tab Controls */}
        <div className="flex items-center gap-2 mt-5">
          {FEATURES.map((feat, idx) => (
            <button
              key={feat.id}
              type="button"
              onClick={() => setActiveIdx(idx)}
              className={`h-1 rounded-full transition-all duration-300 cursor-pointer ${
                activeIdx === idx ? 'w-8 bg-indigo-500' : 'w-2 bg-slate-800 hover:bg-slate-700'
              }`}
              aria-label={`Switch to ${feat.tag}`}
            />
          ))}
        </div>
      </div>

      {/* Bottom Restrained Footer */}
      <div className="relative z-10 pt-4 border-t border-slate-800/60 flex items-center justify-between text-xs text-slate-500">
        <span>Instant user activation · Zero outbound tracking</span>
        <div className="flex items-center gap-1.5 text-slate-400">
          <Shield className="w-3.5 h-3.5 text-indigo-400" />
          <span>Stateless Security</span>
        </div>
      </div>
    </div>
  );
});

AuthBanner.displayName = 'AuthBanner';
