import { Link } from 'react-router-dom';
import { Home, AlertCircle } from 'lucide-react';
import { Routes } from '../../constants';

export const NotFoundPage = () => {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4 bg-bg text-center">
      <div className="text-accent mb-6">
        <AlertCircle className="w-24 h-24 mx-auto opacity-20" />
      </div>
      <h1 className="text-6xl font-bold text-text-h mb-4">404</h1>
      <h2 className="text-2xl font-semibold text-text mb-6">Page Not Found</h2>
      <p className="text-text mb-8 max-w-md">
        The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.
      </p>
      <Link to={Routes.HOME} className="btn btn-primary">
        <Home className="w-4 h-4" />
        Back to Home
      </Link>
    </div>
  );
};
