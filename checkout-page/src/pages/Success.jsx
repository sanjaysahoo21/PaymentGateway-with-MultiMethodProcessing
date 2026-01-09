import React from 'react';

function Success() {
  return (
    <div data-test-id="success-state" className="page">
      <h2>Payment Successful!</h2>
      <p data-test-id="success-message">Your payment has been processed successfully</p>
    </div>
  );
}

export default Success;
