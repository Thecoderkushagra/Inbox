/**
 * Utility functions for CSS class name manipulation.
 */
export const classNames = (...classes: (string | undefined | null | false)[]) => {
  return classes.filter(Boolean).join(' ');
};
