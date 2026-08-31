import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { useAuthStore } from '@/stores/authStore';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Lock, Mail, User, ArrowRight, ShieldCheck, Zap } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const isStrongPassword = (pass: string): boolean => {
  if (pass.length < 8) return false;
  if (!/[A-Z]/.test(pass)) return false;
  if (!/[a-z]/.test(pass)) return false;
  if (!/\d/.test(pass)) return false;
  if (!/[!@#$%^&*(),.?":{}|<>_\-+=\[\]\/\\`~;]/.test(pass)) return false;
  return true;
};

export const AuthPage: React.FC = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [identifier, setIdentifier] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const { login, register, isLoading, error, clearError } = useAuthStore();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();

    try {
      if (isLogin) {
        if (!identifier.trim() || !password) {
          toast.error('Please fill in all fields.');
          return;
        }
        await login(identifier.trim(), password);
      } else {
        if (!username.trim() || !email.trim() || !password || !confirmPassword) {
          toast.error('Please fill in all fields.');
          return;
        }
        if (!isStrongPassword(password)) {
          toast.error('Weak password: Must be at least 8 characters with 1 uppercase, 1 lowercase, 1 number, and 1 special symbol.');
          return;
        }
        if (password !== confirmPassword) {
          toast.error('Passwords do not match.');
          return;
        }
        await register(username.trim(), email.trim(), password, confirmPassword);
      }
      navigate('/');
    } catch {
      // Handled via toast in store
    }
  };

  const toggleMode = () => {
    setIsLogin(!isLogin);
    clearError();
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-slate-950 p-4 sm:p-6 relative overflow-hidden selection:bg-indigo-500 selection:text-white">
      {/* Background modern ambient lights */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-indigo-600/15 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-violet-600/15 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md relative z-10">
        {/* Header Branding */}
        <div className="text-center mb-8">
          <div className="inline-flex p-3 bg-slate-900/90 border border-slate-800 rounded-3xl shadow-2xl mb-4">
            <img src="/frontend/src/assets/inbox-logo.png" alt="Inbox" className="w-12 h-12 object-contain" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            {isLogin ? 'Welcome back to Inbox' : 'Create your account'}
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 mt-1">
            {isLogin
              ? 'Real-time, private, end-to-end encrypted messaging.'
              : 'Instant account activation. No email verification required.'}
          </p>
        </div>

        {/* Form Card */}
        <div className="bg-slate-900/80 backdrop-blur-xl border border-slate-800/90 rounded-3xl p-6 sm:p-8 shadow-2xl space-y-6">
          {error && (
            <div className="p-3.5 bg-rose-500/10 border border-rose-500/30 rounded-2xl text-xs text-rose-400 font-medium">
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
                <Input
                  label="Password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  leftIcon={<Lock className="w-4 h-4 text-slate-500" />}
                />
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
                <Input
                  label="Password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  leftIcon={<Lock className="w-4 h-4 text-slate-500" />}
                />
                <p className="text-[11px] text-slate-400 -mt-2">
                  Must be 8+ chars with uppercase, lowercase, digit &amp; special character (e.g. <span className="text-indigo-300 font-mono">Secret@123</span>).
                </p>
                <Input
                  label="Confirm Password"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  leftIcon={<Lock className="w-4 h-4 text-slate-500" />}
                />
              </>
            )}

            <div className="pt-2">
              <Button type="submit" className="w-full py-3 text-sm font-semibold rounded-2xl" isLoading={isLoading}>
                <span>{isLogin ? 'Sign In' : 'Get Started'}</span>
                <ArrowRight className="w-4 h-4 ml-1.5" />
              </Button>
            </div>
          </form>

          {/* Toggle between Login and Register */}
          <div className="text-center pt-2 border-t border-slate-800">
            <p className="text-xs text-slate-400">
              {isLogin ? "Don't have an account yet?" : 'Already have an account?'}
              <button
                type="button"
                onClick={toggleMode}
                className="ml-1.5 text-indigo-400 hover:text-indigo-300 font-semibold hover:underline cursor-pointer"
              >
                {isLogin ? 'Create one now' : 'Sign in'}
              </button>
            </p>
          </div>
        </div>

        {/* Footer Features */}
        <div className="mt-8 flex items-center justify-center gap-6 text-[11px] text-slate-500 font-medium">
          <div className="flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            <span>Encrypted Monolith</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Zap className="w-4 h-4 text-amber-400" />
            <span>Virtual Threads</span>
          </div>
        </div>
      </div>
    </div>
  );
};
