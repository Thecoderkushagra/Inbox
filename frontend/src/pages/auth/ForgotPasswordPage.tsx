import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { KeyRound } from 'lucide-react';
import { AuthHeader } from '../../components/forms/AuthHeader';
import { AuthFooter } from '../../components/forms/AuthFooter';
import { Input } from '../../components/common/Input';
import { LoadingButton } from '../../components/forms/LoadingButton';
import { AuthService } from '../../services/AuthService';
import { Routes } from '../../constants';
import { getErrorMessage } from '../../utils/errorUtils';

const forgotPasswordSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
});

type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;

export const ForgotPasswordPage = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const navigate = useNavigate();

  const { register, handleSubmit, formState: { errors } } = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
  });

  const onSubmit = async (data: ForgotPasswordFormValues) => {
    setIsLoading(true);
    try {
      const response = await AuthService.forgotPassword(data.email);
      if (response.success) {
        toast.success('Password reset instructions sent!');
        setIsSubmitted(true);
      }
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  };

  if (isSubmitted) {
    return (
      <div className="text-center space-y-6">
        <AuthHeader 
          title="Check your email" 
          subtitle="We have sent password reset instructions to your email."
          icon={<KeyRound className="w-8 h-8" />}
        />
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
        title="Reset Password" 
        subtitle="Enter your email to receive reset instructions"
        icon={<KeyRound className="w-8 h-8" />}
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
        
        <LoadingButton 
          type="submit" 
          variant="primary" 
          loading={isLoading}
          className="w-full mt-6"
        >
          Send Instructions
        </LoadingButton>
      </form>
      
      <AuthFooter 
        text="Remember your password?"
        linkText="Sign in"
        to={Routes.LOGIN}
      />
    </>
  );
};
