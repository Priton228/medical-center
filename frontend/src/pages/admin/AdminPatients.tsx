import { useQuery } from '@tanstack/react-query';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import { patientsApi } from '@/api/endpoints';
import { formatDate } from '@/utils/format';

export default function AdminPatients() {
  const { data, isLoading } = useQuery({ queryKey: ['a-patients'], queryFn: () => patientsApi.list() });

  return (
    <>
      <PageHeader title="Пациенты" subtitle="Список зарегистрированных пациентов." />
      {isLoading ? <Loader /> : (
        <div className="card overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase text-slate-500 border-b">
                <th className="py-2 pr-3">ФИО</th>
                <th className="py-2 pr-3">Логин</th>
                <th className="py-2 pr-3">Email</th>
                <th className="py-2 pr-3">Дата рождения</th>
                <th className="py-2 pr-3">Полис</th>
              </tr>
            </thead>
            <tbody>
              {data!.content.map((p) => (
                <tr key={p.id} className="border-b last:border-0 hover:bg-brand-50/40">
                  <td className="py-2 pr-3 font-medium">{p.fullName}</td>
                  <td className="py-2 pr-3 text-slate-500">{p.username}</td>
                  <td className="py-2 pr-3 text-slate-500">{p.email}</td>
                  <td className="py-2 pr-3">{formatDate(p.birthDate ?? null)}</td>
                  <td className="py-2 pr-3">{p.insuranceNumber ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
