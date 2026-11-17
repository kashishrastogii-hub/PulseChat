import { apiClient, setToken } from '../api/client';

export async function signUp({ username, email, password }) {
  const { data } = await apiClient.post('/auth/signup', { username, email, password });
  setToken(data.data.token);
  return data.data;
}

export async function signIn({ identifier, password }) {
  const { data } = await apiClient.post('/auth/signin', { identifier, password });
  setToken(data.data.token);
  return data.data;
}

export function signOut() {
  setToken(null);
}
