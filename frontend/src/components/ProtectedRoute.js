import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export default function ProtectedRoute() {
  const { token, loading } = useAuth();

  if (loading) {
    return <div className="center-screen">Loading PulseChat…</div>;
  }

  return token ? <Outlet /> : <Navigate to="/login" replace />;
}
