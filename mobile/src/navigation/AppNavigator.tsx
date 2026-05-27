import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { useSelector } from 'react-redux';
import { RootState } from '../store';

// Screens
import LoginScreen from '../screens/LoginScreen';
import OtpScreen from '../screens/OtpScreen';
import LanguageScreen from '../screens/LanguageScreen';
import HomeScreen from '../screens/HomeScreen';

// Types: define the params each screen expects
// (like DTOs in Spring — they define the shape of data passed between screens)
export type AuthStackParamList = {
  Login: undefined;
  Otp: { mobile: string };
  Language: { mobile: string; token: string; farmerId: string };
};

export type AppTabParamList = {
  Home: undefined;
};

const AuthStack = createStackNavigator<AuthStackParamList>();
const AppTab = createBottomTabNavigator<AppTabParamList>();

/**
 * Auth flow: Login → OTP → Language → Main App
 * Similar to a Spring Security filter chain — user must pass through
 * auth before reaching protected screens.
 */
function AuthNavigator() {
  return (
    <AuthStack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#2E7D32' },
        headerTintColor: '#fff',
        headerTitleStyle: { fontWeight: 'bold' },
      }}
    >
      <AuthStack.Screen
        name="Login"
        component={LoginScreen}
        options={{ title: '🌾 KrishiMitra' }}
      />
      <AuthStack.Screen
        name="Otp"
        component={OtpScreen}
        options={{ title: 'OTP Verify' }}
      />
      <AuthStack.Screen
        name="Language"
        component={LanguageScreen}
        options={{ title: 'Choose Language' }}
      />
    </AuthStack.Navigator>
  );
}

/**
 * Main app tabs — what the farmer sees after logging in.
 */
function AppNavigator() {
  return (
    <AppTab.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#2E7D32' },
        headerTintColor: '#fff',
        tabBarActiveTintColor: '#2E7D32',
      }}
    >
      <AppTab.Screen
        name="Home"
        component={HomeScreen}
        options={{ title: '🌾 KrishiMitra' }}
      />
    </AppTab.Navigator>
  );
}

/**
 * Root navigator: switches between Auth flow and App flow
 * based on Redux isAuthenticated state
 * (like a Spring Security role-based redirect)
 */
export default function RootNavigator() {
  const isAuthenticated = useSelector(
    (state: RootState) => state.farmer.isAuthenticated
  );

  return (
    <NavigationContainer>
      {isAuthenticated ? <AppNavigator /> : <AuthNavigator />}
    </NavigationContainer>
  );
}