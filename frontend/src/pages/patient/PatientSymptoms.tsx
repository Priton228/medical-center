import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { Activity, Sparkles } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import { recommendationsApi, symptomsApi } from '@/api/endpoints';
import { formatDateTime } from '@/utils/format';
import type { RecommendationResponse } from '@/types';

export default function PatientSymptoms() {
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [latest, setLatest] = useState<RecommendationResponse | null>(null);
  const qc = useQueryClient();

  const { data: symptoms, isLoading } = useQuery({ queryKey: ['symptoms'], queryFn: () => symptomsApi.list() });
  const { data: history } = useQuery({ queryKey: ['p-rec-history'], queryFn: () => recommendationsApi.history() });

  const analyze = useMutation({
    mutationFn: (ids: number[]) => recommendationsApi.analyze(ids),
    onSuccess: (rec) => {
      setLatest(rec);
      qc.invalidateQueries({ queryKey: ['p-rec-history'] });
      toast.success('Анализ выполнен');
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Ошибка анализа'),
  });

  const toggle = (id: number) => {
    const next = new Set(selected);
    if (next.has(id)) next.delete(id); else next.add(id);
    setSelected(next);
  };

  return (
    <>
      <PageHeader title="Анализ симптомов" subtitle="Отметьте беспокоящие симптомы — получите рекомендованного специалиста." />
      <div className="grid lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-4">
          <div className="card">
            <h3 className="font-semibold text-slate-800 mb-3 flex items-center gap-2"><Activity size={18} className="text-brand-600" /> Симптомы</h3>
            {isLoading ? <Loader /> :
              <div className="flex flex-wrap gap-2">
                {(symptoms ?? []).map((s) => {
                  const active = selected.has(s.id);
                  return (
                    <button
                      key={s.id}
                      type="button"
                      onClick={() => toggle(s.id)}
                      className={
                        'px-3 py-1.5 rounded-full border text-sm transition ' +
                        (active
                          ? 'bg-gradient-to-r from-brand-500 to-brand-700 text-white border-transparent shadow-soft'
                          : 'bg-white text-slate-700 border-brand-200 hover:bg-brand-50')
                      }
                      title={s.description ?? ''}
                    >
                      {s.name}
                    </button>
                  );
                })}
              </div>}
            <div className="mt-4 flex items-center justify-between">
              <span className="text-sm text-slate-500">Выбрано: <b>{selected.size}</b></span>
              <button
                className="btn-primary"
                disabled={selected.size === 0 || analyze.isPending}
                onClick={() => analyze.mutate(Array.from(selected))}
              >
                <Sparkles size={16} />
                {analyze.isPending ? 'Анализируем…' : 'Получить рекомендацию'}
              </button>
            </div>
          </div>

          {latest && (
            <motion.div
              initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}
              className="card"
            >
              <h3 className="font-semibold text-slate-800 mb-3">Результат анализа</h3>
              {latest.diagnosisName ? (
                <div className="space-y-2">
                  <div>
                    <div className="text-xs uppercase text-slate-500">Возможный диагноз</div>
                    <div className="text-xl font-bold text-brand-700">{latest.diagnosisName}</div>
                  </div>
                  <div>
                    <div className="text-xs uppercase text-slate-500">Уверенность</div>
                    <div className="flex items-center gap-2">
                      <div className="flex-1 bg-brand-100 rounded-full h-2 overflow-hidden">
                        <motion.div
                          className="h-full bg-gradient-to-r from-brand-500 to-brand-700"
                          initial={{ width: 0 }}
                          animate={{ width: `${latest.confidence}%` }}
                          transition={{ duration: 0.6 }}
                        />
                      </div>
                      <span className="font-semibold text-slate-700 text-sm">{latest.confidence}%</span>
                    </div>
                  </div>
                  {latest.recommendedDoctorName && (
                    <div>
                      <div className="text-xs uppercase text-slate-500">Рекомендованный врач</div>
                      <div className="font-medium">{latest.recommendedDoctorName}</div>
                      <div className="text-sm text-slate-500">{latest.recommendedDoctorSpecialization}</div>
                    </div>
                  )}
                </div>
              ) : (
                <div className="text-slate-500">По указанным симптомам совпадений не найдено. Обратитесь к терапевту.</div>
              )}
            </motion.div>
          )}
        </div>

        <div className="card">
          <h3 className="font-semibold text-slate-800 mb-3">История анализов</h3>
          {(history ?? []).length === 0 ? (
            <div className="text-sm text-slate-500">Пока пусто.</div>
          ) : (
            <ul className="space-y-3 max-h-[420px] overflow-y-auto pr-1">
              {(history ?? []).map((r) => (
                <li key={r.id} className="border-b border-brand-50 last:border-0 pb-2">
                  <div className="text-sm font-medium">{r.diagnosisName ?? 'Без совпадений'}</div>
                  <div className="text-xs text-slate-500">{formatDateTime(r.createdAt)} · {r.confidence}%</div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </>
  );
}
