import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { UserPlus } from 'lucide-react';
import { AuthHeader } from '../../components/forms/AuthHeader';
import { AuthFooter } from '../../components/forms/AuthFooter';
import { Input } from '../../components/common/Input';
import { PasswordInput } from '../../components/forms/PasswordInput';
import { LoadingButton } from '../../components/forms/LoadingButton';
import { AuthService } from '../../services/AuthService';
import { Routes, Regex } from '../../constants';
import { getErrorMessage } from '../../utils/errorUtils';

const registerSchema = z.object({
  username: z.string().min(3, 'Username must be at least 3 characters').max(30, 'Username must be less than 30 characters'),
  email: z.string().email('Please enter a valid email address'),
  password: z.string().regex(Regex.PASSWORD, 'Password must be at least 8 characters with 1 letter and 1 number'),
  confirmPassword: z.string()
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export const RegisterPage = () => {
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormValues) => {
    setIsLoading(true);
    try {
      const response = await AuthService.register({
        email: data.email,
        username: data.username,
        password: data.password
      });
      if (response.success) {
        toast.success('Registration successful! Please verify your email.');
        // navigate(Routes.VERIFY_OTP, { state: { email: data.email } });
        navigate('/verify-otp', { state: { email: data.email } });
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
        title="Create an Account" 
        subtitle="Join Aurora Stream today"
        icon={<UserPlus className="w-8 h-8" />}
      />
      
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <Input
          label="Username"
          type="text"
          placeholder="johndoe"
          error={errors.username?.message}
          {...register('username')}
          disabled={isLoading}
          autoComplete="username"
          autoFocus
        />

        <Input
          label="Email Address"
          type="email"
          placeholder="you@example.com"
          error={errors.email?.message}
          {...register('email')}
          disabled={isLoading}
          autoComplete="email"
        />
        
        <PasswordInput
          label="Password"
          placeholder="••••••••"
          error={errors.password?.message}
          {...register('password')}
          disabled={isLoading}
          autoComplete="new-password"
        />

        <PasswordInput
          label="Confirm Password"
          placeholder="••••••••"
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
          disabled={isLoading}
          autoComplete="new-password"
        />
        
        <LoadingButton 
          type="submit" 
          variant="primary" 
          loading={isLoading}
          className="w-full mt-6"
        >
          Sign Up
        </LoadingButton>
      </form>
      
      <AuthFooter 
        text="Already have an account?"
        linkText="Sign in"
        to={Routes.LOGIN}
      />
    </>
  );
};
