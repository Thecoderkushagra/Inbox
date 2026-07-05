/**
 * Utility functions for date formatting and manipulation.
 */
export const formatDate = (date: string | number | Date) => {
  if (!date) return '';
  return new Intl.DateTimeFormat('en-US').format(new Date(date));
};
