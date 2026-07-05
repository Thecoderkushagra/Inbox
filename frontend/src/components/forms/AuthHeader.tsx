import type { ReactNode } from 'react';

interface AuthHeaderProps {
  title: string;
  subtitle: string;
  icon?: ReactNode;
}

export const AuthHeader = ({ title, subtitle, icon }: AuthHeaderProps) => {
  return (
    <div className="text-center mb-8">
      {icon && <div className="flex justify-center mb-4 text-accent">{icon}</div>}
      <h1 className="text-2xl font-bold tracking-tight text-text-h mb-2">
        {title}
      </h1>
      <p className="text-sm text-text">
        {subtitle}
      </p>
    </div>
  );
};
