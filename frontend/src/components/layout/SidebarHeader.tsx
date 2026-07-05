import { MessageCircle, X } from 'lucide-react';

interface SidebarHeaderProps {
  onClose: () => void;
}

export const SidebarHeader = ({ onClose }: SidebarHeaderProps) => {
  return (
    <div className="flex h-16 items-center justify-between border-b border-border px-4">
      <div className="flex items-center gap-2">
        <MessageCircle className="h-6 w-6 text-accent" />
        <span className="text-lg font-bold text-text-h">Aurora Stream</span>
      </div>
      <button
        onClick={onClose}
        className="rounded-md p-1.5 text-text hover:bg-bg hover:text-text-h lg:hidden"
        aria-label="Close sidebar"
      >
        <X className="h-5 w-5" />
      </button>
    </div>
  );
};
