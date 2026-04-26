import { useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { Camera, Trash2 } from 'lucide-react';
import { usersApi } from '@/api/endpoints';
import { useAuth } from '@/hooks/useAuth';

interface Props {
  /** Текущий относительный URL аватара (например, /uploads/avatars/...). Если null/undefined — показывается заглушка. */
  avatarUrl?: string | null;
  fullName?: string | null;
  /** Колбэк, вызываемый при изменении URL — чтобы родитель обновил кэш/UI. */
  onChanged?: (url: string | null) => void;
  size?: number;
}

/**
 * Виджет загрузки аватара: квадратное превью + кнопки "Загрузить" и "Удалить".
 * Отправляет multipart на POST /api/v1/users/me/avatar.
 */
export default function AvatarUploader({ avatarUrl, fullName, onChanged, size = 96 }: Props) {
  const { updateProfile } = useAuth();
  const fileRef = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);
  const initials = (fullName || '').split(/\s+/).filter(Boolean).slice(0, 2)
    .map((s) => s.charAt(0).toUpperCase()).join('') || '?';
  const apiBase = (import.meta.env.VITE_API_BASE_URL as string | undefined) || '';
  const fullUrl = avatarUrl
    ? (avatarUrl.startsWith('http') ? avatarUrl : apiBase.replace(/\/$/, '') + avatarUrl)
    : null;

  const handle = async (file: File) => {
    if (file.size > 5 * 1024 * 1024) {
      toast.error('Файл больше 5 МБ');
      return;
    }
    if (!file.type.startsWith('image/')) {
      toast.error('Допустимы только изображения');
      return;
    }
    setBusy(true);
    try {
      const res = await usersApi.uploadAvatar(file);
      updateProfile({ avatarUrl: res.avatarUrl ?? null });
      toast.success('Аватар обновлён');
      onChanged?.(res.avatarUrl ?? null);
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Не удалось загрузить файл');
    } finally {
      setBusy(false);
    }
  };

  const remove = async () => {
    setBusy(true);
    try {
      await usersApi.removeAvatar();
      updateProfile({ avatarUrl: null });
      toast.success('Аватар удалён');
      onChanged?.(null);
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Не удалось удалить аватар');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="flex items-center gap-4">
      <div
        className="rounded-full bg-gradient-to-br from-brand-200 to-brand-500 text-white flex items-center justify-center font-semibold overflow-hidden border-2 border-white shadow"
        style={{ width: size, height: size, fontSize: size / 3 }}
      >
        {fullUrl
          ? <img src={fullUrl} alt={fullName || 'avatar'} className="w-full h-full object-cover" />
          : <span>{initials}</span>}
      </div>
      <div className="flex flex-col gap-2">
        <input
          ref={fileRef}
          type="file"
          accept="image/png,image/jpeg,image/webp,image/gif"
          className="hidden"
          onChange={(e) => {
            const f = e.target.files?.[0];
            if (f) handle(f);
            e.currentTarget.value = '';
          }}
        />
        <button
          type="button"
          className="btn-primary text-sm"
          disabled={busy}
          onClick={() => fileRef.current?.click()}
        >
          <Camera size={16} /> {avatarUrl ? 'Заменить аватар' : 'Загрузить аватар'}
        </button>
        {avatarUrl && (
          <button type="button" className="btn-ghost text-sm text-rose-600" onClick={remove} disabled={busy}>
            <Trash2 size={16} /> Удалить
          </button>
        )}
        <p className="text-xs text-slate-400">PNG/JPEG/WEBP, до 5 МБ</p>
      </div>
    </div>
  );
}
