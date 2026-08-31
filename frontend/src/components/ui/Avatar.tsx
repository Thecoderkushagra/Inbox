import React from 'react';
import { PresenceStatus } from '@/types';
import { cn } from '@/utils/cn';

interface AvatarProps {
  src?: string | null;
  name: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  status?: PresenceStatus;
  className?: string;
}

export const Avatar: React.FC<AvatarProps> = ({
  src,
  name,
  size = 'md',
  status,
  className,
}) => {
  const sizeClasses = {
    sm: 'w-8 h-8 text-xs',
    md: 'w-10 h-10 text-sm',
    lg: 'w-12 h-12 text-base',
    xl: 'w-16 h-16 text-lg',
  };

  const statusDotSizes = {
    sm: 'w-2.5 h-2.5 border-2',
    md: 'w-3 h-3 border-2',
    lg: 'w-3.5 h-3.5 border-2',
    xl: 'w-4 h-4 border-2',
  };

  const initials = name
    ? name
        .split(' ')
        .map((n) => n[0])
        .slice(0, 2)
        .join('')
        .toUpperCase()
    : '?';

  // Generate consistent color from name
  const colors = [
    'bg-indigo-600',
    'bg-violet-600',
    'bg-sky-600',
    'bg-emerald-600',
    'bg-rose-600',
    'bg-amber-600',
    'bg-teal-600',
  ];
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  const colorClass = colors[Math.abs(hash) % colors.length];

  return (
    <div className={cn('relative inline-flex flex-shrink-0', className)}>
      <div
        className={cn(
          'rounded-full overflow-hidden flex items-center justify-center font-semibold text-white shadow-sm ring-1 ring-white/10 select-none',
          sizeClasses[size],
          colorClass
        )}
      >
        {src ? (
          <img
            src={src}
            alt={name}
            className="w-full h-full object-cover"
            onError={(e) => {
              // Hide broken image to show initials
              (e.target as HTMLImageElement).style.display = 'none';
            }}
          />
        ) : (
          <span>{initials}</span>
        )}
      </div>

      {status && (
        <span
          className={cn(
            'absolute bottom-0 right-0 rounded-full border-slate-950',
            statusDotSizes[size],
            status === 'ONLINE' && 'bg-emerald-500',
            status === 'AWAY' && 'bg-amber-500',
            status === 'OFFLINE' && 'bg-slate-500'
          )}
          title={`Status: ${status}`}
        />
      )}
    </div>
  );
};
