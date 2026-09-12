import { useEffect, useRef, useState, type ReactNode } from 'react'
import { CvPreview } from '../../components/CvPreview'
import { Field, PrimaryButton, SecondaryButton, SectionCard, TextArea, TextInput } from '../../components/ui'
import { useUiStore } from '../../stores/uiStore'
import { ApiError } from '../../types/api'
import type {
  Certification,
  CvContent,
  CvLanguage,
  CvLink,
  CvSection,
  Education,
  Experience,
  LanguageCode,
  PersonalInfo,
  Project,
  Skill,
  TemplateCode,
} from '../../types/cv'
import { SECTION_TITLES } from '../../types/cv'

export interface EditorDoc {
  name: string
  content: CvContent
  sectionOrder: CvSection[]
  version: number
}

interface CvDocumentEditorProps {
  initial: EditorDoc
  onSave: (input: EditorDoc) => Promise<{ version: number }>
  onReload: () => void
  headerActions?: ReactNode
  fieldActions?: (
    section: CvSection,
    entityId: string | null,
    field: string,
    value: string,
  ) => ReactNode
  previewTemplate: TemplateCode
  previewLanguage: LanguageCode
}

function newId(): string {
  return crypto.randomUUID()
}

function asArray<T>(value: T[] | undefined | null): T[] {
  return value ?? []
}

function val(value: string | null | undefined): string {
  return value ?? ''
}

