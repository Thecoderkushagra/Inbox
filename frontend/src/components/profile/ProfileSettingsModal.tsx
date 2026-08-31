import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Avatar } from '@/components/ui/Avatar';
import { useAuthStore } from '@/stores/authStore';
import { User, Globe, MapPin, AlignLeft, UserCircle } from 'lucide-react';

interface ProfileSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ProfileSettingsModal: React.FC<ProfileSettingsModalProps> = ({
  isOpen,
  onClose,
}) => {
  const { user, profile, updateProfile, isLoading, error } = useAuthStore();

  const [displayName, setDisplayName] = useState(profile?.displayName || user?.username || '');
  const [bio, setBio] = useState(profile?.bio || '');
  const [avatarUrl, setAvatarUrl] = useState(profile?.avatarUrl || '');
  const [location, setLocation] = useState(profile?.location || '');
  const [website, setWebsite] = useState(profile?.website || '');
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await updateProfile({
        displayName: displayName.trim(),
        bio: bio.trim(),
        avatarUrl: avatarUrl.trim() || undefined,
        location: location.trim() || undefined,
        website: website.trim() || undefined,
      });
      onClose();
    } catch {
      // Handled via toast in store
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Profile Settings"
      description="Update your personal details and avatar"
      maxWidth="md"
    >
      <form onSubmit={handleSave} className="space-y-4">
        {error && (
          <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-xs text-rose-400">
            {error}
          </div>
        )}

        {successMessage && (
          <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-xs text-emerald-400">
            {successMessage}
          </div>
        )}

        {/* Avatar Preview */}
        <div className="flex items-center gap-4 p-4 bg-slate-950/60 rounded-2xl border border-slate-800">
          <Avatar name={displayName || 'User'} src={avatarUrl} size="lg" />
          <div className="flex-1 min-w-0">
            <div className="text-sm font-semibold text-white truncate">
              {displayName || 'User'}
            </div>
            <div className="text-xs text-slate-400">@{user?.username}</div>
            <div className="text-[10px] text-slate-500">{user?.email}</div>
          </div>
        </div>

        <Input
          label="Display Name"
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
          placeholder="Your full name"
          required
          leftIcon={<User className="w-4 h-4 text-slate-500" />}
          className="bg-slate-950"
        />

        <Input
          label="Avatar Image URL (Cloudinary or Direct Link)"
          value={avatarUrl}
          onChange={(e) => setAvatarUrl(e.target.value)}
          placeholder="https://res.cloudinary.com/..."
          leftIcon={<UserCircle className="w-4 h-4 text-slate-500" />}
          className="bg-slate-950"
        />

        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
            Bio
          </label>
          <textarea
            value={bio}
            onChange={(e) => setBio(e.target.value)}
            placeholder="Tell us about yourself..."
            rows={3}
            className="w-full bg-slate-950 text-slate-100 placeholder-slate-500 border border-slate-700/80 rounded-xl px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Location"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder="e.g. San Francisco, CA"
            leftIcon={<MapPin className="w-4 h-4 text-slate-500" />}
            className="bg-slate-950"
          />
          <Input
            label="Website"
            value={website}
            onChange={(e) => setWebsite(e.target.value)}
            placeholder="https://example.com"
            leftIcon={<Globe className="w-4 h-4 text-slate-500" />}
            className="bg-slate-950"
          />
        </div>

        <div className="pt-2 flex justify-end gap-2">
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" isLoading={isLoading}>
            Save Changes
          </Button>
        </div>
      </form>
    </Modal>
  );
};
