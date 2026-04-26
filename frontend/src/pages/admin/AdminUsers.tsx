import { useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import { usersApi } from '@/api/endpoints';
import { roleLabel } from '@/utils/format';

export default function AdminUsers() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['a-users'], queryFn: () => usersApi.list() });

  const toggle = async (id: number, enabled: boolean) => {
    try {
      await usersApi.setEnabled(id, enabled);
      toast.success('Статус обновлён');
      qc.invalidateQueries({ queryKey: ['a-users'] });
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Ошибка');
    }
  };

  return (
    <>
      <PageHeader title="Пользователи" subtitle="Управление учётными записями." />
      {isLoading ? <Loader /> : (
        <div className="card overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase text-slate-500 border-b">
                <th className="py-2 pr-3">ID</th>
                <th className="py-2 pr-3">Логин</th>
                <th className="py-2 pr-3">ФИО</th>
                <th className="py-2 pr-3">Email</th>
                <th className="py-2 pr-3">Роли</th>
                <th className="py-2 pr-3">Статус</th>
                <th className="py-2"></th>
              </tr>
            </thead>
            <tbody>
              {data!.content.map((u) => (
                <tr key={u.id} className="border-b last:border-0 hover:bg-brand-50/40">
                  <td className="py-2 pr-3">{u.id}</td>
                  <td className="py-2 pr-3 font-medium">{u.login}</td>
                  <td className="py-2 pr-3">{u.fullName}</td>
                  <td className="py-2 pr-3 text-slate-500">{u.email}</td>
                  <td className="py-2 pr-3">{u.roles.map(roleLabel).join(', ')}</td>
                  <td className="py-2 pr-3">{u.enabled ? <span className="chip">активен</span> : <span className="chip bg-rose-50 text-rose-700 border-rose-100">отключён</span>}</td>
                  <td className="py-2 text-right">
                    <button className="btn-ghost text-xs px-2 py-1" onClick={() => toggle(u.id, !u.enabled)}>
                      {u.enabled ? 'Отключить' : 'Включить'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
