import React from 'react';
import { cn } from '@/utils/cn';

interface BadgeProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger' | 'outline';
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'primary',
  className,
}) => {
  const variants = {
    primary: 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30',
    secondary: 'bg-slate-800 text-slate-300 border border-slate-700',
    success: 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30',
    warning: 'bg-amber-500/20 text-amber-300 border border-amber-500/30',
    danger: 'bg-rose-500/20 text-rose-300 border border-rose-500/30',
    outline: 'border border-slate-700 text-slate-400',
  };

  return (
    <span
      className={cn(
        'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium select-none',
        variants[variant],
        className
      )}
    >
      {children}
    </span>
  );
};
