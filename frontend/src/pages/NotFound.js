import React from 'react';
import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="center-screen">
      <div>
        <h1>404</h1>
        <p>That page does not exist.</p>
        <Link to="/chat" className="primary-button">Back to chat</Link>
      </div>
    </div>
  );
}
