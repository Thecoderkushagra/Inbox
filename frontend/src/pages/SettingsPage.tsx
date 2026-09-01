import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { usersApi } from '@/api';
import { ProfileVisibility } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { MobileHeader } from '@/components/navigation/MobileHeader';
import {
  User,
  Settings,
  Shield,
  Palette,
  Bell,
  LogOut,
  Check,
  Lock,
  Sparkles,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { cn } from '@/utils/cn';

type SettingsTab = 'profile' | 'appearance' | 'security';

export const SettingsPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, profile, updateProfile, logout } = useAuthStore();

  const [activeTab, setActiveTab] = useState<SettingsTab>('profile');
  const [displayName, setDisplayName] = useState(profile?.displayName || user?.username || '');
  const [bio, setBio] = useState(profile?.bio || '');
  const [statusMessage, setStatusMessage] = useState(profile?.statusMessage || '');
  const [visibility, setVisibility] = useState<ProfileVisibility>(profile?.visibility || 'PUBLIC');
  const [avatarUrl, setAvatarUrl] = useState(profile?.avatarUrl || '');
  const [isLoading, setIsLoading] = useState(false);

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await updateProfile({
        displayName: displayName.trim(),
        bio: bio.trim(),
        statusMessage: statusMessage.trim(),
        visibility,
        avatarUrl: avatarUrl.trim() || undefined,
      });
      toast.success('Profile updated successfully!');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to update profile';
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = async () => {
    if (confirm('Are you sure you want to sign out?')) {
      await logout();
      navigate('/login');
    }
  };

  return (
    <div className="h-full w-full flex flex-col bg-slate-950 text-slate-100 overflow-hidden pb-16 md:pb-0">
      <MobileHeader title="Settings" showBack={true} backTo="/inbox" />

      <div className="flex-1 overflow-y-auto p-4 sm:p-8 max-w-4xl mx-auto w-full space-y-6">
        {/* Desktop Header */}
        <div className="hidden md:block">
          <h1 className="text-2xl font-extrabold text-white tracking-tight flex items-center gap-2">
            <span>Settings &amp; Preferences</span>
            <Settings className="w-5 h-5 text-indigo-400" />
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Manage your personal profile, messaging preferences, and account security.
          </p>
        </div>

        {/* Tab Switcher */}
        <div className="flex rounded-2xl bg-slate-900 p-1 border border-slate-800">
          <button
            onClick={() => setActiveTab('profile')}
            className={cn(
              'flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold transition-all cursor-pointer',
              activeTab === 'profile'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            )}
          >
            <User className="w-4 h-4" />
            <span>Profile</span>
          </button>
          <button
            onClick={() => setActiveTab('appearance')}
            className={cn(
              'flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold transition-all cursor-pointer',
              activeTab === 'appearance'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            )}
          >
            <Palette className="w-4 h-4" />
            <span>Appearance</span>
          </button>
          <button
            onClick={() => setActiveTab('security')}
            className={cn(
              'flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold transition-all cursor-pointer',
              activeTab === 'security'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            )}
          >
            <Shield className="w-4 h-4" />
            <span>Security</span>
          </button>
        </div>

        {/* PROFILE TAB */}
        {activeTab === 'profile' && (
          <form onSubmit={handleSaveProfile} className="space-y-6">
            {/* Avatar Header Preview */}
            <div className="p-6 rounded-3xl bg-slate-900/60 border border-slate-800 flex flex-col sm:flex-row items-center gap-6">
              <Avatar
                name={displayName || user?.username || 'User'}
                src={avatarUrl || profile?.avatarUrl}
                size="xl"
                status="ONLINE"
              />
              <div className="flex-1 text-center sm:text-left space-y-1">
                <h3 className="text-base font-bold text-white">
                  {displayName || user?.username}
                </h3>
                <p className="text-xs text-slate-400">@{user?.username} &bull; {user?.email}</p>
                <div className="pt-2">
                  <Input
                    placeholder="Paste image URL for avatar..."
                    value={avatarUrl}
                    onChange={(e) => setAvatarUrl(e.target.value)}
                    className="bg-slate-950 text-xs"
                  />
                </div>
              </div>
            </div>

            {/* Profile Fields Card */}
            <div className="p-6 rounded-3xl bg-slate-900/60 border border-slate-800 space-y-4">
              <Input
                label="Display Name"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                placeholder="Your full name or nickname"
                required
                className="bg-slate-950"
              />

              <Input
                label="Status Message"
                value={statusMessage}
                onChange={(e) => setStatusMessage(e.target.value)}
                placeholder="What's your current status? (e.g. Working remotely)"
                className="bg-slate-950"
              />

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
                  Bio / About
                </label>
                <textarea
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  placeholder="Tell others a bit about yourself..."
                  rows={3}
                  className="w-full bg-slate-950 text-slate-100 placeholder-slate-500 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm outline-none focus:border-indigo-500 transition-all resize-none"
                />
              </div>

              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
                  Profile Visibility
                </label>
                <select
                  value={visibility}
                  onChange={(e) => setVisibility(e.target.value as ProfileVisibility)}
                  className="w-full bg-slate-950 text-slate-100 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm outline-none focus:border-indigo-500 transition-all cursor-pointer"
                >
                  <option value="PUBLIC">Public — Visible to everyone</option>
                  <option value="CONTACTS_ONLY">Contacts Only — Visible to people in your chats</option>
                  <option value="PRIVATE">Private — Hidden from search directory</option>
                </select>
              </div>
            </div>

            <div className="flex justify-end">
              <Button type="submit" isLoading={isLoading} className="rounded-2xl px-6">
                <Check className="w-4 h-4 mr-1.5" />
                <span>Save Changes</span>
              </Button>
            </div>
          </form>
        )}

        {/* APPEARANCE TAB */}
        {activeTab === 'appearance' && (
          <div className="space-y-4">
            <div className="p-6 rounded-3xl bg-slate-900/60 border border-slate-800 space-y-4">
              <h3 className="text-sm font-bold text-white tracking-tight">Theme &amp; Visuals</h3>
              <p className="text-xs text-slate-400">
                Inbox uses an Obsidian Dark mode optimized for OLED and high-contrast accessibility.
              </p>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
                <div className="p-4 rounded-2xl bg-indigo-600/15 border-2 border-indigo-500 flex items-center justify-between cursor-pointer">
                  <div>
                    <div className="text-sm font-bold text-white">Obsidian Dark</div>
                    <div className="text-xs text-indigo-300">Active Theme</div>
                  </div>
                  <Check className="w-5 h-5 text-indigo-400" />
                </div>
              </div>
            </div>
          </div>
        )}

        {/* SECURITY TAB */}
        {activeTab === 'security' && (
          <div className="space-y-6">
            <div className="p-6 rounded-3xl bg-slate-900/60 border border-slate-800 space-y-4">
              <h3 className="text-sm font-bold text-white tracking-tight flex items-center gap-2">
                <Lock className="w-4 h-4 text-emerald-400" />
                <span>Encryption &amp; Privacy</span>
              </h3>
              <p className="text-xs text-slate-400 leading-relaxed">
                Your conversations are end-to-end encrypted with state-of-the-art cryptographic protocols. Messages are stored securely in MongoDB Atlas and transported over secure WebSockets.
              </p>
              <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-2xl text-xs text-emerald-400 font-medium">
                End-to-End Encryption active for all direct and group channels.
              </div>
            </div>

            <div className="p-6 rounded-3xl bg-slate-900/60 border border-slate-800 space-y-4">
              <h3 className="text-sm font-bold text-white tracking-tight">Account Session</h3>
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-xs font-semibold text-white">Signed in as @{user?.username}</div>
                  <div className="text-[10px] text-slate-400">{user?.email}</div>
                </div>
                <Button variant="danger" size="sm" onClick={handleLogout} className="rounded-xl">
                  <LogOut className="w-3.5 h-3.5 mr-1" />
                  <span>Sign Out</span>
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
