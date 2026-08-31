import Cookies from 'js-cookie';

const ACCESS_TOKEN_KEY = 'inbox_access_token';
const REFRESH_TOKEN_KEY = 'inbox_refresh_token';

export const setAccessTokenCookie = (token: string, expiresDays = 1) => {
  Cookies.set(ACCESS_TOKEN_KEY, token, {
    expires: expiresDays,
    sameSite: 'lax',
    secure: window.location.protocol === 'https:',
    path: '/',
  });
};

export const getAccessTokenCookie = (): string | undefined => {
  return Cookies.get(ACCESS_TOKEN_KEY);
};

export const removeAccessTokenCookie = () => {
  Cookies.remove(ACCESS_TOKEN_KEY, { path: '/' });
};

export const setRefreshTokenCookie = (token: string, expiresDays = 7) => {
  Cookies.set(REFRESH_TOKEN_KEY, token, {
    expires: expiresDays,
    sameSite: 'lax',
    secure: window.location.protocol === 'https:',
    path: '/',
  });
};

export const getRefreshTokenCookie = (): string | undefined => {
  return Cookies.get(REFRESH_TOKEN_KEY);
};

export const removeRefreshTokenCookie = () => {
  Cookies.remove(REFRESH_TOKEN_KEY, { path: '/' });
};

export const clearAuthCookies = () => {
  removeAccessTokenCookie();
  removeRefreshTokenCookie();
};
