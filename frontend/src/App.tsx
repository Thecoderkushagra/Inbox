import React, { useEffect, Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { useAuthStore } from '@/stores/authStore';
import { AppShell } from '@/components/layout/AppShell';
import { Skeleton } from '@/components/ui/Skeleton';

// Lazy-loaded route pages
const AuthPage = lazy(() => import('@/pages/AuthPage').then((m) => ({ default: m.AuthPage })));
const InboxPage = lazy(() => import('@/pages/InboxPage').then((m) => ({ default: m.InboxPage })));
const ComposePage = lazy(() => import('@/pages/ComposePage').then((m) => ({ default: m.ComposePage })));
const SearchPage = lazy(() => import('@/pages/SearchPage').then((m) => ({ default: m.SearchPage })));
const PeoplePage = lazy(() => import('@/pages/PeoplePage').then((m) => ({ default: m.PeoplePage })));
const ArchivedPage = lazy(() => import('@/pages/ArchivedPage').then((m) => ({ default: m.ArchivedPage })));
const SettingsPage = lazy(() => import('@/pages/SettingsPage').then((m) => ({ default: m.SettingsPage })));

const PageLoader: React.FC = () => (
  <div className="h-full w-full flex items-center justify-center p-8">
    <div className="space-y-3 w-full max-w-md">
      <Skeleton className="h-8 w-1/3 rounded-2xl" />
      <Skeleton className="h-24 w-full rounded-2xl" />
      <Skeleton className="h-48 w-full rounded-2xl" />
    </div>
  </div>
);

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuthStore();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

export const App: React.FC = () => {
  const { initialize, isAuthenticated } = useAuthStore();

  useEffect(() => {
    initialize();
  }, [initialize]);

  return (
    <>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: '#0B0F19',
            color: '#F8FAFC',
            border: '1px solid #1E293B',
            borderRadius: '16px',
            fontSize: '13px',
            fontWeight: 600,
            padding: '12px 16px',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.7)',
          },
          success: {
            iconTheme: {
              primary: '#10B981',
              secondary: '#0B0F19',
            },
          },
          error: {
            iconTheme: {
              primary: '#F43F5E',
              secondary: '#0B0F19',
            },
          },
        }}
      />

      <Suspense fallback={<PageLoader />}>
        <Routes>
          {/* Public Auth Routes */}
          <Route
            path="/login"
            element={isAuthenticated ? <Navigate to="/inbox" replace /> : <AuthPage />}
          />
          <Route
            path="/register"
            element={isAuthenticated ? <Navigate to="/inbox" replace /> : <AuthPage />}
          />

          {/* Protected Multi-Page Routes inside AppShell */}
          <Route
            element={
              <ProtectedRoute>
                <AppShell />
              </ProtectedRoute>
            }
          >
            <Route path="/inbox" element={<InboxPage />} />
            <Route path="/inbox/:conversationId" element={<InboxPage />} />
            <Route path="/compose" element={<ComposePage />} />
            <Route path="/search" element={<SearchPage />} />
            <Route path="/people" element={<PeoplePage />} />
            <Route path="/archived" element={<ArchivedPage />} />
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/" element={<Navigate to="/inbox" replace />} />
          </Route>

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/inbox" replace />} />
        </Routes>
      </Suspense>
    </>
  );
};
