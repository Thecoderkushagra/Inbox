import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { useAuthStore } from '@/stores/authStore';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import {
  Lock,
  Mail,
  User,
  ArrowRight,
  ShieldCheck,
  Eye,
  EyeOff,
  Check,
  CheckCircle2,
} from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import inboxLogo from '@/assets/inbox-logo.png';
import { AuthBanner } from '@/components/auth/AuthBanner';

const passwordCriteria = [
  { label: '8+ characters', test: (p: string) => p.length >= 8 },
  { label: 'Uppercase letter', test: (p: string) => /[A-Z]/.test(p) },
  { label: 'Lowercase letter', test: (p: string) => /[a-z]/.test(p) },
  { label: 'Number', test: (p: string) => /\d/.test(p) },
  { label: 'Special symbol', test: (p: string) => /[!@#$%^&*(),.?":{}|<>_\-+=\[\]\/\\`~;]/.test(p) },
];

const isStrongPassword = (pass: string): boolean => {
  return passwordCriteria.every((c) => c.test(pass));
};

export const AuthPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [isLogin, setIsLogin] = useState(location.pathname !== '/register');
  const [identifier, setIdentifier] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const { login, register, isLoading, error, clearError } = useAuthStore();

  useEffect(() => {
    if (location.pathname === '/register') {
      setIsLogin(false);
    } else if (location.pathname === '/login') {
      setIsLogin(true);
    }
  }, [location.pathname]);

  const switchMode = (loginMode: boolean) => {
    setIsLogin(loginMode);
    setShowPassword(false);
    setShowConfirmPassword(false);
    clearError();
    navigate(loginMode ? '/login' : '/register', { replace: true });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();

    try {
      if (isLogin) {
        if (!identifier.trim() || !password) {
          toast.error('Please enter your identifier and password.');
          return;
        }
        await login(identifier.trim(), password);
      } else {
        if (!username.trim() || !email.trim() || !password || !confirmPassword) {
          toast.error('Please fill in all fields.');
          return;
        }
        if (!isStrongPassword(password)) {
          toast.error(
            'Password must be at least 8 characters and include uppercase, lowercase, a digit, and a special symbol.'
          );
          return;
        }
        if (password !== confirmPassword) {
          toast.error('Passwords do not match.');
          return;
        }
        await register(username.trim(), email.trim(), password, confirmPassword);
      }
      navigate('/inbox');
    } catch {
      // Handled via toast in store
    }
  };

  return (
    <div className="min-h-screen w-full flex bg-[#080c14] text-slate-100 selection:bg-indigo-500 selection:text-white">
      {/* Left Column: 50% Atmospheric Brand Banner (Hidden on Mobile) */}
      <div className="hidden lg:flex lg:w-1/2 min-h-screen border-r border-slate-800/60 bg-gradient-to-br from-indigo-950/70 via-slate-950 to-[#030611] relative overflow-hidden flex-col justify-between">
        <AuthBanner />
      </div>

      {/* Right Column: Main Login / Signup Form (Focal Point on Desktop, Full Width on Mobile) */}
      <div className="w-full lg:w-1/2 min-h-screen flex flex-col justify-center items-center p-6 sm:p-10 lg:p-12 xl:p-16 relative overflow-y-auto">
        <div className="w-full max-w-md my-auto space-y-6 relative z-10 py-4">
          {/* Mobile-Only Header */}
          <div className="lg:hidden flex items-center justify-center gap-2.5 mb-2">
            <div className="p-2 bg-slate-900 border border-slate-800 rounded-xl shadow-md">
              <img src={inboxLogo} alt="Inbox" className="w-7 h-7 object-contain" />
            </div>
            <span className="text-xl font-bold text-white tracking-tight">Inbox</span>
          </div>

          {/* Form Header */}
          <div className="text-left">
            <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              {isLogin ? 'Sign in to Inbox' : 'Create an account'}
            </h1>
            <p className="text-sm text-slate-400 mt-1.5">
              {isLogin
                ? 'Welcome back. Enter your credentials to access your conversations.'
                : 'Get started in seconds. Instant activation with zero email delays.'}
            </p>
          </div>

          {/* Segmented Mode Switcher */}
          <div className="flex p-1 bg-slate-900/90 border border-slate-800/80 rounded-xl">
            <button
              type="button"
              onClick={() => switchMode(true)}
              className={`flex-1 py-2 text-xs sm:text-sm font-medium rounded-lg transition-all cursor-pointer ${
                isLogin
                  ? 'bg-slate-800 text-white shadow-sm font-semibold'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Sign in
            </button>
            <button
              type="button"
              onClick={() => switchMode(false)}
              className={`flex-1 py-2 text-xs sm:text-sm font-medium rounded-lg transition-all cursor-pointer ${
                !isLogin
                  ? 'bg-slate-800 text-white shadow-sm font-semibold'
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              Create account
            </button>
          </div>

          {/* Form Box */}
          <div className="bg-slate-900/50 border border-slate-800/80 rounded-2xl p-6 sm:p-7 shadow-xl space-y-4">
            {error && (
              <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-xs text-rose-400 font-medium">
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              {isLogin ? (
                <>
                  <Input
                    label="Username or Email"
                    value={identifier}
                    onChange={(e) => setIdentifier(e.target.value)}
                    placeholder="Enter your username or email"
                    required
                    leftIcon={<Mail className="w-4 h-4 text-slate-500" />}
                  />
                  <div>
                    <Input
                      label="Password"
                      type={showPassword ? 'text' : 'password'}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                      leftIcon={<Lock className="w-4 h-4 text-slate-500" />}
                      rightIcon={
                        <button
                          type="button"
                          onClick={() => setShowPassword(!showPassword)}
                          className="text-slate-400 hover:text-slate-200 transition-colors focus:outline-none cursor-pointer flex items-center justify-center"
                          aria-label={showPassword ? 'Hide password' : 'Show password'}
                        >
                          {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                        </button>
                      }
                    />
                  </div>
                </>
              ) : (
                <>
                  <Input
                    label="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="e.g. alexander"
                    required
                    leftIcon={<User className="w-4 h-4 text-slate-500" />}
                  />
                  <Input
                    label="Email Address"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="alex@example.com"
                    required
                    leftIcon={<Mail className="w-4 h-4 text-slate-500" />}
                  />
                  <div>
                    <Input
                      label="Password"
                      type={showPassword ? 'text' : 'password'}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                      leftIcon={<Lock className="w-4 h-4 text-slate-500" />}
                      rightIcon={
                        <button
                          type="button"
                          onClick={() => setShowPassword(!showPassword)}
                          className="text-slate-400 hover:text-slate-200 transition-colors focus:outline-none cursor-pointer flex items-center justify-center"
                          aria-label={showPassword ? 'Hide password' : 'Show password'}
                        >
                          {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                        </button>
                      }
                    />

                    {/* Password criteria checklist */}
                    {password && (
                      <div className="mt-2.5 p-2.5 bg-slate-950/60 border border-slate-800/80 rounded-xl space-y-1.5">
                        <span className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">
                          Password Requirements
                        </span>
                        <div className="flex flex-wrap gap-1.5">
                          {passwordCriteria.map((c, i) => {
                            const satisfied = c.test(password);
                            return (
                              <span
                                key={i}
                                className={`text-[10px] px-2 py-0.5 rounded-md flex items-center gap-1 font-medium transition-colors ${
                                  satisfied
                                    ? 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/30'
                                    : 'bg-slate-900 text-slate-500 border border-slate-800'
                                }`}
                              >
                                {satisfied ? (
                                  <Check className="w-2.5 h-2.5 text-emerald-400" />
                                ) : (
                                  <span className="w-1.5 h-1.5 rounded-full bg-slate-600" />
                                )}
                                {c.label}
                              </span>
                            );
                          })}
                        </div>
                      </div>
                    )}
                  </div>

                  <div>
                    <Input
                      label="Confirm Password"
                      type={showConfirmPassword ? 'text' : 'password'}
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                      leftIcon={<Lock className="w-4 h-4 text-slate-500" />}
                      rightIcon={
                        <button
                          type="button"
                          onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                          className="text-slate-400 hover:text-slate-200 transition-colors focus:outline-none cursor-pointer flex items-center justify-center"
                          aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                        >
                          {showConfirmPassword ? (
                            <EyeOff className="w-4 h-4" />
                          ) : (
                            <Eye className="w-4 h-4" />
                          )}
                        </button>
                      }
                    />
                    {confirmPassword && (
                      <div className="mt-1.5 flex items-center gap-1.5 text-xs">
                        {password === confirmPassword ? (
                          <span className="text-emerald-400 font-medium flex items-center gap-1">
                            <CheckCircle2 className="w-3.5 h-3.5" /> Passwords match
                          </span>
                        ) : (
                          <span className="text-rose-400 font-medium">Passwords do not match</span>
                        )}
                      </div>
                    )}
                  </div>
                </>
              )}

              <div className="pt-2">
                <Button
                  type="submit"
                  className="w-full py-3 text-sm font-medium rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white shadow-lg shadow-indigo-600/25 transition-all"
                  isLoading={isLoading}
                >
                  <span>{isLogin ? 'Sign in' : 'Create account'}</span>
                  <ArrowRight className="w-4 h-4 ml-1.5" />
                </Button>
              </div>
            </form>

            {/* Switch mode footer */}
            <div className="text-center pt-3 border-t border-slate-800/80">
              <p className="text-xs text-slate-400">
                {isLogin ? "Don't have an account?" : 'Already have an account?'}
                <button
                  type="button"
                  onClick={() => switchMode(!isLogin)}
                  className="ml-1.5 text-indigo-400 hover:text-indigo-300 font-semibold hover:underline cursor-pointer"
                >
                  {isLogin ? 'Create one now' : 'Sign in'}
                </button>
              </p>
            </div>
          </div>

          {/* Privacy Note */}
          <div className="flex items-center justify-center gap-2 text-xs text-slate-500">
            <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
            <span>End-to-end encrypted messaging · Instant activation</span>
          </div>
        </div>
      </div>
    </div>
  );
};
