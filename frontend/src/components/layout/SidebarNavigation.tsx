import {
  MessageSquare,
  Users,
  UsersRound,
  Search,
  Bell,
  User,
  Settings,
} from 'lucide-react';
import { Routes } from '../../constants';
import { SidebarItem } from './SidebarItem';

const navigationItems = [
  { icon: MessageSquare, label: 'Chats', path: Routes.CHAT },
  { icon: Users, label: 'Friends', path: Routes.FRIENDS },
  { icon: UsersRound, label: 'Groups', path: Routes.GROUPS },
  { icon: Search, label: 'Search', path: Routes.SEARCH },
  { icon: Bell, label: 'Notifications', path: Routes.NOTIFICATIONS },
  { icon: User, label: 'Profile', path: Routes.PROFILE },
  { icon: Settings, label: 'Settings', path: Routes.SETTINGS },
];

interface SidebarNavigationProps {
  currentPath: string;
  onItemClick: () => void;
}

export const SidebarNavigation = ({ currentPath, onItemClick }: SidebarNavigationProps) => {
  return (
    <nav className="flex-1 overflow-y-auto px-3 py-4">
      <ul className="space-y-1">
        {navigationItems.map((item) => (
          <SidebarItem
            key={item.path}
            icon={item.icon}
            label={item.label}
            path={item.path}
            isActive={currentPath === item.path}
            onClick={onItemClick}
          />
        ))}
      </ul>
    </nav>
  );
};
