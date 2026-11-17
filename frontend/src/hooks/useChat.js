import { useCallback, useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import toast from 'react-hot-toast';
import { errorMessage, getToken, resolveWebSocketUrl } from '../api/client';
import { searchUsers } from '../services/userService';
import {
  createConversation,
  getMessages,
  listConversations,
  markAsRead,
} from '../services/conversationService';

/**
 * Owns the STOMP connection and the chat state. Messages are only rendered after the server
 * persists them and echoes them back on /user/queue/messages, so the UI never shows a message
 * that failed to save.
 */
export function useChat() {
  const [connected, setConnected] = useState(false);
  const [conversations, setConversations] = useState([]);
  const [active, setActive] = useState(null);
  const [messages, setMessages] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(true);

  const clientRef = useRef(null);
  const activeIdRef = useRef(null);

  const refreshConversations = useCallback(async () => {
    try {
      setConversations(await listConversations());
    } catch (error) {
      toast.error(errorMessage(error));
    }
  }, []);

  useEffect(() => {
    refreshConversations().finally(() => setLoading(false));
  }, [refreshConversations]);

  const appendMessage = useCallback((message) => {
    if (!message || message.conversationId !== activeIdRef.current) return;
    setMessages((previous) =>
      previous.some((item) => item.id === message.id) ? previous : [...previous, message]
    );
  }, []);

  useEffect(() => {
    const token = getToken();
    if (!token) return undefined;

    const client = new Client({
      webSocketFactory: () => new WebSocket(resolveWebSocketUrl()),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 4000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/user/queue/messages', (frame) => {
          const message = JSON.parse(frame.body);
          appendMessage(message);
          setConversations((previous) =>
            previous
              .map((conversation) =>
                conversation.id === message.conversationId
                  ? {
                      ...conversation,
                      lastMessagePreview: message.content,
                      lastMessageAt: message.sentAt,
                      unreadCount:
                        conversation.id === activeIdRef.current
                          ? 0
                          : (conversation.unreadCount || 0) + 1,
                    }
                  : conversation
              )
              .sort((a, b) => new Date(b.lastMessageAt) - new Date(a.lastMessageAt))
          );
        });

        client.subscribe('/user/queue/read', (frame) => {
          const receipt = JSON.parse(frame.body);
          if (receipt.conversationId === activeIdRef.current) {
            setMessages((previous) =>
              previous.map((message) => (message.status === 'SENT' ? { ...message, status: 'READ' } : message))
            );
          }
        });

        client.subscribe('/user/queue/errors', (frame) => {
          try {
            toast.error(JSON.parse(frame.body).message);
          } catch {
            toast.error(frame.body);
          }
        });
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
      onStompError: (frame) => {
        setConnected(false);
        toast.error(`WebSocket error: ${frame.headers?.message || 'unknown'}`);
      },
    });

    clientRef.current = client;
    client.activate();

    return () => {
      client.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [appendMessage]);

  const selectConversation = useCallback(async (conversation) => {
    activeIdRef.current = conversation.id;
    setActive(conversation);
    setMessages([]);
    setConversations((previous) =>
      previous.map((item) => (item.id === conversation.id ? { ...item, unreadCount: 0 } : item))
    );

    try {
      const page = await getMessages(conversation.id);
      setMessages([...page.content].reverse());
      await markAsRead(conversation.id);
      if (clientRef.current?.connected) {
        clientRef.current.publish({
          destination: '/app/chat.read',
          body: JSON.stringify({ conversationId: conversation.id }),
        });
      }
    } catch (error) {
      toast.error(errorMessage(error));
    }
  }, []);

  const startConversationWith = useCallback(
    async (username) => {
      const conversation = await createConversation(username);
      await refreshConversations();
      setSearchResults([]);
      await selectConversation(conversation);
      return conversation;
    },
    [refreshConversations, selectConversation]
  );

  const search = useCallback(async (query) => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }
    try {
      setSearchResults(await searchUsers(query.trim()));
    } catch (error) {
      toast.error(errorMessage(error));
    }
  }, []);

  const sendMessage = useCallback((content) => {
    const text = content.trim();
    if (!text || !activeIdRef.current) return;
    if (!clientRef.current?.connected) {
      toast.error('Connecting to the chat server…');
      return;
    }
    clientRef.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ conversationId: activeIdRef.current, content: text }),
    });
  }, []);

  return {
    connected,
    conversations,
    active,
    messages,
    searchResults,
    loading,
    selectConversation,
    startConversationWith,
    search,
    sendMessage,
  };
}
