import { Menu } from 'lucide-react';

interface HamburgerButtonProps {
  onClick: () => void;
}

export const HamburgerButton = ({ onClick }: HamburgerButtonProps) => {
  return (
    <button
      onClick={onClick}
      className="rounded-md p-2 text-text hover:bg-bg hover:text-text-h lg:hidden"
      aria-label="Toggle sidebar"
    >
      <Menu className="h-5 w-5" />
    </button>
  );
};
