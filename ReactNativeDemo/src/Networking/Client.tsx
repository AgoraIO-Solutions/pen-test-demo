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

async function getApiTokens(channelName: string): Tokens {
  try {
    const response = await fetch(`${URL}/dev/api/pen_test_token?channel=${channelName}`, options);
    const newTokens = new Tokens();
    newTokens.copyInto(response.json());
    return response;
  } catch (e) {
    console.error(`Error ${e}`);
  }
}

async function getAesKey(channelName: string) {

}

export type AesKey = {
  key: string;
};

export type Tokens = {
  uid: number;
  rtmuid: string;
  rtc: string;
  rtm: string;
};
