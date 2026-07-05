export const Spinner = ({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) => {
  const sizeMap = { sm: '16px', md: '24px', lg: '32px' };
  return (
    <div
      style={{
        width: sizeMap[size],
        height: sizeMap[size],
        border: '2px solid var(--color-border)',
        borderTopColor: 'var(--color-primary)',
        borderRadius: '50%',
        animation: 'spin 1s linear infinite'
      }}
    />
  );
};
