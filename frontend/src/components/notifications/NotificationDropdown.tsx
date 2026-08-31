import React, { useState, useEffect, useRef } from 'react';
import { Bell, Check, CheckCheck } from 'lucide-react';
import { useNotificationStore } from '@/stores/notificationStore';
import { formatRelativeTime } from '@/utils/formatDate';
import { cn } from '@/utils/cn';

export const NotificationDropdown: React.FC = () => {
  const {
    notifications,
    unreadCount,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
  } = useNotificationStore();

  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchNotifications();
    fetchUnreadCount();
  }, []);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div ref={dropdownRef} className="relative">
      <button
        onClick={() => {
          setIsOpen(!isOpen);
          if (!isOpen) fetchNotifications();
        }}
        className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors relative cursor-pointer"
        title="Notifications"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute top-1.5 right-1.5 w-2.5 h-2.5 bg-rose-500 rounded-full ring-2 ring-slate-900 animate-pulse" />
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden z-50 animate-in fade-in zoom-in-95">
          <div className="p-3.5 border-b border-slate-800 flex items-center justify-between">
            <h3 className="text-sm font-bold text-white">Notifications</h3>
            {notifications.some((n) => !n.read) && (
              <button
                onClick={() => markAllAsRead()}
                className="text-[11px] text-indigo-400 hover:text-indigo-300 font-medium flex items-center gap-1 cursor-pointer"
              >
                <CheckCheck className="w-3.5 h-3.5" />
                Mark all read
              </button>
            )}
          </div>

          <div className="max-h-80 overflow-y-auto divide-y divide-slate-800/60">
            {notifications.length === 0 ? (
              <div className="p-6 text-center text-xs text-slate-500">
                No notifications right now
              </div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.notificationId}
                  onClick={() => !n.read && markAsRead(n.notificationId)}
                  className={cn(
                    'p-3.5 flex items-start justify-between gap-3 hover:bg-slate-800/60 transition-colors cursor-pointer',
                    !n.read ? 'bg-indigo-600/10' : 'bg-transparent'
                  )}
                >
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-1.5">
                      <span className="text-xs font-semibold text-white truncate">{n.title}</span>
                      {!n.read && (
                        <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 flex-shrink-0" />
                      )}
                    </div>
                    <p className="text-xs text-slate-300 mt-0.5 leading-relaxed">{n.message}</p>
                    <span className="text-[10px] text-slate-500 mt-1 block">
                      {formatRelativeTime(n.createdAt)}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
