import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {NavigationContainer} from '@react-navigation/native';
import LoginScreen from './LoginScreen';
import MainTabNavigator from './MainTabNavigator';
import {useHookstate} from '@hookstate/core';
import GlobalLoginState from './State/LoginState';

const Stack = createNativeStackNavigator();

const Navigator: React.FC = () => {
  const loginState = useHookstate(GlobalLoginState);
  return (
    <NavigationContainer>
      {!loginState.loggedIn.get() ? (
        <Stack.Navigator>
          <Stack.Screen name="Login" component={LoginScreen} />
        </Stack.Navigator>
      ) : (
        <MainTabNavigator />
      )}
    </NavigationContainer>
  );
};

export default Navigator;
