import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Routes as AppRoutes } from '../constants';
import { LoadingScreen } from '../components/common/LoadingScreen';

export const ProtectedRoute = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return <LoadingScreen />;
  }

  if (!user) {
    return <Navigate to={AppRoutes.LOGIN} replace />;
  }

  return <Outlet />;
};
