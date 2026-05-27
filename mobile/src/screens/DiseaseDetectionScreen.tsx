import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
  ActivityIndicator,
  Alert,
  ScrollView,
} from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { useSelector } from 'react-redux';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootState } from '../store';
import { AppTabParamList } from '../navigation/AppNavigator';
import { diseaseApi } from '../api/diseaseApi';

type DiseaseScreenNavigationProp = StackNavigationProp<AppTabParamList, 'Disease'>;

interface Props {
  navigation: DiseaseScreenNavigationProp;
}

export default function DiseaseDetectionScreen({ navigation }: Props) {
  const farmer = useSelector((state: RootState) => state.farmer);
  const [image, setImage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);

  const pickImage = useCallback(async () => {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permission Denied', 'Camera permission is required for disease detection');
      return;
    }

    const pickerResult = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.8,
    });

    if (!pickerResult.canceled && pickerResult.assets?.length > 0) {
      setImage(pickerResult.assets[0].uri);
      setResult(null);
    }
  }, []);

  const pickFromGallery = useCallback(async () => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permission Denied', 'Gallery access is required');
      return;
    }

    const pickerResult = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.8,
    });

    if (!pickerResult.canceled && pickerResult.assets?.length > 0) {
      setImage(pickerResult.assets[0].uri);
      setResult(null);
    }
  }, []);

  const detectDisease = async () => {
    if (!image || !farmer.id || !farmer.authToken) return;

    setLoading(true);
    try {
      const fileUri = image;
      const filename = fileUri.split('/').pop() || 'leaf.jpg';
      const match = /\.(\w+)$/.exec(filename);
      const ext = match ? match[1] : 'jpg';
      const mimeType = ext === 'png' ? 'image/png' : 'image/jpeg';

      const response = await diseaseApi.detect({
        file: { uri: fileUri, name: filename, type: mimeType },
        farmer_id: farmer.id,
        crop_type: 'soybean',
        language: farmer.language || 'marathi',
        authToken: farmer.authToken,
      });

      setResult(response);
    } catch (error: any) {
      Alert.alert('Detection Failed', error?.response?.data?.message || 'Failed to analyze image');
    } finally {
      setLoading(false);
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'high': return '#FF3D00';
      case 'medium': return '#FF9800';
      default: return '#4CAF50';
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>🩺 Disease Detection</Text>
      <Text style={styles.subtitle}>Capture or upload a leaf image to detect crop diseases</Text>

      {/* Image picker section */}
      {!image ? (
        <View style={styles.imagePickerArea}>
          <Text style={styles.imagePickerIcon}>📸</Text>
          <TouchableOpacity style={styles.pickButton} onPress={pickImage}>
            <Text style={styles.pickButtonText}>📷 Take Photo</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.pickButton, styles.galleryButton]} onPress={pickFromGallery}>
            <Text style={styles.pickButtonText}>🖼️ Pick from Gallery</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <View style={styles.imageSection}>
          <Image source={{ uri: image }} style={styles.previewImage} />
          <View style={styles.imageActions}>
            <TouchableOpacity style={styles.miniButton} onPress={() => { setImage(null); setResult(null); }}>
              <Text style={styles.miniButtonText}>Retake</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.detectButton, loading && styles.buttonDisabled]}
              onPress={detectDisease}
              disabled={loading}
            >
              {loading ? (
                <ActivityIndicator color="#fff" size="small" />
              ) : (
                <Text style={styles.detectButtonText}>🔍 Detect Disease</Text>
              )}
            </TouchableOpacity>
          </View>
        </View>
      )}

      {/* Loading indicator */}
      {loading && (
        <View style={styles.loadingSection}>
          <ActivityIndicator size="large" color="#2E7D32" />
          <Text style={styles.loadingText}>Analyzing leaf image with AI...</Text>
          <Text style={styles.loadingSubtext}>Claude Vision is examining your crop</Text>
        </View>
      )}

      {/* Result card */}
      {result && (
        <View style={styles.resultCard}>
          {/* Severity badge */}
          <View style={[styles.severityBadge, { backgroundColor: getSeverityColor(result.severity) }]}>
            <Text style={styles.severityText}>
              {result.severity === 'high' ? '⚠️ HIGH' : result.severity === 'medium' ? '⚡ MEDIUM' : '✅ LOW'} Severity
            </Text>
          </View>

          {/* Disease name */}
          <Text style={styles.diseaseName}>
            {result.disease_mr || result.disease}
          </Text>
          <Text style={styles.diseaseNameEn}>{result.disease}</Text>

          {/* Confidence */}
          <View style={styles.confidenceRow}>
            <Text style={styles.confidenceLabel}>Confidence:</Text>
            <Text style={styles.confidenceValue}>{(result.confidence * 100).toFixed(0)}%</Text>
          </View>

          {/* Remedy card */}
          <View style={styles.remedyCard}>
            <Text style={styles.remedyTitle}>💊 उपाय / Remedy</Text>
            <Text style={styles.remedyText}>{result.remedy_mr}</Text>
            <View style={styles.remedyDivider} />
            <Text style={styles.remedyTextEn}>{result.remedy_en}</Text>
          </View>

          {/* Notification status */}
          {result.notification_sent && (
            <View style={styles.notificationStatus}>
              <Text style={styles.notificationText}>🔔 Alert sent to your village coordinator</Text>
            </View>
          )}
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  content: { padding: 20, paddingBottom: 40 },
  title: { fontSize: 24, fontWeight: 'bold', color: '#2E7D32', textAlign: 'center', marginBottom: 6 },
  subtitle: { fontSize: 14, color: '#666', textAlign: 'center', marginBottom: 24 },
  imagePickerArea: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 40,
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#E0E0E0',
    borderStyle: 'dashed',
    marginBottom: 20,
  },
  imagePickerIcon: { fontSize: 48, marginBottom: 16 },
  pickButton: {
    backgroundColor: '#2E7D32',
    borderRadius: 10,
    paddingHorizontal: 24,
    paddingVertical: 14,
    width: '100%',
    alignItems: 'center',
    marginBottom: 10,
  },
  galleryButton: { backgroundColor: '#1565C0' },
  pickButtonText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  imageSection: { alignItems: 'center', marginBottom: 20 },
  previewImage: { width: '100%', height: 280, borderRadius: 16, marginBottom: 12 },
  imageActions: { flexDirection: 'row', gap: 12, width: '100%' },
  miniButton: {
    flex: 1,
    backgroundColor: '#666',
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: 'center',
  },
  miniButtonText: { color: '#fff', fontSize: 14, fontWeight: '600' },
  detectButton: {
    flex: 2,
    backgroundColor: '#2E7D32',
    borderRadius: 10,
    paddingVertical: 14,
    alignItems: 'center',
  },
  detectButtonText: { color: '#fff', fontSize: 16, fontWeight: 'bold' },
  buttonDisabled: { opacity: 0.6 },
  loadingSection: { alignItems: 'center', paddingVertical: 30 },
  loadingText: { fontSize: 16, color: '#333', marginTop: 16, fontWeight: '600' },
  loadingSubtext: { fontSize: 13, color: '#888', marginTop: 4 },
  resultCard: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 20,
    shadowColor: '#000',
    shadowOpacity: 0.08,
    shadowOffset: { width: 0, height: 4 },
    elevation: 4,
  },
  severityBadge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 20,
    marginBottom: 14,
  },
  severityText: { color: '#fff', fontSize: 13, fontWeight: 'bold' },
  diseaseName: { fontSize: 22, fontWeight: 'bold', color: '#333', marginBottom: 4 },
  diseaseNameEn: { fontSize: 15, color: '#888', marginBottom: 14 },
  confidenceRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 16, gap: 8 },
  confidenceLabel: { fontSize: 14, color: '#666' },
  confidenceValue: { fontSize: 16, fontWeight: 'bold', color: '#2E7D32' },
  remedyCard: {
    backgroundColor: '#E8F5E9',
    borderRadius: 12,
    padding: 16,
    marginBottom: 14,
  },
  remedyTitle: { fontSize: 16, fontWeight: 'bold', color: '#2E7D32', marginBottom: 8 },
  remedyText: { fontSize: 15, color: '#333', lineHeight: 22, marginBottom: 8 },
  remedyDivider: { height: 1, backgroundColor: '#C8E6C9', marginVertical: 8 },
  remedyTextEn: { fontSize: 14, color: '#555', lineHeight: 20 },
  notificationStatus: {
    backgroundColor: '#FFF3E0',
    borderRadius: 10,
    padding: 12,
    alignItems: 'center',
  },
  notificationText: { fontSize: 14, color: '#E65100', fontWeight: '600' },
});