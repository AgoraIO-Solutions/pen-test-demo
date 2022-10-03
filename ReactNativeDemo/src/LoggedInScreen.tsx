import React from 'react';
import {Button, SafeAreaView, Text} from 'react-native';
import {logOut} from './State/LoginState';

const LoggedInScreen = () => {
  return (
    <SafeAreaView>
      <Text>Hello Log In</Text>
      <Button title="Logout" onPress={logOut} />
    </SafeAreaView>
  );
};

export default LoggedInScreen;
