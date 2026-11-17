export function formatTime(value) {
  if (!value) return '';
  // The backend serializes LocalDateTime without a zone suffix; treat it as local time.
  const date = new Date(value.length === 16 || value.length === 19 ? `${value}Z` : value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export function formatListTime(value) {
  if (!value) return '';
  const date = new Date(`${value}Z`);
  if (Number.isNaN(date.getTime())) return '';
  const today = new Date();
  const sameDay = date.toDateString() === today.toDateString();
  return sameDay
    ? date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : date.toLocaleDateString([], { month: 'short', day: 'numeric' });
}

export function initials(username = '') {
  return username.slice(0, 2).toUpperCase();
}
