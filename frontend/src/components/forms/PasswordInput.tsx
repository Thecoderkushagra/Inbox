import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { type InputProps } from '../common/Input';
import { cn } from '../../utils/cn';

export const PasswordInput = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, error, label, id, ...props }, ref) => {
    const [showPassword, setShowPassword] = useState(false);
    const inputId = id || Math.random().toString(36).substring(7);

    return (
      <div className="flex flex-col gap-1.5">
        {label && <label htmlFor={inputId} className="text-sm font-medium text-text-h">{label}</label>}
        <div className="relative flex items-center">
          <input
            id={inputId}
            type={showPassword ? 'text' : 'password'}
            ref={ref}
            className={cn(
              "w-full bg-code-bg border border-transparent rounded-2xl px-4 py-2.5 pr-10 text-sm text-text-h outline-none transition-colors",
              "focus:border-accent-border",
              "placeholder:text-text",
              error && "border-accent",
              className
            )}
            {...props}
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 flex items-center justify-center text-text hover:text-text-h focus:outline-none"
            tabIndex={-1}
          >
            {showPassword ? (
              <EyeOff className="h-4 w-4" aria-hidden="true" />
            ) : (
              <Eye className="h-4 w-4" aria-hidden="true" />
            )}
          </button>
        </div>
        {error && <span className="text-xs text-accent mt-1">{error}</span>}
      </div>
    );
  }
);
PasswordInput.displayName = 'PasswordInput';
