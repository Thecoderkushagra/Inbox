import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Mail } from 'lucide-react';
import { AuthHeader } from '../../components/forms/AuthHeader';
import { AuthFooter } from '../../components/forms/AuthFooter';
import { Input } from '../../components/common/Input';
import { PasswordInput } from '../../components/forms/PasswordInput';
import { LoadingButton } from '../../components/forms/LoadingButton';
import { AuthService } from '../../services/AuthService';
import { useAuth } from '../../hooks/useAuth';
import { Routes } from '../../constants';
import { getErrorMessage } from '../../utils/errorUtils';

const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export const LoginPage = () => {
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormValues) => {
    setIsLoading(true);
    try {
      const response = await AuthService.login(data);
      if (response.success) {
        login(response.data.accessToken, response.data.user);
        toast.success('Successfully logged in!');
        navigate(Routes.HOME);
      }
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <AuthHeader 
        title="Welcome Back" 
        subtitle="Sign in to your account to continue"
        icon={<Mail className="w-8 h-8" />}
      />
      
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Email Address"
          type="email"
          placeholder="you@example.com"
          error={errors.email?.message}
          {...register('email')}
          disabled={isLoading}
          autoComplete="email"
          autoFocus
        />
        
        <div className="space-y-1">
          <div className="flex items-center justify-between">
            <label className="text-sm font-medium text-text-h">Password</label>
            <Link 
              to={Routes.FORGOT_PASSWORD || '/forgot-password'} 
              className="text-sm font-medium text-accent hover:text-accent-hover hover:underline"
              tabIndex={-1}
            >
              Forgot password?
            </Link>
          </div>
          <PasswordInput
            placeholder="••••••••"
            error={errors.password?.message}
            {...register('password')}
            disabled={isLoading}
            autoComplete="current-password"
          />
        </div>
        
        <LoadingButton 
          type="submit" 
          variant="primary" 
          loading={isLoading}
          className="w-full mt-6"
        >
          Sign In
        </LoadingButton>
      </form>
      
      <AuthFooter 
        text="Don't have an account?"
        linkText="Sign up"
        to={Routes.REGISTER}
      />
    </>
  );
};
