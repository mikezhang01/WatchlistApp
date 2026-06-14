export function PageTitle({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="mb-6">
      <h1 className="text-3xl font-semibold tracking-normal">{title}</h1>
      {subtitle ? <p className="mt-1 max-w-2xl text-sm text-neutral-600">{subtitle}</p> : null}
    </div>
  );
}

export function Button(props: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      {...props}
      className={`rounded bg-accent px-4 py-2 text-sm font-medium text-white disabled:opacity-50 ${props.className ?? ""}`}
    />
  );
}

export function Field(props: React.InputHTMLAttributes<HTMLInputElement>) {
  return <input {...props} className={`w-full rounded border border-line px-3 py-2 ${props.className ?? ""}`} />;
}

export function TextArea(props: React.TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea {...props} className={`w-full rounded border border-line px-3 py-2 ${props.className ?? ""}`} />;
}

export function Panel({ children }: { children: React.ReactNode }) {
  return <section className="rounded border border-line bg-white p-4">{children}</section>;
}

