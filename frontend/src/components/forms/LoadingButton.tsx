import React from 'react';
import { Loader2 } from 'lucide-react';
import { Button, type ButtonProps } from '../common/Button';
import { cn } from '../../utils/cn';

export interface LoadingButtonProps extends ButtonProps {
  loading?: boolean;
}

export const LoadingButton = React.forwardRef<HTMLButtonElement, LoadingButtonProps>(
  ({ className, loading, children, disabled, ...props }, ref) => {
    return (
      <Button
        ref={ref}
        className={cn('w-full relative', className)}
        disabled={disabled || loading}
        {...props}
      >
        {loading && (
          <Loader2 className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 h-5 w-5 animate-spin" />
        )}
        <span className={cn('flex items-center gap-2', loading && 'invisible')}>
          {children}
        </span>
      </Button>
    );
  }
);
LoadingButton.displayName = 'LoadingButton';
