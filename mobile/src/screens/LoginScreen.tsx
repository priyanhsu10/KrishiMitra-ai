import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { StackNavigationProp } from '@react-navigation/stack';
import { AuthStackParamList } from '../navigation/AppNavigator';
import { authApi } from '../api/authApi';

type LoginScreenNavigationProp = StackNavigationProp<AuthStackParamList, 'Login'>;

interface Props {
  navigation: LoginScreenNavigationProp;
}

/**
 * LoginScreen — Step 1 of auth flow.
 * User enters their mobile number, we call POST /auth/login to get OTP.
 *
 * Think of this like a Spring @Controller POST handler,
 * but for a mobile form instead of an HTTP request.
 */
export default function LoginScreen({ navigation }: Props) {
  const [mobile, setMobile] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    // Validation — like @Valid annotation checks
    if (mobile.trim().length !== 10) {
      Alert.alert('Error', 'Please enter a valid 10-digit mobile number');
      return;
    }

    setLoading(true);
    try {
      // API call — the mobile equivalent of a RestTemplate.postForObject()
      await authApi.login(mobile.trim());
      // Navigate to OTP screen, passing mobile as param
      navigation.navigate('Otp', { mobile: mobile.trim() });
    } catch (error: any) {
      Alert.alert('Error', error?.response?.data?.message || 'Failed to send OTP');
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <View style={styles.inner}>
        <Text style={styles.title}>🌾 Welcome, Farmer!</Text>
        <Text style={styles.subtitle}>Enter your mobile number to get started</Text>

        <TextInput
          style={styles.input}
          placeholder="9876543210"
          keyboardType="phone-pad"
          maxLength={10}
          value={mobile}
          onChangeText={setMobile}
          autoFocus
        />

        <TouchableOpacity
          style={[styles.button, loading && styles.buttonDisabled]}
          onPress={handleLogin}
          disabled={loading}
        >
          <Text style={styles.buttonText}>
            {loading ? 'Sending OTP...' : 'Get OTP'}
          </Text>
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  inner: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 30,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#2E7D32',
    textAlign: 'center',
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    marginBottom: 40,
  },
  input: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 10,
    padding: 16,
    fontSize: 20,
    textAlign: 'center',
    letterSpacing: 2,
    marginBottom: 20,
  },
  button: {
    backgroundColor: '#2E7D32',
    borderRadius: 10,
    padding: 16,
    alignItems: 'center',
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  buttonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
});