import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { z } from 'zod'
import { Field, PrimaryButton, TextInput } from '../components/ui'
import { useRegister } from '../features/auth/authApi'
import { useT } from '../i18n'
import { toastFromError, useUiStore } from '../stores/uiStore'

const schema = z.object({
  email: z.string().trim().min(1).email().max(255),
  password: z.string().min(8).max(100),
})
type Form = z.infer<typeof schema>

export function RegisterPage() {
  const navigate = useNavigate()
  const notify = useUiStore((s) => s.notify)
  const t = useT()
  const registerMutation = useRegister()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Form>()

  async function onSubmit(data: Form) {
    const parsed = schema.safeParse(data)
    if (!parsed.success) {
      const emailIssue = parsed.error.issues.find((i) => i.path[0] === 'email')
      notify(emailIssue ? t('auth.emailInvalid') : t('auth.passwordShort'), 'error')
      return
    }
    try {
      await registerMutation.mutateAsync(parsed.data)
      notify(t('auth.welcomeNew'), 'success')
      navigate('/')
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  return (
    <div className="mx-auto mt-16 w-full max-w-md rounded-lg border border-slate-200 bg-white p-6">
      <h1 className="mb-1 text-xl font-bold">{t('auth.registerTitle')}</h1>
      <p className="mb-4 text-sm text-slate-500">
        {t('auth.hasAccount')}{' '}
        <Link to="/login" className="text-blue-700 hover:underline">
          {t('auth.loginLink')}
        </Link>
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <Field label={t('auth.email')}>
          <TextInput {...register('email')} type="email" autoComplete="email" autoFocus />
        </Field>
        {errors.email && <p className="text-sm text-red-600">{t('auth.emailInvalid')}</p>}
        <Field label={t('auth.passwordMin')}>
          <TextInput {...register('password')} type="password" autoComplete="new-password" />
        </Field>
        {errors.password && <p className="text-sm text-red-600">{t('auth.passwordShort')}</p>}
        <PrimaryButton type="submit" disabled={registerMutation.isPending}>
          {registerMutation.isPending ? t('auth.registering') : t('auth.registerBtn')}
        </PrimaryButton>
      </form>
    </div>
  )
}
