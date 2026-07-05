import { useLocation } from 'react-router-dom';
import { SidebarHeader } from './SidebarHeader';
import { SidebarNavigation } from './SidebarNavigation';
import { SidebarFooter } from './SidebarFooter';
import { cn } from '../../utils/cn';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export const Sidebar = ({ isOpen, onClose }: SidebarProps) => {
  const location = useLocation();

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-border bg-surface transition-transform duration-300 ease-in-out lg:static lg:translate-x-0',
          isOpen ? 'translate-x-0' : '-translate-x-full'
        )}
        role="navigation"
        aria-label="Main navigation"
      >
        <SidebarHeader onClose={onClose} />
        <SidebarNavigation currentPath={location.pathname} onItemClick={onClose} />
        <SidebarFooter />
      </aside>
    </>
  );
};
