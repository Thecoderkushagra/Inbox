import { HamburgerButton } from './HamburgerButton';
import { ThemeToggle } from './ThemeToggle';
import { UserMenu } from './UserMenu';

interface TopBarProps {
  title: string;
  onMenuClick: () => void;
}

export const TopBar = ({ title, onMenuClick }: TopBarProps) => {
  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-surface px-4 lg:px-6">
      <div className="flex items-center gap-3">
        <HamburgerButton onClick={onMenuClick} />
        <h1 className="text-lg font-semibold text-text-h">{title}</h1>
      </div>
      <div className="flex items-center gap-2">
        <ThemeToggle />
        <UserMenu />
      </div>
    </header>
  );
};
