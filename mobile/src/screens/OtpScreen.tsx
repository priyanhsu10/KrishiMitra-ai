import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
} from 'react-native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RouteProp } from '@react-navigation/native';
import { useDispatch } from 'react-redux';
import { AuthStackParamList } from '../navigation/AppNavigator';
import { authApi } from '../api/authApi';
import { setFarmer } from '../store/farmerSlice';
import { registerFCMToken } from '../services/notifications';

type OtpScreenNavigationProp = StackNavigationProp<AuthStackParamList, 'Otp'>;
type OtpScreenRouteProp = RouteProp<AuthStackParamList, 'Otp'>;

interface Props {
  navigation: OtpScreenNavigationProp;
  route: OtpScreenRouteProp;
}

/**
 * OtpScreen — Step 2 of auth flow.
 * User enters the 6-digit OTP (mock: "123456").
 * On success:
 *   1. Save farmer data to Redux (like storing in session)
 *   2. Register FCM token with backend (for push notifications)
 *   3. Navigate to language selection
 *
 * Analogy: This is like Spring Security's AuthenticationProvider —
 * it verifies credentials and sets the security context.
 */
export default function OtpScreen({ navigation, route }: Props) {
  const { mobile } = route.params;
  const dispatch = useDispatch();

  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const inputRefs = [
    useRef<TextInput>(null),
    useRef<TextInput>(null),
    useRef<TextInput>(null),
    useRef<TextInput>(null),
    useRef<TextInput>(null),
    useRef<TextInput>(null),
  ];

  // Auto-focus first input on mount
  useEffect(() => {
    inputRefs[0].current?.focus();
  }, []);

  // Handle single digit input and auto-advance to next box
  const handleOtpChange = (text: string, index: number) => {
    const newOtp = [...otp];
    newOtp[index] = text.replace(/[^0-9]/g, '').slice(0, 1);
    setOtp(newOtp);

    // Auto-advance to next input
    if (text && index < 5) {
      inputRefs[index + 1].current?.focus();
    }
  };

  const handleKeyPress = (key: string, index: number) => {
    // Backspace: go to previous input
    if (key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs[index - 1].current?.focus();
    }
  };

  const handleVerify = async () => {
    const otpString = otp.join('');
    if (otpString.length !== 6) {
      Alert.alert('Error', 'Please enter the complete 6-digit OTP');
      return;
    }

    setLoading(true);
    try {
      // Verify OTP with backend
      const result = await authApi.verify(mobile, otpString);

      // Extract farmer data from response
      const { token, farmer_id, name, language } = result;

      // Save to Redux (like setting the security context / session)
      dispatch(
        setFarmer({
          id: farmer_id,
          name,
          mobile,
          language,
          authToken: token,
        })
      );

      // Register FCM token for push notifications
      // (fire-and-forget — failure doesn't block login)
      registerFCMToken(farmer_id, token);

      // Navigate to language selection
      navigation.navigate('Language', {
        mobile,
        token,
        farmerId: farmer_id,
      });
    } catch (error: any) {
      Alert.alert(
        'Verification Failed',
        error?.response?.data?.message || 'Invalid OTP. Use 123456 for testing.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Verify OTP</Text>
      <Text style={styles.subtitle}>
        We sent a 6-digit code to{'\n'}+91 {mobile}
      </Text>

      {/* OTP input boxes — like a 6-digit PIN pad */}
      <View style={styles.otpRow}>
        {otp.map((digit, index) => (
          <TextInput
            key={index}
            ref={inputRefs[index]}
            style={[styles.otpBox, digit ? styles.otpBoxFilled : null]}
            keyboardType="number-pad"
            maxLength={1}
            value={digit}
            onChangeText={(text) => handleOtpChange(text, index)}
            onKeyPress={({ nativeEvent }) => handleKeyPress(nativeEvent.key, index)}
          />
        ))}
      </View>

      <TouchableOpacity
        style={[styles.button, loading && styles.buttonDisabled]}
        onPress={handleVerify}
        disabled={loading}
      >
        <Text style={styles.buttonText}>
          {loading ? 'Verifying...' : 'Verify & Continue'}
        </Text>
      </TouchableOpacity>

      <Text style={styles.hint}>💡 For testing, use OTP: 123456</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    justifyContent: 'center',
    paddingHorizontal: 30,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#2E7D32',
    textAlign: 'center',
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    marginBottom: 30,
  },
  otpRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 30,
    gap: 10,
  },
  otpBox: {
    flex: 1,
    backgroundColor: '#fff',
    borderWidth: 2,
    borderColor: '#ddd',
    borderRadius: 10,
    padding: 16,
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  otpBoxFilled: {
    borderColor: '#2E7D32',
    backgroundColor: '#E8F5E9',
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
  hint: {
    textAlign: 'center',
    color: '#999',
    marginTop: 20,
    fontSize: 14,
  },
});