export function CvDocumentEditor({
  initial,
  onSave,
  onReload,
  headerActions,
  fieldActions,
  previewTemplate,
  previewLanguage,
}: CvDocumentEditorProps) {
  const [name, setName] = useState(initial.name)
  const [content, setContent] = useState<CvContent>(initial.content)
  const [sectionOrder, setSectionOrder] = useState<CvSection[]>(initial.sectionOrder)
  const [version, setVersion] = useState(initial.version)
  const [dirty, setDirty] = useState(false)
  const [saving, setSaving] = useState(false)
  const [conflict, setConflict] = useState(false)
  const [lastSaved, setLastSaved] = useState<string | null>(null)
  const stateRef = useRef({ name, content, sectionOrder, version })
  stateRef.current = { name, content, sectionOrder, version }
  const notify = useUiStore((s) => s.notify)

  function markDirty(patch: Partial<{ name: string; content: CvContent; sectionOrder: CvSection[] }>) {
    if (patch.name !== undefined) setName(patch.name)
    if (patch.content !== undefined) setContent(patch.content)
    if (patch.sectionOrder !== undefined) setSectionOrder(patch.sectionOrder)
    setDirty(true)
    setConflict(false)
  }

  async function save() {
    const state = stateRef.current
    if (saving || conflict) return
    setSaving(true)
    try {
      const result = await onSave({ name: state.name, content: state.content, sectionOrder: state.sectionOrder, version: state.version })
      setVersion(result.version)
      setDirty(false)
      setLastSaved(new Date().toLocaleTimeString())
    } catch (e) {
      if (e instanceof ApiError && e.code === 'VERSION_CONFLICT') {
        setConflict(true)
      } else {
        notify(e instanceof Error ? e.message : 'Save failed', 'error')
      }
    } finally {
      setSaving(false)
    }
  }

  useEffect(() => {
    if (!dirty || saving || conflict) return
    const timer = setTimeout(() => void save(), 800)
    return () => clearTimeout(timer)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dirty, name, content, sectionOrder, saving, conflict])

  function updatePersonal(patch: Partial<PersonalInfo>) {
    markDirty({ content: { ...content, personal: { ...(content.personal ?? {}), ...patch } } })
  }

  function moveSection(index: number, direction: -1 | 1) {
    const next = [...sectionOrder]
    const target = index + direction
    if (target < 0 || target >= next.length) return
    ;[next[index], next[target]] = [next[target], next[index]]
    markDirty({ sectionOrder: next })
  }

  function updateEntry<T extends { id?: string | null }>(
    list: T[] | undefined | null,
    id: string,
    patch: Partial<T>,
  ): T[] {
    return asArray(list).map((item) => (item.id === id ? { ...item, ...patch } : item))
  }

  function removeEntry<T extends { id?: string | null }>(list: T[] | undefined | null, id: string): T[] {
    return asArray(list).filter((item) => item.id !== id)
  }

  function aiActions(section: CvSection, entityId: string | null, field: string, value: string) {
    if (!fieldActions || !value.trim()) return null
    return <div className="flex gap-1">{fieldActions(section, entityId, field, value)}</div>
  }

  return (
    <div>
      {conflict && (
        <div className="mb-4 rounded-md border border-amber-300 bg-amber-50 p-3 text-sm">
          <strong>Version conflict (409).</strong> This document was modified elsewhere. Your edits were
          kept locally, but saving is paused.
          <button onClick={onReload} className="ml-3 rounded bg-amber-600 px-3 py-1 text-white">
            Reload latest
          </button>
        </div>
      )}
      <div className="mb-4 flex flex-wrap items-center gap-3">
        <input
          value={name}
          onChange={(e) => markDirty({ name: e.target.value })}
          className="min-w-0 flex-1 rounded-md border border-slate-300 px-3 py-2 text-lg font-semibold"
        />
        {headerActions}
        <PrimaryButton onClick={() => void save()} disabled={saving || !dirty || conflict}>
          {saving ? 'Saving…' : 'Save'}
        </PrimaryButton>
        <span className="text-xs text-slate-500">
          {conflict ? 'Paused (conflict)' : dirty ? (saving ? 'Saving…' : 'Unsaved changes') : lastSaved ? `Saved ${lastSaved}` : `v${version}`}
        </span>
      </div>
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <div className="flex flex-col gap-4">
          <SectionCard title="Personal Information">
            <div className="grid grid-cols-2 gap-3">
              <Field label="Full name">
                <TextInput value={val(content.personal?.fullName)} onChange={(e) => updatePersonal({ fullName: e.target.value })} />
              </Field>
              <Field label="Title">
                <TextInput value={val(content.personal?.title)} onChange={(e) => updatePersonal({ title: e.target.value })} />
              </Field>
              <Field label="Email">
                <TextInput value={val(content.personal?.email)} onChange={(e) => updatePersonal({ email: e.target.value })} />
              </Field>
              <Field label="Phone">
                <TextInput value={val(content.personal?.phone)} onChange={(e) => updatePersonal({ phone: e.target.value })} />
              </Field>
              <Field label="Location">
                <TextInput value={val(content.personal?.location)} onChange={(e) => updatePersonal({ location: e.target.value })} />
              </Field>
              <Field label="Website">
                <TextInput value={val(content.personal?.website)} onChange={(e) => updatePersonal({ website: e.target.value })} />
              </Field>
              <Field label="LinkedIn">
                <TextInput value={val(content.personal?.linkedin)} onChange={(e) => updatePersonal({ linkedin: e.target.value })} />
              </Field>
              <Field label="GitHub">
                <TextInput value={val(content.personal?.github)} onChange={(e) => updatePersonal({ github: e.target.value })} />
              </Field>
            </div>
          </SectionCard>

          {sectionOrder.map((section, index) => (
            <SectionCard
              key={section}
              title={SECTION_TITLES[section]}
              actions={
                <>
                  <SecondaryButton onClick={() => moveSection(index, -1)} disabled={index === 0} title="Move up">↑</SecondaryButton>
                  <SecondaryButton
                    onClick={() => moveSection(index, 1)}
                    disabled={index === sectionOrder.length - 1}
                    title="Move down"
                  >
                    ↓
                  </SecondaryButton>
                </>
              }
            >
              {section === 'SUMMARY' && (
                <>
                  <TextArea
                    value={val(content.summary)}
                    onChange={(e) => markDirty({ content: { ...content, summary: e.target.value } })}
                    rows={4}
                  />
                  {aiActions('SUMMARY', null, 'summary', val(content.summary))}
                </>
              )}

              {section === 'EXPERIENCE' && (
                <>
                  {asArray(content.experiences).map((exp: Experience) => (
                    <div key={exp.id} className="rounded border border-slate-200 p-3">
                      <div className="grid grid-cols-2 gap-2">
                        <Field label="Company">
                          <TextInput
                            value={val(exp.company)}
                            onChange={(e) =>
                              markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { company: e.target.value }) } })
                            }
                          />
                        </Field>
                        <Field label="Position">
                          <TextInput
                            value={val(exp.position)}
                            onChange={(e) =>
                              markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { position: e.target.value }) } })
                            }
                          />
                        </Field>
                        <Field label="Location">
                          <TextInput
                            value={val(exp.location)}
                            onChange={(e) =>
                              markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { location: e.target.value }) } })
                            }
                          />
                        </Field>
                        <div className="grid grid-cols-3 gap-2">
                          <Field label="Start (YYYY-MM)">
                            <TextInput
                              value={val(exp.startDate)}
                              onChange={(e) =>
                                markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { startDate: e.target.value }) } })
                              }
                            />
                          </Field>
                          <Field label="End (YYYY-MM)">
                            <TextInput
                              value={val(exp.endDate)}
                              disabled={exp.current}
                              onChange={(e) =>
                                markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { endDate: e.target.value }) } })
                              }
                            />
                          </Field>
                          <label className="flex items-end gap-1 pb-2 text-sm">
                            <input
                              type="checkbox"
                              checked={exp.current ?? false}
                              onChange={(e) =>
                                markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { current: e.target.checked }) } })
                              }
                            />
                            Current
                          </label>
                        </div>
                      </div>
                      <div className="mt-2">
                        <span className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">Bullets</span>
                        {asArray(exp.description).map((bullet, bi) => (
                          <div key={bi} className="mb-1 flex gap-1">
                            <TextInput
                              value={bullet}
                              onChange={(e) => {
                                const next = [...asArray(exp.description)]
                                next[bi] = e.target.value
                                markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { description: next }) } })
                              }}
                            />
                            <SecondaryButton
                              onClick={() => {
                                const next = asArray(exp.description).filter((_, j) => j !== bi)
                                markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { description: next }) } })
                              }}
                            >
                              ✕
                            </SecondaryButton>
                          </div>
                        ))}
                        {aiActions('EXPERIENCE', exp.id!, 'description', asArray(exp.description).join('\n'))}
                        <div className="mt-1 flex gap-1">
                          <SecondaryButton
                            onClick={() =>
                              markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { description: [...asArray(exp.description), ''] }) } })
                            }
                          >
                            + Bullet
                          </SecondaryButton>
                          <SecondaryButton
                            onClick={() =>
                              markDirty({ content: { ...content, experiences: removeEntry(content.experiences, exp.id!) } })
                            }
                          >
                            Remove entry
                          </SecondaryButton>
                        </div>
                      </div>
                    </div>
                  ))}
                  <SecondaryButton
                    onClick={() =>
                      markDirty({
                        content: {
                          ...content,
                          experiences: [...asArray(content.experiences), { id: newId(), description: [] as string[] }],
                        },
                      })
                    }
                  >
                    + Add experience
                  </SecondaryButton>
                </>
              )}

              {section === 'EDUCATION' && (
                <>
                  {asArray(content.education).map((edu: Education) => (
                    <div key={edu.id} className="rounded border border-slate-200 p-3">
                      <div className="grid grid-cols-2 gap-2">
                        <Field label="Institution">
                          <TextInput value={val(edu.institution)} onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { institution: e.target.value }) } })} />
                        </Field>
                        <Field label="Degree">
                          <TextInput value={val(edu.degree)} onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { degree: e.target.value }) } })} />
                        </Field>
                        <Field label="Field">
                          <TextInput value={val(edu.field)} onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { field: e.target.value }) } })} />
                        </Field>
                        <Field label="Dates">
                          <TextInput
                            value={`${val(edu.startDate)}${edu.endDate ? ` – ${edu.endDate}` : ''}`}
                            placeholder="YYYY-MM – YYYY-MM"
                            onChange={(e) => {
                              const [s, en] = e.target.value.split('–').map((x) => x.trim())
                              markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { startDate: s, endDate: en }) } })
                            }}
                          />
                        </Field>
                      </div>
                      <Field label="Description">
                        <TextArea value={val(edu.description)} onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { description: e.target.value }) } })} />
                      </Field>
                      {aiActions('EDUCATION', edu.id!, 'description', val(edu.description))}
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, education: removeEntry(content.education, edu.id!) } })}>
                        Remove entry
                      </SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, education: [...asArray(content.education), { id: newId() }] } })}>
                    + Add education
                  </SecondaryButton>
                </>
              )}

              {section === 'PROJECTS' && (
                <>
                  {asArray(content.projects).map((p: Project) => (
                    <div key={p.id} className="rounded border border-slate-200 p-3">
                      <div className="grid grid-cols-2 gap-2">
                        <Field label="Name">
                          <TextInput value={val(p.name)} onChange={(e) => markDirty({ content: { ...content, projects: updateEntry(content.projects, p.id!, { name: e.target.value }) } })} />
                        </Field>
                        <Field label="URL">
                          <TextInput value={val(p.url)} onChange={(e) => markDirty({ content: { ...content, projects: updateEntry(content.projects, p.id!, { url: e.target.value }) } })} />
                        </Field>
                      </div>
                      <Field label="Description">
                        <TextArea value={val(p.description)} onChange={(e) => markDirty({ content: { ...content, projects: updateEntry(content.projects, p.id!, { description: e.target.value }) } })} />
                      </Field>
                      {aiActions('PROJECTS', p.id!, 'description', val(p.description))}
                      <Field label="Technologies (comma separated)">
                        <TextInput
                          value={(p.technologies ?? []).join(', ')}
                          onChange={(e) =>
                            markDirty({ content: { ...content, projects: updateEntry(content.projects, p.id!, { technologies: e.target.value.split(',').map((x) => x.trim()).filter(Boolean) }) } })
                          }
                        />
                      </Field>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, projects: removeEntry(content.projects, p.id!) } })}>
                        Remove entry
                      </SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, projects: [...asArray(content.projects), { id: newId(), technologies: [] }] } })}>
                    + Add project
                  </SecondaryButton>
                </>
              )}

              {section === 'SKILLS' && (
                <>
                  {asArray(content.skills).map((s: Skill) => (
                    <div key={s.id} className="flex gap-2">
                      <div className="flex-1">
                        <TextInput value={val(s.name)} placeholder="Skill" onChange={(e) => markDirty({ content: { ...content, skills: updateEntry(content.skills, s.id!, { name: e.target.value }) } })} />
                      </div>
                      <div className="flex-1">
                        <TextInput value={val(s.category)} placeholder="Category" onChange={(e) => markDirty({ content: { ...content, skills: updateEntry(content.skills, s.id!, { category: e.target.value }) } })} />
                      </div>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, skills: removeEntry(content.skills, s.id!) } })}>✕</SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, skills: [...asArray(content.skills), { id: newId() }] } })}>
                    + Add skill
                  </SecondaryButton>
                </>
              )}

              {section === 'LANGUAGES' && (
                <>
                  {asArray(content.languages).map((l: CvLanguage) => (
                    <div key={l.id} className="flex gap-2">
                      <div className="flex-1">
                        <TextInput value={val(l.name)} placeholder="Language" onChange={(e) => markDirty({ content: { ...content, languages: updateEntry(content.languages, l.id!, { name: e.target.value }) } })} />
                      </div>
                      <div className="flex-1">
                        <TextInput value={val(l.level)} placeholder="Level" onChange={(e) => markDirty({ content: { ...content, languages: updateEntry(content.languages, l.id!, { level: e.target.value }) } })} />
                      </div>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, languages: removeEntry(content.languages, l.id!) } })}>✕</SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, languages: [...asArray(content.languages), { id: newId() }] } })}>
                    + Add language
                  </SecondaryButton>
                </>
              )}

              {section === 'CERTIFICATIONS' && (
                <>
                  {asArray(content.certifications).map((c: Certification) => (
                    <div key={c.id} className="rounded border border-slate-200 p-3">
                      <div className="grid grid-cols-2 gap-2">
                        <Field label="Name">
                          <TextInput value={val(c.name)} onChange={(e) => markDirty({ content: { ...content, certifications: updateEntry(content.certifications, c.id!, { name: e.target.value }) } })} />
                        </Field>
                        <Field label="Issuer">
                          <TextInput value={val(c.issuer)} onChange={(e) => markDirty({ content: { ...content, certifications: updateEntry(content.certifications, c.id!, { issuer: e.target.value }) } })} />
                        </Field>
                        <Field label="Date">
                          <TextInput value={val(c.date)} onChange={(e) => markDirty({ content: { ...content, certifications: updateEntry(content.certifications, c.id!, { date: e.target.value }) } })} />
                        </Field>
                        <Field label="URL">
                          <TextInput value={val(c.url)} onChange={(e) => markDirty({ content: { ...content, certifications: updateEntry(content.certifications, c.id!, { url: e.target.value }) } })} />
                        </Field>
                      </div>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, certifications: removeEntry(content.certifications, c.id!) } })}>
                        Remove entry
                      </SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, certifications: [...asArray(content.certifications), { id: newId() }] } })}>
                    + Add certification
                  </SecondaryButton>
                </>
              )}

              {section === 'LINKS' && (
                <>
                  {asArray(content.links).map((l: CvLink) => (
                    <div key={l.id} className="flex gap-2">
                      <div className="flex-1">
                        <TextInput value={val(l.label)} placeholder="Label" onChange={(e) => markDirty({ content: { ...content, links: updateEntry(content.links, l.id!, { label: e.target.value }) } })} />
                      </div>
                      <div className="flex-1">
                        <TextInput value={val(l.url)} placeholder="URL" onChange={(e) => markDirty({ content: { ...content, links: updateEntry(content.links, l.id!, { url: e.target.value }) } })} />
                      </div>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, links: removeEntry(content.links, l.id!) } })}>✕</SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, links: [...asArray(content.links), { id: newId() }] } })}>
                    + Add link
                  </SecondaryButton>
                </>
              )}
            </SectionCard>
          ))}
        </div>

        <div className="xl:sticky xl:top-4 xl:self-start">
          <h3 className="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500">Live preview</h3>
          <CvPreview content={content} sectionOrder={sectionOrder} template={previewTemplate} language={previewLanguage} />
        </div>
      </div>
    </div>
  )
}
