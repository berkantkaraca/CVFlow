import type { CvContent, CvSection, LanguageCode, TemplateCode } from '../types/cv'
import { SECTION_TITLES } from '../types/cv'

const SECTION_LABEL_TR: Record<CvSection, string> = {
  SUMMARY: 'Özet',
  EXPERIENCE: 'Deneyim',
  PROJECTS: 'Projeler',
  EDUCATION: 'Eğitim',
  SKILLS: 'Yetenekler',
  LANGUAGES: 'Diller',
  CERTIFICATIONS: 'Sertifikalar',
  LINKS: 'Bağlantılar',
}

function label(section: CvSection, language: LanguageCode): string {
  return language === 'TR' ? SECTION_LABEL_TR[section] : SECTION_TITLES[section]
}

function text(value: string | null | undefined): string {
  return value ?? ''
}

function CvPreviewSection({ section, content }: { section: CvSection; content: CvContent }) {
  switch (section) {
    case 'SUMMARY':
      return content.summary ? <p className="text-sm">{content.summary}</p> : null
    case 'EXPERIENCE':
      return (
        <div className="flex flex-col gap-3">
          {(content.experiences ?? []).map((e, i) => (
            <div key={e.id ?? i}>
              <div className="flex justify-between text-sm">
                <strong>
                  {text(e.position) || '—'} {e.company ? `@ ${e.company}` : ''}
                </strong>
                <span className="text-slate-500">
                  {[e.startDate, e.current ? 'Present' : e.endDate].filter(Boolean).join(' – ')}
                </span>
              </div>
              {(e.description ?? []).length > 0 && (
                <ul className="ml-5 list-disc text-sm">
                  {e.description!.map((d, j) => (
                    <li key={j}>{d}</li>
                  ))}
                </ul>
              )}
            </div>
          ))}
        </div>
      )
    case 'PROJECTS':
      return (
        <div className="flex flex-col gap-2">
          {(content.projects ?? []).map((p, i) => (
            <div key={p.id ?? i} className="text-sm">
              <strong>{text(p.name) || '—'}</strong>
              {p.description && <p>{p.description}</p>}
              {(p.technologies ?? []).length > 0 && (
                <p className="text-slate-500">{p.technologies!.join(', ')}</p>
              )}
            </div>
          ))}
        </div>
      )
    case 'EDUCATION':
      return (
        <div className="flex flex-col gap-2">
          {(content.education ?? []).map((e, i) => (
            <div key={e.id ?? i} className="text-sm">
              <strong>{text(e.institution) || '—'}</strong>
              <p className="text-slate-600">
                {[e.degree, e.field].filter(Boolean).join(', ')}
              </p>
            </div>
          ))}
        </div>
      )
    case 'SKILLS':
      return (
        <div className="flex flex-col gap-1 text-sm">
          {(content.skills ?? []).map((s, i) => (
            <div key={s.id ?? i}>
              <strong>{text(s.name)}</strong>
              {s.category && <span className="text-slate-500"> — {s.category}</span>}
            </div>
          ))}
        </div>
      )
    case 'LANGUAGES':
      return (
        <div className="flex flex-col gap-1 text-sm">
          {(content.languages ?? []).map((l, i) => (
            <div key={l.id ?? i}>
              <strong>{text(l.name)}</strong>
              {l.level && <span className="text-slate-500"> — {l.level}</span>}
            </div>
          ))}
        </div>
      )
    case 'CERTIFICATIONS':
      return (
        <div className="flex flex-col gap-1 text-sm">
          {(content.certifications ?? []).map((c, i) => (
            <div key={c.id ?? i}>
              <strong>{text(c.name)}</strong>
              {c.issuer && <span className="text-slate-500"> — {c.issuer}</span>}
            </div>
          ))}
        </div>
      )
    case 'LINKS':
      return (
        <div className="flex flex-col gap-1 text-sm">
          {(content.links ?? []).map((l, i) => (
            <div key={l.id ?? i}>
              <strong>{text(l.label)}</strong>
              {l.url && <span className="text-slate-500"> — {l.url}</span>}
            </div>
          ))}
        </div>
      )
  }
}

function hasContent(section: CvSection, content: CvContent): boolean {
  switch (section) {
    case 'SUMMARY':
      return Boolean(content.summary?.trim())
    case 'EXPERIENCE':
      return (content.experiences ?? []).length > 0
    case 'PROJECTS':
      return (content.projects ?? []).length > 0
    case 'EDUCATION':
      return (content.education ?? []).length > 0
    case 'SKILLS':
      return (content.skills ?? []).length > 0
    case 'LANGUAGES':
      return (content.languages ?? []).length > 0
    case 'CERTIFICATIONS':
      return (content.certifications ?? []).length > 0
    case 'LINKS':
      return (content.links ?? []).length > 0
  }
}

const frameClass: Record<TemplateCode, string> = {
  MODERN: 'rounded-lg border border-slate-200 bg-white',
  MINIMAL: 'border border-slate-200 bg-white',
  PROFESSIONAL: 'rounded border border-slate-300 bg-white',
  ATS: 'border border-slate-200 bg-white',
}

const headerClass: Record<TemplateCode, string> = {
  MODERN: 'rounded-t-lg bg-blue-700 px-6 py-5 text-white',
  MINIMAL: 'px-6 py-5 text-center',
  PROFESSIONAL: 'border-b-2 border-slate-900 px-6 py-4',
  ATS: 'px-6 py-4',
}

export function CvPreview({
  content,
  sectionOrder,
  template,
  language = 'EN',
}: {
  content: CvContent
  sectionOrder: CvSection[]
  template: TemplateCode
  language?: LanguageCode
}) {
  const personal = content.personal ?? {}
  const contacts = [personal.email, personal.phone, personal.location, personal.website, personal.linkedin, personal.github].filter(
    (c): c is string => Boolean(c),
  )
  return (
    <div className={frameClass[template]}>
      <div className={headerClass[template]}>
        {personal.photoUrl && (
          <img src={personal.photoUrl} alt="" className="mb-2 h-24 w-24 rounded-full object-cover" />
        )}
        <h1 className="text-2xl font-bold">{personal.fullName || 'Untitled CV'}</h1>
        {personal.title && <p className="text-sm opacity-90">{personal.title}</p>}
        {contacts.length > 0 && <p className="mt-1 text-xs opacity-80">{contacts.join(' · ')}</p>}
      </div>
      <div className="flex flex-col gap-4 px-6 py-4">
        {sectionOrder.map((section) =>
          hasContent(section, content) ? (
            <section key={section}>
              <h2
                className={
                  template === 'MINIMAL'
                    ? 'mb-2 text-center text-xs uppercase tracking-widest'
                    : template === 'MODERN'
                      ? 'mb-2 border-b border-blue-100 text-xs font-semibold uppercase tracking-wider text-blue-700'
                      : template === 'ATS'
                        ? 'mb-2 text-xs uppercase'
                        : 'mb-2 border-b border-slate-300 text-xs font-semibold uppercase'
                }
              >
                {label(section, language)}
              </h2>
              <CvPreviewSection section={section} content={content} />
            </section>
          ) : null,
        )}
        {(content.customSections ?? []).map(
          (custom, i) =>
            (custom.items ?? []).length > 0 && (
              <section key={custom.id ?? i}>
                <h2 className="mb-2 text-xs font-semibold uppercase">{custom.title || '—'}</h2>
                <ul className="ml-5 list-disc text-sm">
                  {custom.items!.map((item, j) => (
                    <li key={j}>{item}</li>
                  ))}
                </ul>
              </section>
            ),
        )}
      </div>
    </div>
  )
}
