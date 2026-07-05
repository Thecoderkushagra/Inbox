import { LogOut } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

export const SidebarFooter = () => {
  const { logout } = useAuth();

  return (
    <div className="border-t border-border p-3">
      <button
        onClick={logout}
        className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-text transition-colors hover:bg-accent-bg hover:text-accent"
        aria-label="Log out"
      >
        <LogOut className="h-5 w-5 shrink-0" />
        <span>Logout</span>
      </button>
    </div>
  );
};
