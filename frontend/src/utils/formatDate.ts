import { format, isToday, isYesterday, formatDistanceToNow } from 'date-fns';

export function formatMessageTime(dateString?: string | null): string {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '';
  return format(date, 'p'); // e.g. 4:30 PM
}

export function formatConversationTime(dateString?: string | null): string {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '';

  if (isToday(date)) {
    return format(date, 'p');
  }
  if (isYesterday(date)) {
    return 'Yesterday';
  }
  return format(date, 'MMM d');
}

export function formatRelativeTime(dateString?: string | null): string {
  if (!dateString) return 'offline';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return 'offline';
  return formatDistanceToNow(date, { addSuffix: true });
}
