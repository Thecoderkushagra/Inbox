import { Users } from 'lucide-react';

export const FriendsPage = () => {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="rounded-full bg-accent/10 p-4 mb-4">
        <Users className="h-8 w-8 text-accent" />
      </div>
      <h2 className="text-2xl font-semibold text-text-h mb-2">Friends</h2>
      <p className="text-text max-w-sm">
        Manage your friends list here. This feature is coming in a future milestone.
      </p>
    </div>
  );
};
