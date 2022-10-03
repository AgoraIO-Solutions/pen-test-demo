import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {NavigationContainer} from '@react-navigation/native';
import LoginScreen from './LoginScreen';
import LoggedInScreen from './LoggedInScreen';
import {useHookstate} from '@hookstate/core';
import GlobalLoginState from './State/LoginState';

const Stack = createNativeStackNavigator();

const Navigator = () => {
  const loginState = useHookstate(GlobalLoginState);
  return (
    <NavigationContainer>
      <Stack.Navigator>
        {!loginState.loggedIn.get() ? (
          <Stack.Screen name="Login" component={LoginScreen} />
        ) : (
          <Stack.Screen name="SignedIn" component={LoggedInScreen} />
        )}
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default Navigator;
