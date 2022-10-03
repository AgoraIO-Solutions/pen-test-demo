import React from 'react';
import {Button, SafeAreaView, Text} from 'react-native';
import {useHookstate} from '@hookstate/core';
import LoginState from './State/LoginState';
import GlobalLoginState from './State/LoginState';

const LoggedInScreen: React.FC = () => {
  const loginState = useHookstate<typeof LoginState>(GlobalLoginState);
  function logOut() {
    // @ts-ignore
    loginState.set(() => ({loggedIn: false, aesKey: null, tokens: null}));
  }

  return (
    <SafeAreaView>
      <Text>Hello Log In</Text>
      <Button title="Logout" onPress={logOut} />
    </SafeAreaView>
  );
};

export default LoggedInScreen;
