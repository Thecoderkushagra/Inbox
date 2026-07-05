import { Outlet } from 'react-router-dom';

export const AuthLayout = () => {
  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-bg">
      <div className="w-full max-w-md">
        <div className="bg-surface border border-border shadow-md rounded-2xl p-6 sm:p-8">
          <Outlet />
        </div>
      </div>
    </div>
  );
};
