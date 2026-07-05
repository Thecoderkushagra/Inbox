import { createContext, useState } from 'react';
import type { ReactNode, Dispatch, SetStateAction } from 'react';

interface SocketContextType {
  socket: unknown;
  setSocket: Dispatch<SetStateAction<unknown>>;
}

const SocketContext = createContext<SocketContextType | undefined>(undefined);

export function SocketProvider({ children }: { children: ReactNode }) {
  const [socket, setSocket] = useState<unknown>(null);

  // Implementation will be added later
  return (
    <SocketContext.Provider value={{ socket, setSocket }}>
      {children}
    </SocketContext.Provider>
  );
}

export { SocketContext };
