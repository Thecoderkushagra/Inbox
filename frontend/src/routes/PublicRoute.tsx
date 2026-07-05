import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Routes as AppRoutes } from '../constants';

export const PublicRoute = () => {
  const { user, loading } = useAuth();

  if (loading) {
    // Optionally return a full page loader here
    return null;
  }

  if (user) {
    return <Navigate to={AppRoutes.HOME} replace />;
  }

  return <Outlet />;
};
