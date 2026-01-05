import axios from 'axios';

// API base URL from environment or default to localhost
const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8000';

/**
 * Create an authenticated Axios client with merchant credentials.
 * Adds X-Api-Key and X-Api-Secret headers for API authentication.
 * 
 * @param {Object} auth - Merchant authentication object
 * @param {string} auth.apiKey - Merchant's API key
 * @param {string} auth.apiSecret - Merchant's API secret
 * @returns {Object} Configured Axios client instance
 */
export function createClient(auth) {
  const headers = {};
  // Add merchant credentials to request headers if available
  if (auth?.apiKey && auth?.apiSecret) {
    headers['X-Api-Key'] = auth.apiKey;
    headers['X-Api-Secret'] = auth.apiSecret;
  }
  return axios.create({
    baseURL,
    headers
  });
}

/**
 * Fetch test merchant credentials from the backend.
 * Used during login to populate API key and secret for demo testing.
 * 
 * @returns {Promise<Object>} Test merchant object with email, api_key, api_secret
 */
export async function fetchTestMerchant() {
  const client = axios.create({ baseURL });
  const res = await client.get('/api/v1/test/merchant');
  return res.data;
}
