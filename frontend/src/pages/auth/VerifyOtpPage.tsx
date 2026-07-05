import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ShieldCheck } from 'lucide-react';
import { AuthHeader } from '../../components/forms/AuthHeader';
import { AuthFooter } from '../../components/forms/AuthFooter';
import { OtpInput } from '../../components/forms/OtpInput';
import { LoadingButton } from '../../components/forms/LoadingButton';
import { AuthService } from '../../services/AuthService';
import { useAuth } from '../../hooks/useAuth';
import { Routes } from '../../constants';
import { getErrorMessage } from '../../utils/errorUtils';

export const VerifyOtpPage = () => {
  const [otp, setOtp] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();
  
  const email = location.state?.email as string | undefined;

  if (!email) {
    // Redirect back to register if accessed directly without email state
    navigate(Routes.REGISTER, { replace: true });
    return null;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (otp.length !== 6) {
      toast.error('Please enter a complete 6-digit code');
      return;
    }

    setIsLoading(true);
    try {
      const response = await AuthService.verifyOtp(email, otp);
      if (response.success) {
        login(response.data.accessToken, response.data.user);
        toast.success('Email verified successfully!');
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
        title="Verify your email" 
        subtitle={`We sent a 6-digit code to ${email}`}
        icon={<ShieldCheck className="w-8 h-8" />}
      />
      
      <form onSubmit={handleSubmit} className="space-y-6">
        <OtpInput
          length={6}
          value={otp}
          onChange={setOtp}
          disabled={isLoading}
        />
        
        <LoadingButton 
          type="submit" 
          variant="primary" 
          loading={isLoading}
          className="w-full"
        >
          Verify Code
        </LoadingButton>
      </form>
      
      <AuthFooter 
        text="Didn't receive the code?"
        linkText="Resend"
        to="#" // For now, no actual resend link action specified
      />
    </>
  );
};
