import { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { User, Settings, LogOut, ChevronDown } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { Routes } from '../../constants';
import { UserAvatar } from './UserAvatar';
import { cn } from '../../utils/cn';

export const UserMenu = () => {
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const { user, logout } = useAuth();

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      document.addEventListener('keydown', handleEscape);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen]);

  if (!user) return null;

  return (
    <div className="relative" ref={menuRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 rounded-lg p-1.5 hover:bg-bg transition-colors"
        aria-expanded={isOpen}
        aria-haspopup="true"
        aria-label="User menu"
      >
        <UserAvatar username={user.displayName || user.username} src={user.avatarUrl} size="sm" />
        <span className="hidden text-sm font-medium text-text-h sm:block">
          {user.displayName || user.username}
        </span>
        <ChevronDown className={cn('h-4 w-4 text-text transition-transform', isOpen && 'rotate-180')} />
      </button>

      {isOpen && (
        <div
          className="absolute right-0 top-full mt-2 w-56 rounded-lg border border-border bg-surface shadow"
          role="menu"
          aria-orientation="vertical"
        >
          <div className="border-b border-border px-4 py-3">
            <p className="text-sm font-medium text-text-h">{user.displayName || user.username}</p>
            <p className="text-xs text-text">{user.email}</p>
          </div>
          <div className="p-1">
            <Link
              to={Routes.PROFILE}
              onClick={() => setIsOpen(false)}
              className="flex items-center gap-3 rounded-md px-3 py-2 text-sm text-text hover:bg-bg hover:text-text-h transition-colors"
              role="menuitem"
            >
              <User className="h-4 w-4" />
              Profile
            </Link>
            <Link
              to={Routes.SETTINGS}
              onClick={() => setIsOpen(false)}
              className="flex items-center gap-3 rounded-md px-3 py-2 text-sm text-text hover:bg-bg hover:text-text-h transition-colors"
              role="menuitem"
            >
              <Settings className="h-4 w-4" />
              Settings
            </Link>
          </div>
          <div className="border-t border-border p-1">
            <button
              onClick={logout}
              className="flex w-full items-center gap-3 rounded-md px-3 py-2 text-sm text-text hover:bg-accent-bg hover:text-accent transition-colors"
              role="menuitem"
            >
              <LogOut className="h-4 w-4" />
              Logout
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
