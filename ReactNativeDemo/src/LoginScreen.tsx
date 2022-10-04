import {
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import React, {useState} from 'react';
import {useHookstate} from '@hookstate/core';
import GlobalLoginState from './State/LoginState';
import LoginState from './State/LoginState';
import { getAesKey, getApiTokens } from "./Networking/Client";

const LoginHeader = 'Login';
const ChannelPrompt = 'Enter a channel name';
const ButtonTitle = 'Join!';

const LoginScreen: React.FC = () => {
  // tODO: Indicate logging in
  const [loggingIn, setLoggingIn] = useState<boolean>(false);
  const [channelName, setChannelName] = useState<string>('TEST');
  const loginState = useHookstate<typeof LoginState>(GlobalLoginState);

  function updateChannelName(newName: string) {
    setChannelName(newName.toUpperCase().replace(/[\W_]+/g, ''));
  }
  async function logIn() {
    if (loggingIn) {
      return;
    }
    setLoggingIn(true);
    try {
      const [tokens, aesKey] =  [null, null];/* await Promise.all([
        getApiTokens(channelName),
        getAesKey(channelName),
      ]);*/
      // @ts-ignore
      loginState.set(_ => {
        return {loggedIn: true, aesKey: aesKey, tokens: tokens};
      });
    } catch (e) {
      console.error(`An Error logging in ${e}`);
    } finally {
      setLoggingIn(false);
    }
  }
  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        <Text style={styles.heading}>{LoginHeader}</Text>
        <TextInput
          onChangeText={updateChannelName}
          style={styles.input}
          value={channelName}
          placeholder={ChannelPrompt}
          clearButtonMode="always"
        />
        <TouchableOpacity onPress={logIn} style={styles.button} disabled={loggingIn}>
          <Text style={styles.buttonText}>{ButtonTitle}</Text>
        </TouchableOpacity>
      <Text>
        // TODO: Network Status
      </Text>
      </View>
      <View style={styles.bottomSpacer} />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 2,
  },
  container: {
    alignSelf: 'flex-start',
    justifyContent: 'space-evenly',
    paddingTop: 10,
    padding: 20,
    width: '100%',
    flex: 1,
  },
  bottomSpacer: {
    flex: 1,
  },
  heading: {
    fontSize: 24,
    fontWeight: '600',
  },
  input: {
    height: 40,
    borderWidth: 1,
    padding: 10,
  },
  button: {
    alignItems: 'center',
    backgroundColor: 'deepskyblue',
    padding: 20,
    borderRadius: 5,
    fontSize: 22,
  },
  buttonText: {
    fontSize: 22,
    color: 'white',
  },
  infoText: {
    fontSize: 16,
    fontWeight: '400',
  },
});

export default LoginScreen;
