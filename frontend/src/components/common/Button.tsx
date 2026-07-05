import React from 'react';
import { cn } from '../../utils/cn';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'md', isLoading, children, disabled, ...props }, ref) => {
    
    // Map variants to available CSS classes or Tailwind classes
    const variantClass = variant === 'secondary' || variant === 'ghost' ? 'btn-secondary' : 'btn-primary';
    
    // The design system doesn't define size classes for buttons, so we use Tailwind for padding variations if needed, or stick to default .btn padding
    const sizeClass = size === 'sm' ? 'px-3 py-1.5 text-xs' : size === 'lg' ? 'px-6 py-3 text-base' : '';

    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={cn('btn', variantClass, sizeClass, className)}
        {...props}
      >
        {isLoading ? 'Loading...' : children}
      </button>
    );
  }
);
Button.displayName = 'Button';
