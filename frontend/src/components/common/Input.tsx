import React from 'react';
import { cn } from '../../utils/cn';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  error?: string;
  label?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, error, label, id, ...props }, ref) => {
    const inputId = id || Math.random().toString(36).substring(7);
    return (
      <div className="flex flex-col gap-1.5">
        {label && <label htmlFor={inputId} className="text-sm font-medium text-text-h">{label}</label>}
        <input
          id={inputId}
          ref={ref}
          className={cn(
            "w-full bg-code-bg border border-transparent rounded-2xl px-4 py-2.5 text-sm text-text-h outline-none transition-colors",
            "focus:border-accent-border",
            "placeholder:text-text",
            error && "border-accent",
            className
          )}
          {...props}
        />
        {error && <span className="text-xs text-accent mt-1">{error}</span>}
      </div>
    );
  }
);
Input.displayName = 'Input';
