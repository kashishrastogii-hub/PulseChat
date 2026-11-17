import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { errorMessage } from '../api/client';
import { useAuth } from '../contexts/AuthContext';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm();

  const onSubmit = async (values) => {
    setSubmitting(true);
    try {
      await login(values);
      toast.success('Welcome back!');
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
        <h1>Welcome back</h1>
        <p className="auth-subtitle">Sign in to continue chatting.</p>

        <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
          <label>
            Username or email
            <input
              {...register('identifier', { required: 'Enter your username or email' })}
              placeholder="alice"
              autoComplete="username"
            />
            {errors.identifier && <small className="field-error">{errors.identifier.message}</small>}
          </label>

          <label>
            Password
            <input
              type="password"
              {...register('password', { required: 'Enter your password' })}
              placeholder="••••••••"
              autoComplete="current-password"
            />
            {errors.password && <small className="field-error">{errors.password.message}</small>}
          </label>

          <button type="submit" className="primary-button" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <p className="auth-switch">
          New here? <Link to="/register">Create an account</Link>
        </p>
      </div>
    </div>
  );
}
