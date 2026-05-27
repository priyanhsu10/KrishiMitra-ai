import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Alert,
  ActivityIndicator,
} from 'react-native';
import * as Location from 'expo-location';
import { useSelector } from 'react-redux';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootState } from '../store';
import { AppTabParamList } from '../navigation/AppNavigator';
import { farmerApi } from '../api/farmerApi';

type AddFarmScreenNavigationProp = StackNavigationProp<AppTabParamList, 'AddFarm'>;

interface Props {
  navigation: AddFarmScreenNavigationProp;
}

const SOIL_TYPES = [
  { label: 'Black', value: 'black' },
  { label: 'Red', value: 'red' },
  { label: 'Loamy', value: 'loamy' },
  { label: 'Sandy', value: 'sandy' },
  { label: 'Clay', value: 'clay' },
];

export default function AddFarmScreen({ navigation }: Props) {
  const farmer = useSelector((state: RootState) => state.farmer);

  const [name, setName] = useState('');
  const [area, setArea] = useState('');
  const [soilType, setSoilType] = useState('black');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [loading, setLoading] = useState(false);
  const [gettingLocation, setGettingLocation] = useState(false);

  const getCurrentLocation = async () => {
    setGettingLocation(true);
    try {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') {
        Alert.alert('Permission Denied', 'Location permission is required');
        return;
      }
      const loc = await Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.High });
      setLatitude(loc.coords.latitude.toFixed(6));
      setLongitude(loc.coords.longitude.toFixed(6));
    } catch (err) {
      Alert.alert('Error', 'Failed to get location');
    } finally {
      setGettingLocation(false);
    }
  };

  const handleSubmit = async () => {
    if (!name.trim()) { Alert.alert('Error', 'Please enter farm name'); return; }
    if (!latitude || !longitude) { Alert.alert('Error', 'Please pick farm location'); return; }
    if (!area.trim() || isNaN(Number(area))) { Alert.alert('Error', 'Please enter valid area in acres'); return; }

    if (!farmer.id || !farmer.authToken) {
      Alert.alert('Error', 'Not authenticated');
      return;
    }

    setLoading(true);
    try {
      await farmerApi.createFarm({
        farmer_id: farmer.id,
        name: name.trim(),
        latitude: parseFloat(latitude),
        longitude: parseFloat(longitude),
        area_acres: parseFloat(area),
        soil_type: soilType,
      }, farmer.authToken);

      Alert.alert('Success', 'Farm created successfully!', [
        { text: 'OK', onPress: () => navigation.goBack() },
      ]);
    } catch (err: any) {
      Alert.alert('Error', err?.response?.data?.message || 'Failed to create farm');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>🏡 Add Your Farm</Text>
      <Text style={styles.subtitle}>Register your farm details</Text>

      {/* Farm name */}
      <Text style={styles.label}>Farm Name</Text>
      <TextInput
        style={styles.input}
        placeholder="e.g. Main Farm, Green Field"
        value={name}
        onChangeText={setName}
      />

      {/* Area */}
      <Text style={styles.label}>Area (acres)</Text>
      <TextInput
        style={styles.input}
        placeholder="e.g. 5.5"
        keyboardType="decimal-pad"
        value={area}
        onChangeText={setArea}
      />

      {/* Soil type */}
      <Text style={styles.label}>Soil Type</Text>
      <View style={styles.chipRow}>
        {SOIL_TYPES.map(s => (
          <TouchableOpacity
            key={s.value}
            style={[styles.chip, soilType === s.value && styles.chipActive]}
            onPress={() => setSoilType(s.value)}
          >
            <Text style={[styles.chipText, soilType === s.value && styles.chipTextActive]}>
              {s.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Location */}
      <Text style={styles.label}>Farm Location</Text>
      <TouchableOpacity
        style={styles.locationButton}
        onPress={getCurrentLocation}
        disabled={gettingLocation}
      >
        {gettingLocation ? (
          <ActivityIndicator size="small" color="#fff" />
        ) : (
          <Text style={styles.locationButtonText}>
            📍 {latitude ? 'Change Location' : 'Get Current Location'}
          </Text>
        )}
      </TouchableOpacity>

      {(latitude !== '' || longitude !== '') && (
        <View style={styles.coordRow}>
          <View style={styles.coordBox}>
            <Text style={styles.coordLabel}>Latitude</Text>
            <Text style={styles.coordValue}>{latitude || '--'}</Text>
          </View>
          <View style={styles.coordBox}>
            <Text style={styles.coordLabel}>Longitude</Text>
            <Text style={styles.coordValue}>{longitude || '--'}</Text>
          </View>
        </View>
      )}

      {/* Submit */}
      <TouchableOpacity
        style={[styles.submitButton, loading && styles.buttonDisabled]}
        onPress={handleSubmit}
        disabled={loading}
      >
        {loading ? (
          <ActivityIndicator size="small" color="#fff" />
        ) : (
          <Text style={styles.submitButtonText}>✅ Save Farm</Text>
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
  label: { fontSize: 15, fontWeight: '600', color: '#333', marginBottom: 8, marginTop: 16 },
  input: {
    backgroundColor: '#fff', borderRadius: 10, padding: 14, fontSize: 16,
    borderWidth: 1, borderColor: '#ddd',
  },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 8 },
  chip: {
    paddingHorizontal: 16, paddingVertical: 10,
    backgroundColor: '#fff', borderRadius: 20, borderWidth: 1, borderColor: '#ddd',
  },
  chipActive: { backgroundColor: '#2E7D32', borderColor: '#2E7D32' },
  chipText: { fontSize: 14, color: '#555' },
  chipTextActive: { color: '#fff', fontWeight: '600' },
  locationButton: {
    backgroundColor: '#1565C0', borderRadius: 10, padding: 16,
    alignItems: 'center', marginBottom: 12,
  },
  locationButtonText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  coordRow: { flexDirection: 'row', gap: 12, marginBottom: 8 },
  coordBox: {
    flex: 1, backgroundColor: '#E3F2FD', borderRadius: 10, padding: 14, alignItems: 'center',
  },
  coordLabel: { fontSize: 12, color: '#1565C0', marginBottom: 4 },
  coordValue: { fontSize: 16, fontWeight: 'bold', color: '#333' },
  submitButton: {
    backgroundColor: '#2E7D32', borderRadius: 12, padding: 18,
    alignItems: 'center', marginTop: 28,
  },
  buttonDisabled: { opacity: 0.6 },
  submitButtonText: { color: '#fff', fontSize: 18, fontWeight: 'bold' },
});