import React from 'react';
import { Button, SafeAreaView, Text, View } from "react-native";
import {useHookstate} from '@hookstate/core';
import LoginState from './State/LoginState';
import GlobalLoginState from './State/LoginState';

const SettingsScreen: React.FC = () => {
  const loginState = useHookstate<typeof LoginState>(GlobalLoginState);
  function logOut() {
    // @ts-ignore
    loginState.set(() => ({loggedIn: false, aesKey: null, tokens: null}));
  }

  return (
    <SafeAreaView>
      <View style={{paddingVertical: "50%"}}>
        <Button  title="Logout" onPress={logOut} />
      </View>
    </SafeAreaView>
  );
};

export default SettingsScreen;
