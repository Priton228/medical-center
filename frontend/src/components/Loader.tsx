export default function Loader({ label = 'Загрузка…' }: { label?: string }) {
  return (
    <div className="flex items-center justify-center py-10 text-brand-600 gap-2">
      <span className="w-2.5 h-2.5 bg-brand-500 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
      <span className="w-2.5 h-2.5 bg-brand-500 rounded-full animate-bounce" style={{ animationDelay: '120ms' }} />
      <span className="w-2.5 h-2.5 bg-brand-500 rounded-full animate-bounce" style={{ animationDelay: '240ms' }} />
      <span className="ml-2 text-sm text-slate-500">{label}</span>
    </div>
  );
}
