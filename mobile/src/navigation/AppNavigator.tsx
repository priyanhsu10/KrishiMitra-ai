import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { Text, View, StyleSheet } from 'react-native';

// Auth screens
import LoginScreen from '../screens/LoginScreen';
import OtpScreen from '../screens/OtpScreen';
import LanguageScreen from '../screens/LanguageScreen';

// Main app screens
import HomeScreen from '../screens/HomeScreen';
import DiseaseDetectionScreen from '../screens/DiseaseDetectionScreen';
import AdvisoryChatScreen from '../screens/AdvisoryChatScreen';
import WeatherScreen from '../screens/WeatherScreen';
import NotificationsScreen from '../screens/NotificationsScreen';
import MandiScreen from '../screens/MandiScreen';
import AddFarmScreen from '../screens/AddFarmScreen';
import AddCropScreen from '../screens/AddCropScreen';

// ── Type definitions ──────────────────────────────────────────────────────

export type AuthStackParamList = {
  Login: undefined;
  Otp: { mobile: string };
  Language: { mobile: string; token: string; farmerId: string };
};

// Tab-level params (for direct tab navigation)
export type HomeStackParamList = {
  HomeMain: undefined;
  Disease: undefined;
  Advisory: undefined;
  Weather: undefined;
  Mandi: undefined;
  Notifications: undefined;
  AddFarm: undefined;
  AddCrop: undefined;
};

// ── Simple tab icon ─────────────────────────────────────────────────────

function TabIcon({ emoji, focused }: { emoji: string; focused: boolean }) {
  return (
    <View style={[tabStyles.iconWrap, focused && tabStyles.iconWrapActive]}>
      <Text style={tabStyles.emoji}>{emoji}</Text>
    </View>
  );
}

const tabStyles = StyleSheet.create({
  iconWrap: { width: 36, height: 36, borderRadius: 18, justifyContent: 'center', alignItems: 'center' },
  iconWrapActive: { backgroundColor: '#E8F5E9' },
  emoji: { fontSize: 20 },
});

// ── Auth navigator ────────────────────────────────────────────────────────

const AuthStack = createStackNavigator<AuthStackParamList>();

function AuthNavigator() {
  return (
    <AuthStack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#2E7D32' },
        headerTintColor: '#fff',
        headerTitleStyle: { fontWeight: 'bold' },
      }}
    >
      <AuthStack.Screen name="Login" component={LoginScreen} options={{ title: '🌾 KrishiMitra' }} />
      <AuthStack.Screen name="Otp" component={OtpScreen} options={{ title: 'OTP Verify' }} />
      <AuthStack.Screen name="Language" component={LanguageScreen} options={{ title: 'Choose Language' }} />
    </AuthStack.Navigator>
  );
}

// ── Home stack with header ────────────────────────────────────────────────

const HomeStack = createStackNavigator<HomeStackParamList>();

function HomeStackNavigator() {
  return (
    <HomeStack.Navigator
      screenOptions={{
        headerStyle: { backgroundColor: '#2E7D32' },
        headerTintColor: '#fff',
        headerTitleStyle: { fontWeight: 'bold' },
      }}
    >
      <HomeStack.Screen name="HomeMain" component={HomeScreen} options={{ title: '🌾 KrishiMitra', headerShown: false }} />
      <HomeStack.Screen name="Disease" component={DiseaseDetectionScreen} options={{ title: '🩺 Disease Detection' }} />
      <HomeStack.Screen name="Advisory" component={AdvisoryChatScreen} options={{ title: '💬 AI Advisory' }} />
      <HomeStack.Screen name="Weather" component={WeatherScreen} options={{ title: '🌤️ Weather' }} />
      <HomeStack.Screen name="Mandi" component={MandiScreen} options={{ title: '💰 Mandi Prices' }} />
      <HomeStack.Screen name="Notifications" component={NotificationsScreen} options={{ title: '🔔 Notifications' }} />
      <HomeStack.Screen name="AddFarm" component={AddFarmScreen} options={{ title: '🏡 Add Farm' }} />
      <HomeStack.Screen name="AddCrop" component={AddCropScreen} options={{ title: '🌱 Add Crop' }} />
    </HomeStack.Navigator>
  );
}

// ── Main tab navigator ────────────────────────────────────────────────────

const Tab = createBottomTabNavigator();

function AppNavigator() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: '#2E7D32',
        tabBarInactiveTintColor: '#999',
        tabBarStyle: {
          backgroundColor: '#fff',
          borderTopWidth: 1,
          borderTopColor: '#E0E0E0',
          paddingBottom: 6,
          paddingTop: 6,
          height: 60,
        },
        tabBarLabelStyle: { fontSize: 11, fontWeight: '600' },
      }}
    >
      <Tab.Screen
        name="Home"
        component={HomeStackNavigator}
        options={{
          title: 'Home',
          tabBarIcon: ({ focused }) => <TabIcon emoji="🏠" focused={focused} />,
        }}
      />
      <Tab.Screen
        name="DiseaseTab"
        component={DiseaseDetectionScreen}
        options={{
          title: 'Disease',
          tabBarIcon: ({ focused }) => <TabIcon emoji="🩺" focused={focused} />,
          headerShown: true,
          headerStyle: { backgroundColor: '#2E7D32' },
          headerTintColor: '#fff',
          headerTitle: '🩺 Disease Detection',
          headerTitleStyle: { fontWeight: 'bold' },
        }}
      />
      <Tab.Screen
        name="AdvisoryTab"
        component={AdvisoryChatScreen}
        options={{
          title: 'Advisory',
          tabBarIcon: ({ focused }) => <TabIcon emoji="💬" focused={focused} />,
          headerShown: true,
          headerStyle: { backgroundColor: '#2E7D32' },
          headerTintColor: '#fff',
          headerTitle: '💬 AI Advisory',
          headerTitleStyle: { fontWeight: 'bold' },
        }}
      />
      <Tab.Screen
        name="WeatherTab"
        component={WeatherScreen}
        options={{
          title: 'Weather',
          tabBarIcon: ({ focused }) => <TabIcon emoji="🌤️" focused={focused} />,
          headerShown: true,
          headerStyle: { backgroundColor: '#2E7D32' },
          headerTintColor: '#fff',
          headerTitle: '🌤️ Weather',
          headerTitleStyle: { fontWeight: 'bold' },
        }}
      />
      <Tab.Screen
        name="MandiTab"
        component={MandiScreen}
        options={{
          title: 'Mandi',
          tabBarIcon: ({ focused }) => <TabIcon emoji="💰" focused={focused} />,
          headerShown: true,
          headerStyle: { backgroundColor: '#2E7D32' },
          headerTintColor: '#fff',
          headerTitle: '💰 Mandi Prices',
          headerTitleStyle: { fontWeight: 'bold' },
        }}
      />
    </Tab.Navigator>
  );
}

// ── Root navigator ────────────────────────────────────────────────────────

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