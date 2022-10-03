import {AWS_APP_KEY, AWS_API_Base} from '@env';

const URL = `https://${AWS_API_Base}`;

const options: RequestInit = {
  method: 'GET',
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    'X-API-KEY': AWS_APP_KEY,
  },
};

async function getApiTokens(channelName: string): Promise<Tokens> {
  const response = await fetch(
    `${URL}/dev/api/pen_test_token?channel=${channelName}`,
    options,
  );
  const {data, errors} = await response.json();
  if (response.ok) {
    return data;
  } else {
    throw errors;
  }
}

async function getAesKey(channelName: string) {}

export interface AesKey {
  key: string;
}

export interface Tokens {
  uid: number;
  rtmuid: string;
  rtc: string;
  rtm: string;
}
