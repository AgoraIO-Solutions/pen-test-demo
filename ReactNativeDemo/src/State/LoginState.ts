import {hookstate} from '@hookstate/core';
import {AesKey, Tokens} from '../Networking/Client';

type LoginState = {
  loggedIn: boolean;
  aesKey?: AesKey;
  tokens?: Tokens;
};

const defaultLoginState: LoginState = {
  loggedIn: false,
};

const GlobalLoginState = hookstate(defaultLoginState);

export default GlobalLoginState;

export const logIn = () => {
  GlobalLoginState.loggedIn.set(true);
};

export const logOut = () => {
  GlobalLoginState.loggedIn.set(false);
};
