import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { errorMessage } from '../api/client';
import { useAuth } from '../contexts/AuthContext';

export default function Register() {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const { register, handleSubmit, watch, formState: { errors } } = useForm();
  const password = watch('password');

  const onSubmit = async (values) => {
    setSubmitting(true);
    try {
      await registerUser(values);
      toast.success('Account created');
      navigate('/chat', { replace: true });
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="brand-mark brand-mark-large">P</div>
        <h1>Create your account</h1>
        <p className="auth-subtitle">A username is all your friends need to find you.</p>

        <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
          <label>
            Username
            <input
              {...register('username', {
                required: 'Choose a username',
                minLength: { value: 3, message: 'At least 3 characters' },
                pattern: { value: /^[a-zA-Z0-9_]+$/, message: 'Letters, numbers and underscores only' },
              })}
              placeholder="alice"
              autoComplete="username"
            />
            {errors.username && <small className="field-error">{errors.username.message}</small>}
          </label>

          <label>
            Email
            <input
              type="email"
              {...register('email', { required: 'Enter your email' })}
              placeholder="alice@example.com"
              autoComplete="email"
            />
            {errors.email && <small className="field-error">{errors.email.message}</small>}
          </label>

          <label>
            Password
            <input
              type="password"
              {...register('password', {
                required: 'Choose a password',
                minLength: { value: 6, message: 'At least 6 characters' },
              })}
              placeholder="••••••••"
              autoComplete="new-password"
            />
            {errors.password && <small className="field-error">{errors.password.message}</small>}
          </label>

          <label>
            Confirm password
            <input
              type="password"
              {...register('confirmPassword', {
                required: 'Confirm your password',
                validate: (value) => value === password || 'Passwords do not match',
              })}
              placeholder="••••••••"
              autoComplete="new-password"
            />
            {errors.confirmPassword && <small className="field-error">{errors.confirmPassword.message}</small>}
          </label>

          <button type="submit" className="primary-button" disabled={submitting}>
            {submitting ? 'Creating…' : 'Create account'}
          </button>
        </form>

        <p className="auth-switch">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
