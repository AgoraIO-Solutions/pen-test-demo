import {AWS_APP_KEY, AWS_API_BASE} from '@env';

const URL = `https://${AWS_API_BASE}`;

const options: RequestInit = {
  mode: 'no-cors',
  method: 'GET',
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    'x-api-key': AWS_APP_KEY,
  },
};

export async function getApiTokens(channelName: string): Promise<Tokens> {
  const url = `${URL}/dev/api/pen_test_token?channel=${channelName}`;
  const response = await fetch(url, options);
  return await response.json();
}

export async function getAesKey(channelName: string): Promise<AesKey> {
  const url = `${URL}/dev/api/pen_test_aes_key?channel=${channelName}`;
  const response = await fetch(url, options);
  return await response.json();
}

export interface AesKey {
  key: string;
}

export interface Tokens {
  uid: number;
  rtmuid: string;
  rtc: string;
  rtm: string;
}
