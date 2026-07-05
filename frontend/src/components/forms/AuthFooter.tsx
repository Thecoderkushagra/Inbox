import { Link } from 'react-router-dom';

interface AuthFooterProps {
  text: string;
  linkText: string;
  to: string;
}

export const AuthFooter = ({ text, linkText, to }: AuthFooterProps) => {
  return (
    <div className="text-center mt-6 text-sm text-text">
      {text}{' '}
      <Link to={to} className="font-semibold text-accent hover:text-accent-hover hover:underline">
        {linkText}
      </Link>
    </div>
  );
};
