import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ActivityIndicator,
  TouchableOpacity,
  ScrollView,
  RefreshControl,
} from 'react-native';
import { useSelector } from 'react-redux';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootState } from '../store';
import { AppTabParamList } from '../navigation/AppNavigator';
import { weatherApi } from '../api/weatherApi';

type WeatherScreenNavigationProp = StackNavigationProp<AppTabParamList, 'Weather'>;

interface Props {
  navigation: WeatherScreenNavigationProp;
}

interface WeatherData {
  weather_summary?: string;
  advice_mr?: string;
  advice_en?: string;
  alert_type?: string;
  priority?: string;
  temperature?: number;
  humidity?: number;
  rainfall_mm?: number;
  description?: string;
  notification_sent?: boolean;
}

export default function WeatherScreen({ navigation }: Props) {
  const farmer = useSelector((state: RootState) => state.farmer);
  const [weather, setWeather] = useState<WeatherData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchWeather = useCallback(async () => {
    if (!farmer.id || !farmer.authToken) return;

    setLoading(true);
    setError('');
    try {
      const data = await weatherApi.getWeather({
        farmer_id: farmer.id,
        authToken: farmer.authToken,
      });
      setWeather(data);
    } catch (err: any) {
      setError('Failed to fetch weather. Pull to retry.');
    } finally {
      setLoading(false);
    }
  }, [farmer]);

  useEffect(() => {
    fetchWeather();
  }, [fetchWeather]);

  const getWeatherEmoji = (temp?: number, rainfall?: number) => {
    if (rainfall && rainfall > 5) return '🌧️';
    if (temp && temp > 38) return '🥵';
    if (temp && temp < 10) return '🥶';
    if (temp && temp > 30) return '☀️';
    return '⛅';
  };

  const getPriorityColor = (priority?: string) => {
    switch (priority) {
      case 'high': return '#FF3D00';
      case 'medium': return '#FF9800';
      default: return '#4CAF50';
    }
  };

  if (loading && !weather) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#2E7D32" />
        <Text style={styles.loadingText}>Fetching weather data...</Text>
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      refreshControl={<RefreshControl refreshing={loading} onRefresh={fetchWeather} colors={['#2E7D32']} />}
    >
      <Text style={styles.title}>🌤️ Weather Advisory</Text>

      {error ? (
        <View style={styles.errorCard}>
          <Text style={styles.errorText}>{error}</Text>
          <TouchableOpacity style={styles.retryButton} onPress={fetchWeather}>
            <Text style={styles.retryText}>Retry</Text>
          </TouchableOpacity>
        </View>
      ) : weather ? (
        <>
          {/* Main weather card */}
          <View style={styles.weatherCard}>
            <Text style={styles.weatherEmoji}>
              {getWeatherEmoji(weather.temperature, weather.rainfall_mm)}
            </Text>

            <View style={styles.weatherRow}>
              <View style={styles.weatherStat}>
                <Text style={styles.statLabel}>🌡️ Temp</Text>
                <Text style={styles.statValue}>
                  {weather.temperature != null ? `${weather.temperature.toFixed(1)}°C` : 'N/A'}
                </Text>
              </View>
              <View style={styles.weatherStat}>
                <Text style={styles.statLabel}>💧 Humidity</Text>
                <Text style={styles.statValue}>
                  {weather.humidity != null ? `${weather.humidity}%` : 'N/A'}
                </Text>
              </View>
              <View style={styles.weatherStat}>
                <Text style={styles.statLabel}>🌧️ Rain (48h)</Text>
                <Text style={styles.statValue}>
                  {weather.rainfall_mm != null ? `${weather.rainfall_mm.toFixed(1)}mm` : 'N/A'}
                </Text>
              </View>
            </View>

            {weather.description ? (
              <Text style={styles.weatherDesc}>{weather.description}</Text>
            ) : null}

            {weather.priority && (
              <View style={[styles.priorityBadge, { backgroundColor: getPriorityColor(weather.priority) }]}>
                <Text style={styles.priorityText}>
                  {weather.priority === 'high' ? '⚠️ ALERT' : weather.priority === 'medium' ? '⚡ Advisory' : '✅ All Clear'}
                </Text>
              </View>
            )}
          </View>

          {/* Advisory card - Marathi */}
          {weather.advice_mr ? (
            <View style={styles.advisoryCard}>
              <Text style={styles.advisoryLang}>मराठी</Text>
              <Text style={styles.advisoryText}>{weather.advice_mr}</Text>
            </View>
          ) : null}

          {/* Advisory card - English */}
          {weather.advice_en ? (
            <View style={[styles.advisoryCard, styles.advisoryCardEn]}>
              <Text style={styles.advisoryLang}>English</Text>
              <Text style={styles.advisoryText}>{weather.advice_en}</Text>
            </View>
          ) : null}

          {/* Notification status */}
          {weather.notification_sent && (
            <View style={styles.notificationBanner}>
              <Text style={styles.notificationBannerText}>
                📳 Push alert sent because of significant weather event
              </Text>
            </View>
          )}

          {/* Summary */}
          {weather.weather_summary ? (
            <View style={styles.summaryCard}>
              <Text style={styles.summaryTitle}>📋 Summary</Text>
              <Text style={styles.summaryText}>{weather.weather_summary}</Text>
            </View>
          ) : null}
        </>
      ) : null}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  content: { padding: 20, paddingBottom: 40 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#f5f5f5' },
  loadingText: { fontSize: 16, color: '#888', marginTop: 14 },
  title: { fontSize: 24, fontWeight: 'bold', color: '#2E7D32', textAlign: 'center', marginBottom: 20 },
  errorCard: { backgroundColor: '#FFEBEE', borderRadius: 12, padding: 20, alignItems: 'center', marginBottom: 16 },
  errorText: { fontSize: 15, color: '#C62828', marginBottom: 12 },
  retryButton: { backgroundColor: '#C62828', borderRadius: 8, paddingHorizontal: 20, paddingVertical: 10 },
  retryText: { color: '#fff', fontWeight: '600' },
  weatherCard: {
    backgroundColor: '#fff',
    borderRadius: 16,
    padding: 24,
    alignItems: 'center',
    marginBottom: 16,
    shadowColor: '#000',
    shadowOpacity: 0.06,
    shadowOffset: { width: 0, height: 3 },
    elevation: 3,
  },
  weatherEmoji: { fontSize: 56, marginBottom: 16 },
  weatherRow: { flexDirection: 'row', justifyContent: 'space-around', width: '100%', marginBottom: 14 },
  weatherStat: { alignItems: 'center' },
  statLabel: { fontSize: 13, color: '#888', marginBottom: 4 },
  statValue: { fontSize: 20, fontWeight: 'bold', color: '#333' },
  weatherDesc: { fontSize: 15, color: '#555', textAlign: 'center', marginBottom: 12 },
  priorityBadge: { paddingHorizontal: 16, paddingVertical: 6, borderRadius: 20 },
  priorityText: { color: '#fff', fontSize: 14, fontWeight: 'bold' },
  advisoryCard: {
    backgroundColor: '#E8F5E9',
    borderRadius: 14,
    padding: 18,
    marginBottom: 12,
  },
  advisoryCardEn: {
    backgroundColor: '#E3F2FD',
    marginBottom: 12,
  },
  advisoryLang: { fontSize: 12, color: '#888', marginBottom: 6, fontWeight: '600' },
  advisoryText: { fontSize: 15, color: '#333', lineHeight: 22 },
  notificationBanner: {
    backgroundColor: '#FFF3E0',
    borderRadius: 10,
    padding: 14,
    alignItems: 'center',
    marginBottom: 12,
  },
  notificationBannerText: { fontSize: 14, color: '#E65100', fontWeight: '600' },
  summaryCard: {
    backgroundColor: '#fff',
    borderRadius: 14,
    padding: 18,
    shadowColor: '#000',
    shadowOpacity: 0.04,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  summaryTitle: { fontSize: 16, fontWeight: 'bold', color: '#333', marginBottom: 8 },
  summaryText: { fontSize: 14, color: '#555', lineHeight: 20 },
});