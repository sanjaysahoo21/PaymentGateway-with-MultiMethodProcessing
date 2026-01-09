import React from 'react';

function Failure() {
  return (
    <div data-test-id="error-state" className="page">
      <h2>Payment Failed</h2>
      <p data-test-id="error-message">Payment could not be processed</p>
      <button data-test-id="retry-button" onClick={() => window.history.back()}>Try Again</button>
    </div>
  );
}

export default Failure;
