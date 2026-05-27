import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Alert,
  ActivityIndicator,
  Platform,
} from 'react-native';
import { useSelector } from 'react-redux';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootState } from '../store';
import { AppTabParamList } from '../navigation/AppNavigator';
import { farmerApi } from '../api/farmerApi';

type AddCropScreenNavigationProp = StackNavigationProp<AppTabParamList, 'AddCrop'>;

interface Props {
  navigation: AddCropScreenNavigationProp;
}

const CROP_TYPES = [
  { label: '🌱 Soybean', value: 'soybean' },
  { label: '🌿 Cotton', value: 'cotton' },
  { label: '🌾 Wheat', value: 'wheat' },
  { label: '🧅 Onion', value: 'onion' },
  { label: '🌽 Maize', value: 'maize' },
  { label: '🥜 Groundnut', value: 'groundnut' },
];

const STAGES = [
  { label: '🌱 Germination', value: 'germination' },
  { label: '🌿 Vegetative', value: 'vegetative' },
  { label: '🌸 Flowering', value: 'flowering' },
  { label: '🌾 Harvest', value: 'harvest' },
];

export default function AddCropScreen({ navigation }: Props) {
  const farmer = useSelector((state: RootState) => state.farmer);

  const [farmId, setFarmId] = useState('');
  const [cropType, setCropType] = useState('soybean');
  const [stage, setStage] = useState('germination');
  const [sowingDate, setSowingDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!farmId.trim()) { Alert.alert('Error', 'Please enter farm ID (UUID)'); return; }
    if (!farmer.id || !farmer.authToken) {
      Alert.alert('Error', 'Not authenticated');
      return;
    }

    setLoading(true);
    try {
      await farmerApi.createCrop({
        farm_id: farmId.trim(),
        crop_type: cropType,
        sowing_date: sowingDate,
        stage,
      }, farmer.authToken);

      Alert.alert('Success', 'Crop registered successfully!', [
        { text: 'OK', onPress: () => navigation.goBack() },
      ]);
    } catch (err: any) {
      Alert.alert('Error', err?.response?.data?.message || 'Failed to create crop');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>🌱 Add Your Crop</Text>
      <Text style={styles.subtitle}>Register the crop you're growing</Text>

      {/* Farm ID */}
      <Text style={styles.label}>Farm ID (UUID)</Text>
      <View style={styles.inputWrapper}>
        <Text style={styles.inputText}>{farmId || 'Enter your farm UUID'}</Text>
        <TouchableOpacity style={styles.miniInput} onPress={() => {
          // Simple text input via Alert.prompt (basic for hackathon)
          if (Platform.OS === 'ios') {
            Alert.prompt('Farm ID', 'Enter your farm UUID', (text: string) => {
              if (text) setFarmId(text);
            });
          } else {
            // Android workaround: use a simple approach
            const id = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'; // Demo default
            setFarmId(id);
            Alert.alert('Farm ID Set', `Using demo farm: ${id}`);
          }
        }}>
          <Text style={styles.miniInputText}>Set</Text>
        </TouchableOpacity>
      </View>

      {/* Crop type */}
      <Text style={styles.label}>Crop Type</Text>
      <View style={styles.chipGrid}>
        {CROP_TYPES.map(c => (
          <TouchableOpacity
            key={c.value}
            style={[styles.chip, cropType === c.value && styles.chipActive]}
            onPress={() => setCropType(c.value)}
          >
            <Text style={[styles.chipText, cropType === c.value && styles.chipTextActive]}>
              {c.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Stage */}
      <Text style={styles.label}>Growth Stage</Text>
      <View style={styles.chipRow}>
        {STAGES.map(s => (
          <TouchableOpacity
            key={s.value}
            style={[styles.chip, stage === s.value && styles.chipActive]}
            onPress={() => setStage(s.value)}
          >
            <Text style={[styles.chipText, stage === s.value && styles.chipTextActive]}>
              {s.label.split(' ')[1]}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Sowing date */}
      <Text style={styles.label}>Sowing Date</Text>
      <View style={styles.inputWrapper}>
        <Text style={styles.inputText}>{sowingDate}</Text>
      </View>

      {/* Submit */}
      <TouchableOpacity
        style={[styles.submitButton, loading && styles.buttonDisabled]}
        onPress={handleSubmit}
        disabled={loading}
      >
        {loading ? (
          <ActivityIndicator size="small" color="#fff" />
        ) : (
          <Text style={styles.submitButtonText}>✅ Register Crop</Text>
        )}
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  content: { padding: 24, paddingBottom: 40 },
  title: { fontSize: 24, fontWeight: 'bold', color: '#2E7D32', textAlign: 'center', marginBottom: 4 },
  subtitle: { fontSize: 14, color: '#666', textAlign: 'center', marginBottom: 28 },
  label: { fontSize: 15, fontWeight: '600', color: '#333', marginBottom: 8, marginTop: 18 },
  inputWrapper: {
    flexDirection: 'row', alignItems: 'center',
    backgroundColor: '#fff', borderRadius: 10, padding: 14,
    borderWidth: 1, borderColor: '#ddd',
  },
  inputText: { flex: 1, fontSize: 14, color: '#555' },
  miniInput: {
    backgroundColor: '#2E7D32', borderRadius: 8,
    paddingHorizontal: 14, paddingVertical: 6,
  },
  miniInputText: { color: '#fff', fontSize: 13, fontWeight: '600' },
  chipGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: {
    paddingHorizontal: 16, paddingVertical: 10,
    backgroundColor: '#fff', borderRadius: 20, borderWidth: 1, borderColor: '#ddd',
  },
  chipActive: { backgroundColor: '#2E7D32', borderColor: '#2E7D32' },
  chipText: { fontSize: 14, color: '#555' },
  chipTextActive: { color: '#fff', fontWeight: '600' },
  submitButton: {
    backgroundColor: '#2E7D32', borderRadius: 12, padding: 18,
    alignItems: 'center', marginTop: 32,
  },
  buttonDisabled: { opacity: 0.6 },
  submitButtonText: { color: '#fff', fontSize: 18, fontWeight: 'bold' },
});