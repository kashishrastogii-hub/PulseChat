import { apiClient } from '../api/client';

export async function getCurrentUser() {
  const { data } = await apiClient.get('/users/me');
  return data.data;
}

export async function searchUsers(query) {
  const { data } = await apiClient.get('/users/search', { params: { query } });
  return data.data;
}
