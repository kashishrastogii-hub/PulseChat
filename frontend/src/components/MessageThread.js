import React, { useEffect, useRef } from 'react';
import { CheckCheck, MessageCircle } from 'lucide-react';
import { formatTime, initials } from '../utils/format';

export default function MessageThread({ active, messages, currentUsername }) {
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages.length, active?.id]);

  if (!active) {
    return (
      <div className="thread-empty">
        <MessageCircle size={48} strokeWidth={1.4} />
        <h2>Select a conversation</h2>
        <p>Choose someone from the list, or search for a username to start chatting.</p>
      </div>
    );
  }

  return (
    <div className="thread">
      <div className="thread-messages">
        {messages.length === 0 && <p className="empty-hint centered">No messages yet — send the first one.</p>}
        {messages.map((message) => {
          const mine = message.senderUsername === currentUsername;
          return (
            <div key={message.id} className={`message-row ${mine ? 'mine' : 'theirs'}`}>
              {!mine && <span className="avatar avatar-small">{initials(message.senderUsername)}</span>}
              <div className="message-body">
                <div className="bubble">{message.content}</div>
                <div className="message-meta">
                  <span>{formatTime(message.sentAt)}</span>
                  {mine && message.status === 'READ' && <CheckCheck size={13} className="read-icon" />}
                </div>
              </div>
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>
    </div>
  );
}
