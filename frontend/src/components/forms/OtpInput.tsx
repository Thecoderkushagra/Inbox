import React, { useRef } from 'react';
import { cn } from '../../utils/cn';

interface OtpInputProps {
  length?: number;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  error?: string;
}

export const OtpInput = ({ length = 6, value, onChange, disabled, error }: OtpInputProps) => {
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>, index: number) => {
    const val = e.target.value;
    if (/[^0-9]/.test(val)) return;

    const newValue = value.split('');
    newValue[index] = val;
    const finalValue = newValue.join('');
    onChange(finalValue);

    // Auto focus next
    if (val !== '' && index < length - 1) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>, index: number) => {
    if (e.key === 'Backspace') {
      if (!value[index] && index > 0) {
        inputRefs.current[index - 1]?.focus();
      }
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text/plain').slice(0, length).replace(/[^0-9]/g, '');
    if (pastedData) {
      onChange(pastedData);
      if (pastedData.length === length) {
        inputRefs.current[length - 1]?.focus();
      } else {
        inputRefs.current[pastedData.length]?.focus();
      }
    }
  };

  return (
    <div className="space-y-2">
      <div className="flex justify-between gap-2">
        {Array.from({ length }).map((_, index) => (
          <input
            key={index}
            ref={(el) => { inputRefs.current[index] = el; }}
            type="text"
            inputMode="numeric"
            maxLength={1}
            value={value[index] || ''}
            onChange={(e) => handleChange(e, index)}
            onKeyDown={(e) => handleKeyDown(e, index)}
            onPaste={handlePaste}
            disabled={disabled}
            className={cn(
              "w-10 h-12 text-center text-lg font-semibold rounded-2xl border border-transparent bg-code-bg transition-colors focus:outline-none focus:border-accent-border",
              error && "border-accent",
              disabled && "opacity-50 cursor-not-allowed"
            )}
            autoComplete="one-time-code"
          />
        ))}
      </div>
      {error && <p className="text-sm text-accent mt-1">{error}</p>}
    </div>
  );
};
