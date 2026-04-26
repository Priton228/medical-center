interface Props {
  fullName?: string | null;
  avatarUrl?: string | null;
  size?: number;
  className?: string;
}

/**
 * Кружок с аватаром пользователя или его инициалами (если URL пуст).
 * Используется в шапке/сайдбаре/таблицах.
 */
export default function UserAvatar({ fullName, avatarUrl, size = 32, className = '' }: Props) {
  const initials = (fullName || '').split(/\s+/).filter(Boolean).slice(0, 2)
    .map((s) => s.charAt(0).toUpperCase()).join('') || '?';
  const apiBase = (import.meta.env.VITE_API_BASE_URL as string | undefined) || '';
  const fullUrl = avatarUrl
    ? (avatarUrl.startsWith('http') ? avatarUrl : apiBase.replace(/\/$/, '') + avatarUrl)
    : null;
  return (
    <div
      className={`rounded-full overflow-hidden bg-gradient-to-br from-brand-200 to-brand-500 text-white flex items-center justify-center font-semibold flex-shrink-0 ${className}`}
      style={{ width: size, height: size, fontSize: size / 2.6 }}
    >
      {fullUrl
        ? <img src={fullUrl} alt={fullName || 'avatar'} className="w-full h-full object-cover" />
        : <span>{initials}</span>}
    </div>
  );
}
