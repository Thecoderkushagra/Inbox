import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useSearchParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Lock } from 'lucide-react';
import { AuthHeader } from '../../components/forms/AuthHeader';
import { PasswordInput } from '../../components/forms/PasswordInput';
import { LoadingButton } from '../../components/forms/LoadingButton';
import { AuthService } from '../../services/AuthService';
import { Routes, Regex } from '../../constants';
import { getErrorMessage } from '../../utils/errorUtils';

const resetPasswordSchema = z.object({
  password: z.string().regex(Regex.PASSWORD, 'Password must be at least 8 characters with 1 letter and 1 number'),
  confirmPassword: z.string()
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;

export const ResetPasswordPage = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const { register, handleSubmit, formState: { errors } } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
  });

  const onSubmit = async (data: ResetPasswordFormValues) => {
    if (!token) {
      toast.error('Invalid or missing reset token');
      return;
    }

    setIsLoading(true);
    try {
      const response = await AuthService.resetPassword(token, data.password);
      if (response.success) {
        toast.success('Password reset successfully!');
        navigate(Routes.LOGIN);
      }
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  };

  if (!token) {
    return (
      <div className="text-center space-y-4">
        <h2 className="text-2xl font-bold text-accent">Invalid Link</h2>
        <p className="text-text">The password reset link is invalid or missing.</p>
        <button 
          onClick={() => navigate(Routes.LOGIN)}
          className="btn btn-primary w-full"
        >
          Return to Login
        </button>
      </div>
    );
  }

  return (
    <>
      <AuthHeader 
        title="Create New Password" 
        subtitle="Please enter your new password below"
        icon={<Lock className="w-8 h-8" />}
      />
      
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <PasswordInput
          label="New Password"
          placeholder="••••••••"
          error={errors.password?.message}
          {...register('password')}
          disabled={isLoading}
          autoComplete="new-password"
          autoFocus
        />

        <PasswordInput
          label="Confirm New Password"
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
          Reset Password
        </LoadingButton>
      </form>
    </>
  );
};
