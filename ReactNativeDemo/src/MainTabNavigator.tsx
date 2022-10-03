import React from 'react';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import RTCScreen from './RTCScreen';
import SettingScreen from './SettingScreen';
import RTMScreen from './RTMScreen';

const Tab = createBottomTabNavigator();

const MainTabNavigator: React.FC = () => {
  return (
    <Tab.Navigator>
      <Tab.Screen name="RTC" component={RTCScreen} />
      <Tab.Screen name="RTM" component={RTMScreen} />
      <Tab.Screen name="Settings" component={SettingScreen} />
    </Tab.Navigator>
  );
};

export default MainTabNavigator;
