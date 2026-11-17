import React, { useState } from 'react';
import { Send } from 'lucide-react';

export default function MessageComposer({ disabled, onSend }) {
  const [value, setValue] = useState('');

  const submit = (event) => {
    event.preventDefault();
    if (!value.trim() || disabled) return;
    onSend(value);
    setValue('');
  };

  return (
    <form className="composer" onSubmit={submit}>
      <input
        value={value}
        onChange={(event) => setValue(event.target.value)}
        placeholder={disabled ? 'Select a conversation' : 'Write a message…'}
        disabled={disabled}
        aria-label="Message"
        autoComplete="off"
      />
      <button type="submit" disabled={disabled || !value.trim()} aria-label="Send message">
        <Send size={18} />
      </button>
    </form>
  );
}
