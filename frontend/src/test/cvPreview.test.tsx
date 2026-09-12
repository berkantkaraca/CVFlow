import { cleanup, render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { CvPreview } from '../components/CvPreview'
import type { CvContent } from '../types/cv'

const content: CvContent = {
  personal: { fullName: 'Jane Doe', title: 'Backend Developer' },
  summary: 'Experienced developer',
  experiences: [],
  education: [],
  projects: [],
  skills: [],
  languages: [],
  certifications: [],
  links: [],
}

describe('CvPreview', () => {
  it('renders header and sections in the given order', () => {
    render(
      <CvPreview content={content} sectionOrder={['EXPERIENCE', 'SUMMARY']} template="MODERN" language="EN" />,
    )
    expect(screen.getByText('Jane Doe')).toBeInTheDocument()
    expect(screen.getByText('Experienced developer')).toBeInTheDocument()
    expect(screen.getByText('Summary')).toBeInTheDocument()
  })

  it('localizes section titles', () => {
    render(
      <CvPreview content={content} sectionOrder={['SUMMARY']} template="MINIMAL" language="TR" />,
    )
    expect(screen.getByText('Özet')).toBeInTheDocument()
    cleanup()
    render(<CvPreview content={content} sectionOrder={['SUMMARY']} template="MINIMAL" language="EN" />)
    expect(screen.getByText('Summary')).toBeInTheDocument()
  })
})
