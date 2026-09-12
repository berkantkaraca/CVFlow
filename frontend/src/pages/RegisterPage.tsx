import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { z } from 'zod'
import { Field, PrimaryButton, TextInput } from '../components/ui'
import { useRegister } from '../features/auth/authApi'
import { toastFromError, useUiStore } from '../stores/uiStore'

const schema = z.object({
  email: z.string().trim().min(1, 'Email is required').email('Enter a valid email').max(255),
  password: z.string().min(8, 'Password must be at least 8 characters').max(100),
})
type Form = z.infer<typeof schema>

export function RegisterPage() {
  const navigate = useNavigate()
  const notify = useUiStore((s) => s.notify)
  const registerMutation = useRegister()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Form>()

  async function onSubmit(data: Form) {
    const parsed = schema.safeParse(data)
    if (!parsed.success) return
    try {
      await registerMutation.mutateAsync(parsed.data)
      notify('Account created — welcome', 'success')
      navigate('/')
    } catch (e) {
      notify(toastFromError(e, 'Registration failed'), 'error')
    }
  }

  return (
    <div className="mx-auto mt-16 w-full max-w-md rounded-lg border border-slate-200 bg-white p-6">
      <h1 className="mb-1 text-xl font-bold">Create your CVFlow account</h1>
      <p className="mb-4 text-sm text-slate-500">
        Already registered?{' '}
        <Link to="/login" className="text-blue-700 hover:underline">
          Log in
        </Link>
      </p>
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <Field label="Email">
          <TextInput {...register('email')} type="email" autoComplete="email" autoFocus />
        </Field>
        {errors.email && <p className="text-sm text-red-600">{errors.email.message}</p>}
        <Field label="Password (min 8 characters)">
          <TextInput {...register('password')} type="password" autoComplete="new-password" />
        </Field>
        {errors.password && <p className="text-sm text-red-600">{errors.password.message}</p>}
        <PrimaryButton type="submit" disabled={registerMutation.isPending}>
          {registerMutation.isPending ? 'Creating…' : 'Register'}
        </PrimaryButton>
      </form>
    </div>
  )
}
