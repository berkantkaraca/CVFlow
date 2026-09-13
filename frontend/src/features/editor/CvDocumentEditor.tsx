import { useEffect, useRef, useState, type DragEvent, type ReactNode } from 'react'
import { CvPreview } from '../../components/CvPreview'
import { Field, PrimaryButton, SecondaryButton, SectionCard, TextArea, TextInput } from '../../components/ui'
import { useUiStore } from '../../stores/uiStore'
import { useT, type I18nKey } from '../../i18n'
import { ApiError } from '../../types/api'
import type {
  Certification,
  CustomSection,
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

function moveInList<T>(items: T[], from: number, to: number): T[] {
  if (from < 0 || to < 0 || from >= items.length || to >= items.length || from === to) return items
  const next = [...items]
  const [moved] = next.splice(from, 1)
  next.splice(to, 0, moved)
  return next
}

function dragSource(listId: string, index: number) {
  return {
    draggable: true,
    onDragStart: (e: DragEvent) => {
      e.dataTransfer.setData('application/x-cvflow', `${listId}:${index}`)
      e.dataTransfer.effectAllowed = 'move'
    },
  }
}

function dropTarget(listId: string, index: number, onMove: (from: number, to: number) => void) {
  return {
    onDragOver: (e: DragEvent) => e.preventDefault(),
    onDrop: (e: DragEvent) => {
      e.preventDefault()
      const [id, from] = e.dataTransfer.getData('application/x-cvflow').split(':')
      if (id === listId) onMove(Number(from), index)
    },
  }
}

function DragHandle({ listId, index }: { listId: string; index: number }) {
  return (
    <span
      {...dragSource(listId, index)}
      title="Drag to reorder"
      className="cursor-grab rounded px-1 text-ink-300 hover:bg-paper active:cursor-grabbing"
    >
      ⠿
    </span>
  )
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
  const t = useT()

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

  function moveSectionTo(from: number, to: number) {
    markDirty({ sectionOrder: moveInList(sectionOrder, from, to) })
  }

  function moveCustomTo(from: number, to: number) {
    markDirty({ content: { ...content, customSections: moveInList(content.customSections ?? [], from, to) } })
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
          <strong>{t('editor.conflict')}</strong>
          <button onClick={onReload} className="ml-3 rounded bg-amber-600 px-3 py-1 text-white">
            {t('editor.reload')}
          </button>
        </div>
      )}
      <div className="mb-4 flex flex-wrap items-center gap-3">
        <input
          value={name}
          onChange={(e) => markDirty({ name: e.target.value })}
          className="min-w-0 flex-1 rounded-xl border border-line bg-card px-4 py-2.5 text-lg font-semibold tracking-tight"
        />
        {headerActions}
        <PrimaryButton onClick={() => void save()} disabled={saving || !dirty || conflict}>
          {saving ? t('common.saving') : t('common.save')}
        </PrimaryButton>
        <span className="text-xs text-ink-500">
          {conflict ? t('editor.paused') : dirty ? (saving ? t('common.saving') : t('editor.unsaved')) : lastSaved ? `${t('editor.savedAt')} ${lastSaved}` : `v${version}`}
        </span>
      </div>
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <div className="flex flex-col gap-4">
          <SectionCard title={t('editor.personalInfo')}>
            <div className="grid grid-cols-2 gap-3">
              <Field label={t('editor.fullName')}>
                <TextInput value={val(content.personal?.fullName)} onChange={(e) => updatePersonal({ fullName: e.target.value })} />
              </Field>
              <Field label={t('editor.title')}>
                <TextInput value={val(content.personal?.title)} onChange={(e) => updatePersonal({ title: e.target.value })} />
              </Field>
              <Field label={t('editor.email')}>
                <TextInput value={val(content.personal?.email)} onChange={(e) => updatePersonal({ email: e.target.value })} />
              </Field>
              <Field label={t('editor.phone')}>
                <TextInput value={val(content.personal?.phone)} onChange={(e) => updatePersonal({ phone: e.target.value })} />
              </Field>
              <Field label={t('editor.location')}>
                <TextInput value={val(content.personal?.location)} onChange={(e) => updatePersonal({ location: e.target.value })} />
              </Field>
              <Field label={t('editor.website')}>
                <TextInput value={val(content.personal?.website)} onChange={(e) => updatePersonal({ website: e.target.value })} />
              </Field>
              <Field label={t('editor.linkedin')}>
                <TextInput value={val(content.personal?.linkedin)} onChange={(e) => updatePersonal({ linkedin: e.target.value })} />
              </Field>
              <Field label={t('editor.github')}>
                <TextInput value={val(content.personal?.github)} onChange={(e) => updatePersonal({ github: e.target.value })} />
              </Field>
              <Field label={t('editor.photoUrl')}>
                <TextInput value={val(content.personal?.photoUrl)} placeholder="https://…" onChange={(e) => updatePersonal({ photoUrl: e.target.value })} />
              </Field>
            </div>
            {content.personal?.photoUrl && (
              <img src={content.personal.photoUrl} alt="" className="mt-1 h-16 w-16 rounded-full object-cover" />
            )}
          </SectionCard>

          {sectionOrder.map((section, index) => (
            <div key={section} {...dropTarget('sections', index, moveSectionTo)}>
            <SectionCard
              title={t(('section.' + section) as I18nKey)}
              actions={
                <>
                  <DragHandle listId="sections" index={index} />
                  <SecondaryButton onClick={() => moveSection(index, -1)} disabled={index === 0} title={t('editor.moveUp')}>↑</SecondaryButton>
                  <SecondaryButton
                    onClick={() => moveSection(index, 1)}
                    disabled={index === sectionOrder.length - 1}
                    title={t('editor.moveDown')}
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
                  {asArray(content.experiences).map((exp: Experience, ei: number) => (
                    <div
                      key={exp.id}
                      {...dropTarget('experiences', ei, (from, to) =>
                        markDirty({
                          content: {
                            ...content,
                            experiences: moveInList(asArray(content.experiences), from, to),
                          },
                        }),
                      )}
                      className="rounded border border-line p-3"
                    >
                      <div className="mb-1 flex items-center gap-1">
                        <DragHandle listId="experiences" index={ei} />
                        <span className="text-xs text-ink-300">{t('editor.dragHint')}</span>
                      </div>
                      <div className="grid grid-cols-2 gap-2">
                        <Field label={t('editor.company')}>
                          <TextInput
                            value={val(exp.company)}
                            onChange={(e) =>
                              markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { company: e.target.value }) } })
                            }
                          />
                        </Field>
                        <Field label={t('editor.position')}>
                          <TextInput
                            value={val(exp.position)}
                            onChange={(e) =>
                              markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { position: e.target.value }) } })
                            }
                          />
                        </Field>
                        <Field label={t('editor.location')}>
                          <TextInput
                            value={val(exp.location)}
                            onChange={(e) =>
                              markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { location: e.target.value }) } })
                            }
                          />
                        </Field>
                        <div className="grid grid-cols-3 gap-2">
                          <Field label={t('editor.start')}>
                            <TextInput
                              type="month"
                              value={val(exp.startDate)}
                              onChange={(e) =>
                                markDirty({ content: { ...content, experiences: updateEntry(content.experiences, exp.id!, { startDate: e.target.value }) } })
                              }
                            />
                          </Field>
                          <Field label={t('editor.end')}>
                            <TextInput
                              type="month"
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
                          </label>                        </div>
                      </div>
                      <div className="mt-2">
                        <span className="mb-1 block text-xs font-medium uppercase tracking-wide text-ink-500">{t('editor.bullets')}</span>
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
                            {t('editor.addBullet')}
                          </SecondaryButton>
                          <SecondaryButton
                            onClick={() =>
                              markDirty({ content: { ...content, experiences: removeEntry(content.experiences, exp.id!) } })
                            }
                          >
                            {t('editor.removeEntry')}
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
                    {t('editor.addExperience')}
                  </SecondaryButton>
                </>
              )}

              {section === 'EDUCATION' && (
                <>
                  {asArray(content.education).map((edu: Education, edi: number) => (
                    <div
                      key={edu.id}
                      {...dropTarget('education', edi, (from, to) =>
                        markDirty({
                          content: {
                            ...content,
                            education: moveInList(asArray(content.education), from, to),
                          },
                        }),
                      )}
                      className="rounded border border-line p-3"
                    >
                      <div className="mb-1 flex items-center gap-1">
                        <DragHandle listId="education" index={edi} />
                        <span className="text-xs text-ink-300">{t('editor.dragHint')}</span>
                      </div>
                      <div className="grid grid-cols-2 gap-2">
                        <Field label={t('editor.institution')}>
                          <TextInput value={val(edu.institution)} onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { institution: e.target.value }) } })} />
                        </Field>
                        <Field label={t('editor.degree')}>
                          <TextInput value={val(edu.degree)} onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { degree: e.target.value }) } })} />
                        </Field>
                        <Field label={t('editor.field')}>
                          <TextInput value={val(edu.field)} onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { field: e.target.value }) } })} />
                        </Field>
                        <Field label={t('editor.location')}>
                          <TextInput value={val(edu.location)} onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { location: e.target.value }) } })} />
                        </Field>
                        <Field label={t('editor.start')}>
                          <TextInput
                            type="month"
                            value={val(edu.startDate)}
                            onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { startDate: e.target.value }) } })}
                          />
                        </Field>
                        <Field label={t('editor.end')}>
                          <TextInput
                            type="month"
                            value={val(edu.endDate)}
                            onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { endDate: e.target.value }) } })}
                          />
                        </Field>
                      </div>
                      <Field label={t('editor.description')}>
                        <TextArea value={val(edu.description)} onChange={(e) => markDirty({ content: { ...content, education: updateEntry(content.education, edu.id!, { description: e.target.value }) } })} />
                      </Field>
                      {aiActions('EDUCATION', edu.id!, 'description', val(edu.description))}
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, education: removeEntry(content.education, edu.id!) } })}>
                        {t('editor.removeEntry')}
                      </SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, education: [...asArray(content.education), { id: newId() }] } })}>
                    {t('editor.addEducation')}
                  </SecondaryButton>
                </>
              )}

              {section === 'PROJECTS' && (
                <>
                  {asArray(content.projects).map((p: Project, pi: number) => (
                    <div
                      key={p.id}
                      {...dropTarget('projects', pi, (from, to) =>
                        markDirty({
                          content: {
                            ...content,
                            projects: moveInList(asArray(content.projects), from, to),
                          },
                        }),
                      )}
                      className="rounded border border-line p-3"
                    >
                      <div className="mb-1 flex items-center gap-1">
                        <DragHandle listId="projects" index={pi} />
                        <span className="text-xs text-ink-300">{t('editor.dragHint')}</span>
                      </div>
                      <div className="grid grid-cols-2 gap-2">
                        <Field label={t('editor.pname')}>
                          <TextInput value={val(p.name)} onChange={(e) => markDirty({ content: { ...content, projects: updateEntry(content.projects, p.id!, { name: e.target.value }) } })} />
                        </Field>
                        <Field label={t('editor.url')}>
                          <TextInput value={val(p.url)} onChange={(e) => markDirty({ content: { ...content, projects: updateEntry(content.projects, p.id!, { url: e.target.value }) } })} />
                        </Field>
                      </div>
                      <Field label={t('editor.description')}>
                        <TextArea value={val(p.description)} onChange={(e) => markDirty({ content: { ...content, projects: updateEntry(content.projects, p.id!, { description: e.target.value }) } })} />
                      </Field>
                      {aiActions('PROJECTS', p.id!, 'description', val(p.description))}
                      <Field label={t('editor.technologies')}>
                        <TextInput
                          value={(p.technologies ?? []).join(', ')}
                          onChange={(e) =>
                            markDirty({ content: { ...content, projects: updateEntry(content.projects, p.id!, { technologies: e.target.value.split(',').map((x) => x.trim()).filter(Boolean) }) } })
                          }
                        />
                      </Field>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, projects: removeEntry(content.projects, p.id!) } })}>
                        {t('editor.removeEntry')}
                      </SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, projects: [...asArray(content.projects), { id: newId(), technologies: [] }] } })}>
                    {t('editor.addProject')}
                  </SecondaryButton>
                </>
              )}

              {section === 'SKILLS' && (
                <>
                  {asArray(content.skills).map((s: Skill) => (
                    <div key={s.id} className="flex gap-2">
                      <div className="flex-1">
                        <TextInput value={val(s.name)} placeholder={t('editor.skillPh')} onChange={(e) => markDirty({ content: { ...content, skills: updateEntry(content.skills, s.id!, { name: e.target.value }) } })} />
                      </div>
                      <div className="flex-1">
                        <TextInput value={val(s.category)} placeholder={t('editor.categoryPh')} onChange={(e) => markDirty({ content: { ...content, skills: updateEntry(content.skills, s.id!, { category: e.target.value }) } })} />
                      </div>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, skills: removeEntry(content.skills, s.id!) } })}>✕</SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, skills: [...asArray(content.skills), { id: newId() }] } })}>
                    {t('editor.addSkill')}
                  </SecondaryButton>
                </>
              )}

              {section === 'LANGUAGES' && (
                <>
                  {asArray(content.languages).map((l: CvLanguage) => (
                    <div key={l.id} className="flex gap-2">
                      <div className="flex-1">
                        <TextInput value={val(l.name)} placeholder={t('editor.languagePh')} onChange={(e) => markDirty({ content: { ...content, languages: updateEntry(content.languages, l.id!, { name: e.target.value }) } })} />
                      </div>
                      <div className="flex-1">
                        <TextInput value={val(l.level)} placeholder={t('editor.levelPh')} onChange={(e) => markDirty({ content: { ...content, languages: updateEntry(content.languages, l.id!, { level: e.target.value }) } })} />
                      </div>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, languages: removeEntry(content.languages, l.id!) } })}>✕</SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, languages: [...asArray(content.languages), { id: newId() }] } })}>
                    {t('editor.addLanguageEntry')}
                  </SecondaryButton>
                </>
              )}

              {section === 'CERTIFICATIONS' && (
                <>
                  {asArray(content.certifications).map((c: Certification) => (
                    <div key={c.id} className="rounded border border-line p-3">
                      <div className="grid grid-cols-2 gap-2">
                        <Field label={t('editor.pname')}>
                          <TextInput value={val(c.name)} onChange={(e) => markDirty({ content: { ...content, certifications: updateEntry(content.certifications, c.id!, { name: e.target.value }) } })} />
                        </Field>
                        <Field label={t('editor.issuer')}>
                          <TextInput value={val(c.issuer)} onChange={(e) => markDirty({ content: { ...content, certifications: updateEntry(content.certifications, c.id!, { issuer: e.target.value }) } })} />
                        </Field>
                        <Field label={t('editor.date')}>
                          <TextInput value={val(c.date)} onChange={(e) => markDirty({ content: { ...content, certifications: updateEntry(content.certifications, c.id!, { date: e.target.value }) } })} />
                        </Field>
                        <Field label={t('editor.url')}>
                          <TextInput value={val(c.url)} onChange={(e) => markDirty({ content: { ...content, certifications: updateEntry(content.certifications, c.id!, { url: e.target.value }) } })} />
                        </Field>
                      </div>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, certifications: removeEntry(content.certifications, c.id!) } })}>
                        {t('editor.removeEntry')}
                      </SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, certifications: [...asArray(content.certifications), { id: newId() }] } })}>
                    {t('editor.addCertification')}
                  </SecondaryButton>
                </>
              )}

              {section === 'LINKS' && (
                <>
                  {asArray(content.links).map((l: CvLink) => (
                    <div key={l.id} className="flex gap-2">
                      <div className="flex-1">
                        <TextInput value={val(l.label)} placeholder={t('editor.labelPh')} onChange={(e) => markDirty({ content: { ...content, links: updateEntry(content.links, l.id!, { label: e.target.value }) } })} />
                      </div>
                      <div className="flex-1">
                        <TextInput value={val(l.url)} placeholder={t('editor.urlPh')} onChange={(e) => markDirty({ content: { ...content, links: updateEntry(content.links, l.id!, { url: e.target.value }) } })} />
                      </div>
                      <SecondaryButton onClick={() => markDirty({ content: { ...content, links: removeEntry(content.links, l.id!) } })}>✕</SecondaryButton>
                    </div>
                  ))}
                  <SecondaryButton onClick={() => markDirty({ content: { ...content, links: [...asArray(content.links), { id: newId() }] } })}>
                    {t('editor.addLink')}
                  </SecondaryButton>
                </>
              )}
            </SectionCard>
            </div>
          ))}
          <SectionCard title="Custom sections">
            {(content.customSections ?? []).map((custom: CustomSection, ci: number) => (
              <div key={custom.id} {...dropTarget('customs', ci, moveCustomTo)} className="rounded border border-line p-3">
                <div className="mb-2 flex items-center gap-2">
                  <DragHandle listId="customs" index={ci} />
                  <div className="flex-1">
                    <TextInput
                      value={custom.title ?? ''}
                      placeholder="Section title (e.g. Hobbies)"
                      onChange={(e) =>
                        markDirty({ content: { ...content, customSections: updateEntry(content.customSections, custom.id!, { title: e.target.value }) } })
                      }
                    />
                  </div>
                  <SecondaryButton
                    onClick={() =>
                      markDirty({ content: { ...content, customSections: removeEntry(content.customSections, custom.id!) } })
                    }
                  >
                    Remove
                  </SecondaryButton>
                </div>
                {(custom.items ?? []).map((item, ii) => (
                  <div key={ii} className="mb-1 flex gap-1">
                    <TextInput
                      value={item}
                      onChange={(e) => {
                        const next = [...(custom.items ?? [])]
                        next[ii] = e.target.value
                        markDirty({ content: { ...content, customSections: updateEntry(content.customSections, custom.id!, { items: next }) } })
                      }}
                    />
                    <SecondaryButton
                      onClick={() => {
                        const next = (custom.items ?? []).filter((_, j) => j !== ii)
                        markDirty({ content: { ...content, customSections: updateEntry(content.customSections, custom.id!, { items: next }) } })
                      }}
                    >
                      ✕
                    </SecondaryButton>
                  </div>
                ))}
                <SecondaryButton
                  onClick={() =>
                    markDirty({ content: { ...content, customSections: updateEntry(content.customSections, custom.id!, { items: [...(custom.items ?? []), ''] }) } })
                  }
                >
                  {t('editor.addItem')}
                </SecondaryButton>
              </div>
            ))}
            <SecondaryButton
              onClick={() =>
                markDirty({ content: { ...content, customSections: [...(content.customSections ?? []), { id: newId(), title: '', items: [] }] } })
              }
            >
              {t('editor.addCustomSection')}
            </SecondaryButton>
          </SectionCard>
        </div>

        <div className="xl:sticky xl:top-4 xl:self-start">
          <h3 className="mb-2 text-sm font-semibold uppercase tracking-wide text-ink-500">{t('editor.livePreview')}</h3>
          <CvPreview content={content} sectionOrder={sectionOrder} template={previewTemplate} language={previewLanguage} />
        </div>
      </div>
    </div>
  )
}
