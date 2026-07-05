import { MessageCircle } from 'lucide-react';

export const LoadingScreen = () => {
  return (
    <div className="flex h-screen w-screen items-center justify-center bg-bg">
      <div className="flex flex-col items-center gap-4">
        <div className="flex items-center gap-2 text-accent">
          <MessageCircle className="h-8 w-8 animate-pulse" />
          <span className="text-2xl font-bold">Aurora Stream</span>
        </div>
        <div className="loading-spinner" />
      </div>
    </div>
  );
};
