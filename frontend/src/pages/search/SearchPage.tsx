import { Search as SearchIcon } from 'lucide-react';

export const SearchPage = () => {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="rounded-full bg-accent/10 p-4 mb-4">
        <SearchIcon className="h-8 w-8 text-accent" />
      </div>
      <h2 className="text-2xl font-semibold text-text-h mb-2">Search</h2>
      <p className="text-text max-w-sm">
        Search for users, messages, and conversations. This feature is coming in a future milestone.
      </p>
    </div>
  );
};
