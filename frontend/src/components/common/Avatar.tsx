export const Avatar = ({ src, alt, fallback }: { src?: string; alt?: string; fallback?: string }) => {
  return (
    <div className="avatar">
      {src ? (
        <img src={src} alt={alt || 'Avatar'} />
      ) : (
        <div className="avatar-fallback">{fallback || '?'}</div>
      )}
    </div>
  );
};
