import React from 'react';

function Success() {
  return (
    <div className="page">
      <div data-test-id="success-state" className="card">
        <h2>Payment Successful!</h2>
        <p data-test-id="success-message">Your payment has been processed successfully</p>
      </div>
    </div>
  );
}

export default Success;
