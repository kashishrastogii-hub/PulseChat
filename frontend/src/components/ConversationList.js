import React, { useEffect, useState } from 'react';
import { LogOut, Search, Wifi, WifiOff } from 'lucide-react';
import { formatListTime, initials } from '../utils/format';

export default function ConversationList({
  conversations,
  active,
  currentUser,
  connected,
  searchResults,
  onSearch,
  onSelect,
  onStart,
  onLogout,
}) {
  const [query, setQuery] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => onSearch(query), 250);
    return () => clearTimeout(timer);
  }, [query, onSearch]);

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="brand-mark">P</span>
        <div>
          <h1>PulseChat</h1>
          <span className={`connection ${connected ? 'online' : 'offline'}`}>
            {connected ? <Wifi size={12} /> : <WifiOff size={12} />}
            {connected ? 'Live' : 'Connecting…'}
          </span>
        </div>
      </div>

      <div className="sidebar-search">
        <Search size={16} />
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Find a user to chat with"
          aria-label="Find a user"
        />
      </div>

      {searchResults.length > 0 && (
        <div className="search-results">
          <p className="section-label">People</p>
          {searchResults.map((user) => (
            <button key={user.id} className="person-row" onClick={() => onStart(user.username)}>
              <span className="avatar avatar-small">{initials(user.username)}</span>
              <span className="person-name">{user.username}</span>
              <span className="person-action">Chat</span>
            </button>
          ))}
        </div>
      )}

      <div className="conversation-list">
        <p className="section-label">Conversations</p>
        {conversations.length === 0 && (
          <p className="empty-hint">No conversations yet. Search for a username above to start one.</p>
        )}
        {conversations.map((conversation) => (
          <button
            key={conversation.id}
            className={`conversation-row ${active?.id === conversation.id ? 'selected' : ''}`}
            onClick={() => onSelect(conversation)}
          >
            <span className="avatar">{initials(conversation.otherUsername)}</span>
            <span className="conversation-copy">
              <span className="conversation-top">
                <strong>{conversation.otherUsername}</strong>
                <small>{formatListTime(conversation.lastMessageAt)}</small>
              </span>
              <span className="conversation-bottom">
                <span className="preview">{conversation.lastMessagePreview || 'Say hello 👋'}</span>
                {conversation.unreadCount > 0 && (
                  <span className="unread">{conversation.unreadCount}</span>
                )}
              </span>
            </span>
          </button>
        ))}
      </div>

      {currentUser && (
        <div className="sidebar-footer">
          <div className="current-user">
            <span className="avatar avatar-small">{initials(currentUser.username)}</span>
            <div>
              <strong>{currentUser.username}</strong>
              <small>{currentUser.email}</small>
            </div>
          </div>
          <button className="icon-button" onClick={onLogout} title="Sign out" aria-label="Sign out">
            <LogOut size={18} />
          </button>
        </div>
      )}
    </aside>
  );
}
