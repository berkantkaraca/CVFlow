export type LanguageCode = 'TR' | 'EN' | 'DE' | 'FR' | 'ES'
export type TemplateCode = 'MODERN' | 'MINIMAL' | 'PROFESSIONAL' | 'ATS'
export type CvSection =
  | 'SUMMARY'
  | 'EXPERIENCE'
  | 'PROJECTS'
  | 'EDUCATION'
  | 'SKILLS'
  | 'LANGUAGES'
  | 'CERTIFICATIONS'
  | 'LINKS'

export const DEFAULT_SECTION_ORDER: CvSection[] = [
  'SUMMARY',
  'EXPERIENCE',
  'PROJECTS',
  'EDUCATION',
  'SKILLS',
  'LANGUAGES',
  'CERTIFICATIONS',
  'LINKS',
]

export const SECTION_TITLES: Record<CvSection, string> = {
  SUMMARY: 'Summary',
  EXPERIENCE: 'Experience',
  PROJECTS: 'Projects',
  EDUCATION: 'Education',
  SKILLS: 'Skills',
  LANGUAGES: 'Languages',
  CERTIFICATIONS: 'Certifications',
  LINKS: 'Links',
}

export interface PersonalInfo {
  fullName?: string | null
  title?: string | null
  email?: string | null
  phone?: string | null
  location?: string | null
  website?: string | null
  linkedin?: string | null
  github?: string | null
  photoUrl?: string | null
}

export interface Experience {
  id?: string | null
  company?: string | null
  position?: string | null
  location?: string | null
  startDate?: string | null
  endDate?: string | null
  current?: boolean
  description?: string[]
}

export interface Education {
  id?: string | null
  institution?: string | null
  degree?: string | null
  field?: string | null
  location?: string | null
  startDate?: string | null
  endDate?: string | null
  description?: string | null
}

export interface Project {
  id?: string | null
  name?: string | null
  description?: string | null
  technologies?: string[]
  url?: string | null
}

export interface Skill {
  id?: string | null
  name?: string | null
  category?: string | null
}

export interface CvLanguage {
  id?: string | null
  name?: string | null
  level?: string | null
}

export interface Certification {
  id?: string | null
  name?: string | null
  issuer?: string | null
  date?: string | null
  url?: string | null
}

export interface CvLink {
  id?: string | null
  label?: string | null
  url?: string | null
}

export interface CustomSection {
  id?: string | null
  title?: string | null
  items?: string[]
}

export interface CvContent {
  personal?: PersonalInfo | null
  summary?: string | null
  experiences?: Experience[]
  education?: Education[]
  projects?: Project[]
  skills?: Skill[]
  languages?: CvLanguage[]
  certifications?: Certification[]
  links?: CvLink[]
  customSections?: CustomSection[]
}

export interface CvSummary {
  id: string
  name: string
  createdAt: string
  updatedAt: string
  version: number
}

export interface CvDetail extends CvSummary {
  content: CvContent
  sectionOrder: CvSection[]
}

export interface LanguageVersionSummary {
  id: string
  masterCvId: string
  languageCode: LanguageCode
  name: string
  templateCode: TemplateCode
  updatedAt: string
  version: number
}

export interface LanguageVersion extends LanguageVersionSummary {
  content: CvContent
  sectionOrder: CvSection[]
  createdAt: string
}

export interface Revision {
  id: string
  version: number
  content: CvContent
  sectionOrder: CvSection[]
  createdAt: string
}

export interface CoverLetterSummary {
  id: string
  title: string
  updatedAt: string
  version: number
}

export interface CoverLetter extends CoverLetterSummary {
  masterCvId: string
  languageCode: LanguageCode
  recipient?: string | null
  company?: string | null
  position?: string | null
  body: string
  createdAt: string
}

export interface ShareLink {
  id: string
  token: string
  expiresAt: string | null
  createdAt: string
}

export interface AtsCheck {
  code: string
  label: string
  passed: boolean
  detail: string
}

export interface AtsScore {
  score: number
  checks: AtsCheck[]
}

export interface Usage {
  plan: string
  aiCallsUsed: number
  aiCallsLimit: number
  cvsUsed: number
  cvsMax: number
  periodStart: string
}

export type AiOperation = 'TRANSLATE' | 'IMPROVE' | 'REWRITE' | 'TAILOR' | 'COVER_LETTER'
export type AiSuggestionStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED'

export interface AiSuggestionPayload {
  section: CvSection
  entityId: string | null
  field: string
  original: string | null
  suggested: string | null
}

export interface AiSuggestion {
  id: string
  operation: AiOperation
  status: AiSuggestionStatus
  payload: AiSuggestionPayload
  createdAt: string
}
