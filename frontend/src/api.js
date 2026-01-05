import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8000';

export function createClient(auth) {
  const headers = {};
  if (auth?.apiKey && auth?.apiSecret) {
    headers['X-Api-Key'] = auth.apiKey;
    headers['X-Api-Secret'] = auth.apiSecret;
  }
  return axios.create({
    baseURL,
    headers
  });
}

export async function fetchTestMerchant() {
  const client = axios.create({ baseURL });
  const res = await client.get('/api/v1/test/merchant');
  return res.data;
}
