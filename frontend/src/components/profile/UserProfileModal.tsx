import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { usersApi, conversationsApi, presenceApi } from '@/api';
import { UserProfile, PresenceResponse } from '@/types';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { Modal } from '@/components/ui/Modal';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import {
  MessageSquare,
  MapPin,
  Globe,
  Calendar,
  ShieldCheck,
  Loader2,
  ExternalLink,
  User,
} from 'lucide-react';
import { formatRelativeTime } from '@/utils/formatDate';
import toast from 'react-hot-toast';

interface UserProfileModalProps {
  userId: string | null;
  isOpen: boolean;
  onClose: () => void;
  fallbackUsername?: string;
  fallbackDisplayName?: string;
  fallbackAvatarUrl?: string;
}

export const UserProfileModal: React.FC<UserProfileModalProps> = ({
  userId,
  isOpen,
  onClose,
  fallbackUsername,
  fallbackDisplayName,
  fallbackAvatarUrl,
}) => {
  const navigate = useNavigate();
  const { user: currentUser } = useAuthStore();
  const { fetchConversations, setActiveConversationId } = useChatStore();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [presence, setPresence] = useState<PresenceResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isStartingChat, setIsStartingChat] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isSelf = currentUser?.id === userId;

  useEffect(() => {
    if (!isOpen || !userId) {
      setProfile(null);
      setPresence(null);
      setError(null);
      return;
    }

    let isMounted = true;
    setIsLoading(true);
    setError(null);

    Promise.all([
      usersApi.getPublicProfile(userId).catch(() => null),
      presenceApi.getPresence(userId).catch(() => null),
    ])
      .then(([profileRes, presenceRes]) => {
        if (!isMounted) return;
        if (profileRes) {
          setProfile(profileRes);
        } else {
          // Fallback if profile not created yet
          setProfile({
            userId,
            displayName: fallbackDisplayName || fallbackUsername || 'User',
            avatarUrl: fallbackAvatarUrl,
          });
        }
        if (presenceRes) {
          setPresence(presenceRes);
        }
      })
      .catch((err: unknown) => {
        if (!isMounted) return;
        const msg = err instanceof Error ? err.message : 'Could not load profile';
        setError(msg);
      })
      .finally(() => {
        if (isMounted) setIsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, [isOpen, userId, fallbackDisplayName, fallbackUsername, fallbackAvatarUrl]);

  const handleStartChat = async () => {
    if (!userId || isSelf) return;
    setIsStartingChat(true);
    try {
      const conv = await conversationsApi.createPrivateConversation(userId);
      await fetchConversations();
      setActiveConversationId(conv.id);
      navigate(`/inbox/${conv.id}`);
      toast.success(`Chat started with ${profile?.displayName || fallbackDisplayName || fallbackUsername || 'user'}`);
      onClose();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to start conversation';
      toast.error(msg);
    } finally {
      setIsStartingChat(false);
    }
  };

  const displayName = profile?.displayName || fallbackDisplayName || fallbackUsername || 'User';
  const avatarUrl = profile?.avatarUrl || fallbackAvatarUrl;
  const username = fallbackUsername || profile?.displayName || 'user';

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="User Profile"
      description="View user information and connect directly"
      maxWidth="md"
    >
      {isLoading ? (
        <div className="py-16 flex flex-col items-center justify-center space-y-3">
          <Loader2 className="w-8 h-8 text-indigo-400 animate-spin" />
          <p className="text-xs text-slate-400">Loading user profile...</p>
        </div>
      ) : error ? (
        <div className="py-8 text-center space-y-3">
          <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-xs text-rose-400">
            {error}
          </div>
          <Button variant="secondary" size="sm" onClick={onClose}>
            Close
          </Button>
        </div>
      ) : (
        <div className="space-y-5">
          {/* Header Banner & Avatar */}
          <div className="relative rounded-2xl overflow-hidden bg-gradient-to-r from-indigo-900/60 via-purple-900/40 to-slate-900 border border-slate-800">
            {profile?.bannerUrl ? (
              <img
                src={profile.bannerUrl}
                alt="Profile Banner"
                className="w-full h-28 object-cover opacity-60"
              />
            ) : (
              <div className="w-full h-24 bg-gradient-to-r from-indigo-600/30 to-purple-600/20" />
            )}

            <div className="p-4 pt-0 -mt-10 flex items-end gap-3.5">
              <div className="relative rounded-full ring-4 ring-slate-900 shadow-xl">
                <Avatar
                  name={displayName}
                  src={avatarUrl}
                  size="xl"
                  status={presence?.status}
                />
              </div>

              <div className="pb-1 min-w-0 flex-1">
                <div className="flex items-center gap-1.5 flex-wrap">
                  <h2 className="text-lg font-bold text-white tracking-tight truncate">
                    {displayName}
                  </h2>
                  {profile?.verified && (
                    <ShieldCheck className="w-4 h-4 text-cyan-400 flex-shrink-0" title="Verified Account" />
                  )}
                  {isSelf && (
                    <Badge variant="primary" className="text-[10px]">
                      You
                    </Badge>
                  )}
                </div>
                <p className="text-xs text-slate-400 truncate">@{username}</p>
              </div>
            </div>
          </div>

          {/* Status badge */}
          <div className="flex items-center justify-between px-1 text-xs">
            <div className="flex items-center gap-2">
              <span className="text-slate-400">Status:</span>
              {presence?.status === 'ONLINE' ? (
                <span className="inline-flex items-center gap-1.5 text-emerald-400 font-medium">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                  Online
                </span>
              ) : presence?.status === 'AWAY' ? (
                <span className="inline-flex items-center gap-1.5 text-amber-400 font-medium">
                  <span className="w-2 h-2 rounded-full bg-amber-500" />
                  Away
                </span>
              ) : (
                <span className="inline-flex items-center gap-1.5 text-slate-400">
                  <span className="w-2 h-2 rounded-full bg-slate-500" />
                  {presence?.lastSeen ? `Last seen ${formatRelativeTime(presence.lastSeen)}` : 'Offline'}
                </span>
              )}
            </div>
          </div>

          {/* Bio */}
          <div className="p-4 rounded-2xl bg-slate-950/60 border border-slate-800/80 space-y-1">
            <h4 className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">About</h4>
            <p className="text-xs text-slate-300 leading-relaxed whitespace-pre-wrap">
              {profile?.bio || 'No bio provided yet.'}
            </p>
          </div>

          {/* Details (Location, Website, etc.) */}
          {(profile?.location || profile?.website || profile?.birthDate || profile?.gender) && (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs">
              {profile.location && (
                <div className="flex items-center gap-2 p-2.5 rounded-xl bg-slate-950/40 border border-slate-800 text-slate-300">
                  <MapPin className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                  <span className="truncate">{profile.location}</span>
                </div>
              )}

              {profile.website && (
                <a
                  href={profile.website.startsWith('http') ? profile.website : `https://${profile.website}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 p-2.5 rounded-xl bg-slate-950/40 border border-slate-800 text-indigo-400 hover:text-indigo-300 hover:border-indigo-500/40 transition-all truncate"
                >
                  <Globe className="w-3.5 h-3.5 flex-shrink-0" />
                  <span className="truncate flex-1">{profile.website.replace(/^https?:\/\//, '')}</span>
                  <ExternalLink className="w-3 h-3 flex-shrink-0 opacity-70" />
                </a>
              )}

              {profile.birthDate && (
                <div className="flex items-center gap-2 p-2.5 rounded-xl bg-slate-950/40 border border-slate-800 text-slate-300">
                  <Calendar className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                  <span className="truncate">Born {profile.birthDate}</span>
                </div>
              )}

              {profile.gender && (
                <div className="flex items-center gap-2 p-2.5 rounded-xl bg-slate-950/40 border border-slate-800 text-slate-300">
                  <User className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
                  <span className="truncate capitalize">{profile.gender}</span>
                </div>
              )}
            </div>
          )}

          {/* Action Buttons */}
          <div className="pt-3 border-t border-slate-800 flex items-center justify-end gap-2.5">
            <Button variant="ghost" size="sm" onClick={onClose} className="rounded-xl">
              Close
            </Button>

            {isSelf ? (
              <Button
                size="sm"
                variant="outline"
                onClick={() => {
                  onClose();
                  navigate('/settings');
                }}
                className="rounded-xl"
              >
                Edit My Profile
              </Button>
            ) : (
              <Button
                size="sm"
                onClick={handleStartChat}
                isLoading={isStartingChat}
                className="rounded-xl gap-2 px-4 shadow-lg shadow-indigo-600/20"
              >
                <MessageSquare className="w-4 h-4" />
                <span>Direct Message</span>
              </Button>
            )}
          </div>
        </div>
      )}
    </Modal>
  );
};
