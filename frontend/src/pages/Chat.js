import React from 'react';
import toast from 'react-hot-toast';
import ConversationList from '../components/ConversationList';
import MessageComposer from '../components/MessageComposer';
import MessageThread from '../components/MessageThread';
import { useAuth } from '../contexts/AuthContext';
import { useChat } from '../hooks/useChat';
import { errorMessage } from '../api/client';

export default function Chat() {
  const { user, logout } = useAuth();
  const {
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
  } = useChat();

  const handleStart = async (username) => {
    try {
      const conversation = await startConversationWith(username);
      toast.success(`Chat with ${conversation.otherUsername} is ready`);
    } catch (error) {
      toast.error(errorMessage(error));
    }
  };

  const handleLogout = () => {
    logout();
    window.location.assign('/login');
  };

  return (
    <div className="chat-shell">
      <ConversationList
        conversations={conversations}
        active={active}
        currentUser={user}
        connected={connected}
        searchResults={searchResults}
        onSearch={search}
        onSelect={selectConversation}
        onStart={handleStart}
        onLogout={handleLogout}
      />

      <main className="chat-main">
        <header className="chat-header">
          <div>
            <h2>{active ? active.otherUsername : 'PulseChat'}</h2>
            <span>{active ? 'Direct message · end-to-end stored on server' : 'Real-time messaging'}</span>
          </div>
          <span className={`status-pill ${connected ? 'online' : 'offline'}`}>
            {connected ? 'Connected' : 'Connecting…'}
          </span>
        </header>

        {loading ? (
          <div className="thread-empty">Loading conversations…</div>
        ) : (
          <MessageThread active={active} messages={messages} currentUsername={user?.username} />
        )}

        <MessageComposer disabled={!active || !connected} onSend={sendMessage} />
      </main>
    </div>
  );
}
