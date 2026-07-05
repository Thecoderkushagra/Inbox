import { Link } from 'react-router-dom';
import type { LucideIcon } from 'lucide-react';
import { cn } from '../../utils/cn';

interface SidebarItemProps {
  icon: LucideIcon;
  label: string;
  path: string;
  isActive: boolean;
  onClick: () => void;
}

export const SidebarItem = ({ icon: Icon, label, path, isActive, onClick }: SidebarItemProps) => {
  return (
    <li>
      <Link
        to={path}
        onClick={onClick}
        className={cn(
          'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
          isActive
            ? 'bg-accent/10 text-accent'
            : 'text-text hover:bg-bg hover:text-text-h'
        )}
        aria-current={isActive ? 'page' : undefined}
      >
        <Icon className="h-5 w-5 shrink-0" />
        <span>{label}</span>
      </Link>
    </li>
  );
};
