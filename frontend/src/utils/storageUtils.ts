/**
 * Utility functions for local storage operations.
 */
export const getItem = (key: string): string | null => {
  try {
    return localStorage.getItem(key);
  } catch (_e) {
    return null;
  }
};

export const setItem = (key: string, value: string): void => {
  try {
    localStorage.setItem(key, value);
  } catch (_e) {
    // Ignore error
  }
};
