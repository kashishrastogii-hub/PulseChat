import { apiClient } from '../api/client';

export async function listConversations() {
  const { data } = await apiClient.get('/conversations');
  return data.data;
}

export async function createConversation(participantUsername) {
  const { data } = await apiClient.post('/conversations', { participantUsername });
  return data.data;
}

export async function getMessages(conversationId, page = 0, size = 50) {
  const { data } = await apiClient.get(`/conversations/${conversationId}/messages`, {
    params: { page, size },
  });
  return data.data;
}

export async function markAsRead(conversationId) {
  await apiClient.post(`/conversations/${conversationId}/read`);
}
