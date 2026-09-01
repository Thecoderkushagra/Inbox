import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { cn } from '@/utils/cn';

interface MobileHeaderProps {
  title: string;
  subtitle?: string;
  showBack?: boolean;
  backTo?: string;
  onBack?: () => void;
  rightAction?: React.ReactNode;
  className?: string;
}

export const MobileHeader: React.FC<MobileHeaderProps> = ({
  title,
  subtitle,
  showBack = false,
  backTo,
  onBack,
  rightAction,
  className,
}) => {
  const navigate = useNavigate();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (backTo) {
      navigate(backTo);
    } else {
      navigate(-1);
    }
  };

  return (
    <header
      className={cn(
        'md:hidden h-14 px-3 bg-slate-950/90 backdrop-blur-md border-b border-slate-800/80 flex items-center justify-between z-30 sticky top-0 left-0 right-0 select-none flex-shrink-0',
        className
      )}
    >
      <div className="flex items-center gap-2 min-w-0 flex-1">
        {showBack && (
          <button
            onClick={handleBack}
            className="p-1.5 -ml-1 text-slate-400 hover:text-white active:bg-slate-800 rounded-xl transition-colors cursor-pointer"
            aria-label="Go Back"
          >
            <ChevronLeft className="w-6 h-6" />
          </button>
        )}
        <div className="min-w-0 flex-1">
          <h1 className="text-sm font-bold text-white tracking-tight truncate leading-tight">
            {title}
          </h1>
          {subtitle && (
            <p className="text-[10px] text-slate-400 truncate leading-tight mt-0.5">
              {subtitle}
            </p>
          )}
        </div>
      </div>

      {rightAction && (
        <div className="flex items-center gap-1 flex-shrink-0">
          {rightAction}
        </div>
      )}
    </header>
  );
};
