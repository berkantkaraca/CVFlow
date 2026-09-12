import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { z } from 'zod'
import { Field, PrimaryButton, TextInput } from '../components/ui'
import { useLogin } from '../features/auth/authApi'
import { toastFromError, useUiStore } from '../stores/uiStore'

const schema = z.object({
  email: z.string().trim().min(1, 'Email is required').email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
})
type Form = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const notify = useUiStore((s) => s.notify)
  const login = useLogin()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Form>()

  async function onSubmit(data: Form) {
    const parsed = schema.safeParse(data)
    if (!parsed.success) return
    try {
      await login.mutateAsync(parsed.data)
      notify('Welcome back', 'success')
      navigate('/')
    } catch (e) {
      notify(toastFromError(e, 'Login failed'), 'error')
    }
  }

  return (
    <div className="mx-auto mt-16 w-full max-w-md rounded-lg border border-slate-200 bg-white p-6">
      <h1 className="mb-1 text-xl font-bold">Log in to CVFlow</h1>
      <p className="mb-4 text-sm text-slate-500">
        No account yet?{' '}
        <Link to="/register" className="text-blue-700 hover:underline">
          Register
        </Link>
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <Field label="Email">
          <TextInput {...register('email')} type="email" autoComplete="email" autoFocus />
        </Field>
        {errors.email && <p className="text-sm text-red-600">{errors.email.message}</p>}
        <Field label="Password">
          <TextInput {...register('password')} type="password" autoComplete="current-password" />
        </Field>
        {errors.password && <p className="text-sm text-red-600">{errors.password.message}</p>}
        <PrimaryButton type="submit" disabled={login.isPending}>
          {login.isPending ? 'Logging in…' : 'Log in'}
        </PrimaryButton>
      </form>
    </div>
  )
}
