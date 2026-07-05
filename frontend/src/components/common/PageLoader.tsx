import { Spinner } from './Spinner';

export const PageLoader = () => {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', width: '100vw' }}>
      <Spinner size="lg" />
    </div>
  );
};
