import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Alert,
} from 'react-native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RouteProp } from '@react-navigation/native';
import { useDispatch } from 'react-redux';
import { AuthStackParamList } from '../navigation/AppNavigator';
import { farmerApi } from '../api/farmerApi';

type LanguageScreenNavigationProp = StackNavigationProp<
  AuthStackParamList,
  'Language'
>;
type LanguageScreenRouteProp = RouteProp<AuthStackParamList, 'Language'>;

interface Props {
  navigation: LanguageScreenNavigationProp;
  route: LanguageScreenRouteProp;
}

const LANGUAGES = [
  { code: 'marathi', label: 'मराठी', flag: '🇮🇳' },
  { code: 'hindi', label: 'हिन्दी', flag: '🇮🇳' },
  { code: 'english', label: 'English', flag: '🇬🇧' },
];

/**
 * LanguageScreen — Step 3 of auth flow.
 * Farmer selects their preferred language.
 * On selection, we save it to the backend and Redux.
 *
 * After this, the user is redirected to the Home tab (auto-navigated
 * by AppNavigator since isAuthenticated is now true).
 */
export default function LanguageScreen({ navigation, route }: Props) {
  const { token, farmerId } = route.params;
  const dispatch = useDispatch();
  const [selected, setSelected] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSelect = async (language: string) => {
    setSelected(language);
    setSaving(true);
    try {
      // Save language preference to backend
      await farmerApi.create(
        {
          name: '',
          language,
        },
        token
      );
      // Note: isAuthenticated is already true from OtpScreen
      // NavigationContainer will automatically switch to AppNavigator
    } catch (error: any) {
      Alert.alert('Error', 'Failed to save language preference');
    } finally {
      setSaving(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Choose Your Language</Text>
      <Text style={styles.subtitle}>भाषा निवडा / भाषा चुनें</Text>

      {LANGUAGES.map((lang) => (
        <TouchableOpacity
          key={lang.code}
          style={[
            styles.languageCard,
            selected === lang.code && styles.languageCardSelected,
          ]}
          onPress={() => handleSelect(lang.code)}
          disabled={saving}
        >
          <Text style={styles.flag}>{lang.flag}</Text>
          <Text
            style={[
              styles.languageLabel,
              selected === lang.code && styles.languageLabelSelected,
            ]}
          >
            {lang.label}
          </Text>
          {selected === lang.code && (
            <Text style={styles.checkmark}>✓</Text>
          )}
        </TouchableOpacity>
      ))}

      {saving && <Text style={styles.saving}>Saving preference...</Text>}
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
    marginBottom: 5,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    marginBottom: 30,
  },
  languageCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderWidth: 2,
    borderColor: '#ddd',
    borderRadius: 12,
    padding: 18,
    marginBottom: 12,
  },
  languageCardSelected: {
    borderColor: '#2E7D32',
    backgroundColor: '#E8F5E9',
  },
  flag: {
    fontSize: 28,
    marginRight: 12,
  },
  languageLabel: {
    fontSize: 20,
    color: '#333',
    flex: 1,
  },
  languageLabelSelected: {
    color: '#2E7D32',
    fontWeight: 'bold',
  },
  checkmark: {
    fontSize: 22,
    color: '#2E7D32',
    fontWeight: 'bold',
  },
  saving: {
    textAlign: 'center',
    color: '#999',
    marginTop: 15,
  },
});