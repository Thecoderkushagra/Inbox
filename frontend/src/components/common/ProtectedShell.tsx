import type { ReactNode } from 'react';

interface ProtectedShellProps {
  children: ReactNode;
}

export const ProtectedShell = ({ children }: ProtectedShellProps) => {
  return (
    <div className="protected-shell">
      {/* Shell layout such as sidebars, headers, etc. will be injected here */}
      <main className="protected-content">
        {children}
      </main>
    </div>
  );
};
