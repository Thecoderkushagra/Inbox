import { cn } from '../../utils/cn';

interface UserAvatarProps {
  src?: string;
  username: string;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeClasses = {
  sm: 'h-8 w-8 text-xs',
  md: 'h-10 w-10 text-sm',
  lg: 'h-12 w-12 text-base',
};

export const UserAvatar = ({ src, username, size = 'md', className }: UserAvatarProps) => {
  const initials = username
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  return (
    <div
      className={cn(
        'relative flex shrink-0 items-center justify-center overflow-hidden rounded-full bg-accent/10 font-medium text-accent',
        sizeClasses[size],
        className
      )}
    >
      {src ? (
        <img
          src={src}
          alt={username}
          className="h-full w-full object-cover"
        />
      ) : (
        <span>{initials}</span>
      )}
    </div>
  );
};
