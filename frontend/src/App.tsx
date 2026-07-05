import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { ErrorBoundary } from './components/common/ErrorBoundary';
import { AuthLayout } from './layouts/AuthLayout';
import { AppLayout } from './layouts/AppLayout';
import { ProtectedRoute } from './routes/ProtectedRoute';
import { PublicRoute } from './routes/PublicRoute';
import { Routes as AppRoutesConstants } from './constants';
import { AuthProvider } from './contexts/AuthContext';
import { ThemeProvider } from './contexts/ThemeContext';

// Auth Pages
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { VerifyOtpPage } from './pages/auth/VerifyOtpPage';
import { ForgotPasswordPage } from './pages/auth/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/auth/ResetPasswordPage';
import { NotFoundPage } from './pages/auth/NotFoundPage';

// App Pages
import { ChatsPage } from './pages/chat/ChatsPage';
import { FriendsPage } from './pages/friends/FriendsPage';
import { GroupsPage } from './pages/groups/GroupsPage';
import { SearchPage } from './pages/search/SearchPage';
import { NotificationsPage } from './pages/notifications/NotificationsPage';
import { ProfilePage } from './pages/profile/ProfilePage';
import { SettingsPage } from './pages/settings/SettingsPage';

export default function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              {/* Public Auth Routes */}
              <Route element={<PublicRoute />}>
                <Route element={<AuthLayout />}>
                  <Route path={AppRoutesConstants.LOGIN} element={<LoginPage />} />
                  <Route path={AppRoutesConstants.REGISTER} element={<RegisterPage />} />
                  <Route path={AppRoutesConstants.VERIFY_OTP} element={<VerifyOtpPage />} />
                  <Route path={AppRoutesConstants.FORGOT_PASSWORD} element={<ForgotPasswordPage />} />
                  <Route path={AppRoutesConstants.RESET_PASSWORD} element={<ResetPasswordPage />} />
                </Route>
              </Route>

              {/* Protected App Routes */}
              <Route element={<ProtectedRoute />}>
                <Route element={<AppLayout />}>
                  <Route path={AppRoutesConstants.CHAT} element={<ChatsPage />} />
                  <Route path={AppRoutesConstants.FRIENDS} element={<FriendsPage />} />
                  <Route path={AppRoutesConstants.GROUPS} element={<GroupsPage />} />
                  <Route path={AppRoutesConstants.SEARCH} element={<SearchPage />} />
                  <Route path={AppRoutesConstants.NOTIFICATIONS} element={<NotificationsPage />} />
                  <Route path={AppRoutesConstants.PROFILE} element={<ProfilePage />} />
                  <Route path={AppRoutesConstants.SETTINGS} element={<SettingsPage />} />
                </Route>
              </Route>

              {/* Default Route */}
              <Route path="/" element={<Navigate to={AppRoutesConstants.CHAT} replace />} />

              {/* 404 Route */}
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </BrowserRouter>

          {/* Global Notifications */}
          <Toaster
            position="top-right"
            toastOptions={{
              style: {
                background: 'var(--surface)',
                color: 'var(--text-accent)',
                border: '1px solid var(--border)',
              },
              success: {
                iconTheme: {
                  primary: 'var(--success)',
                  secondary: 'white',
                },
              },
              error: {
                iconTheme: {
                  primary: 'var(--danger)',
                  secondary: 'white',
                },
              },
            }}
          />
        </AuthProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}
