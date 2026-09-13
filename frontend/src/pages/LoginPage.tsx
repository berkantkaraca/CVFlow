import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { z } from 'zod'
import { Field, PrimaryButton, TextInput } from '../components/ui'
import { useLogin } from '../features/auth/authApi'
import { useT } from '../i18n'
import { toastFromError, useUiStore } from '../stores/uiStore'

const schema = z.object({
  email: z.string().trim().min(1).email(),
  password: z.string().min(1),
})
type Form = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const notify = useUiStore((s) => s.notify)
  const t = useT()
  const login = useLogin()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Form>()

  async function onSubmit(data: Form) {
    const parsed = schema.safeParse(data)
    if (!parsed.success) {
      if (!data.email.trim()) notify(t('auth.emailRequired'), 'error')
      else notify(t('auth.emailInvalid'), 'error')
      return
    }
    try {
      await login.mutateAsync(parsed.data)
      notify(t('auth.welcomeBack'), 'success')
      navigate('/')
    } catch (e) {
      notify(toastFromError(e, t('error.generic')), 'error')
    }
  }

  return (
    <div className="mx-auto mt-16 w-full max-w-md rounded-2xl border border-line bg-card p-8 shadow-[0_8px_30px_rgba(20,32,46,0.08)]">
      <h1 className="mb-1 text-xl font-bold tracking-tight">{t('auth.loginTitle')}</h1>
      <p className="mb-4 text-sm text-ink-500">
        {t('auth.noAccount')}{' '}
        <Link to="/register" className="text-accent-800 hover:underline">
          {t('auth.registerLink')}
        </Link>
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <Field label={t('auth.email')}>
          <TextInput {...register('email')} type="email" autoComplete="email" autoFocus />
        </Field>
        {errors.email && <p className="text-sm text-red-600">{t('auth.emailInvalid')}</p>}
        <Field label={t('auth.password')}>
          <TextInput {...register('password')} type="password" autoComplete="current-password" />
        </Field>
        {errors.password && <p className="text-sm text-red-600">{t('auth.passwordRequired')}</p>}
        <PrimaryButton type="submit" disabled={login.isPending}>
          {login.isPending ? t('auth.loggingIn') : t('auth.loginBtn')}
        </PrimaryButton>
      </form>
    </div>
  )
}
