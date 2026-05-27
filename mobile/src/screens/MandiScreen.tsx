import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  FlatList,
  ActivityIndicator,
  RefreshControl,
} from 'react-native';
import { useSelector } from 'react-redux';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootState } from '../store';
import { AppTabParamList } from '../navigation/AppNavigator';
import { mandiApi } from '../api/mandiApi';

type MandiScreenNavigationProp = StackNavigationProp<AppTabParamList, 'Mandi'>;

interface Props {
  navigation: MandiScreenNavigationProp;
}

interface Price {
  mandi: string;
  price_per_quintal: number;
  trend: string;
}

const CROPS = [
  { label: '🌱 Soybean', value: 'soybean' },
  { label: '🌿 Cotton', value: 'cotton' },
  { label: '🌾 Wheat', value: 'wheat' },
  { label: '🧅 Onion', value: 'onion' },
];

const TREND_LABELS: Record<string, { icon: string; color: string; label: string }> = {
  rising: { icon: '📈', color: '#4CAF50', label: 'Rising' },
  stable: { icon: '➡️', color: '#FF9800', label: 'Stable' },
  falling: { icon: '📉', color: '#FF3D00', label: 'Falling' },
};

export default function MandiScreen({ navigation }: Props) {
  const farmer = useSelector((state: RootState) => state.farmer);
  const [selectedCrop, setSelectedCrop] = useState('soybean');
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const fetchPrices = useCallback(async (crop: string) => {
    if (!farmer.authToken) return;
    setLoading(true);
    try {
      const result = await mandiApi.getPrices({ crop, authToken: farmer.authToken });
      setData(result);
    } catch (err) {
      console.warn('[Mandi] Fetch failed:', err);
    } finally {
      setLoading(false);
    }
  }, [farmer]);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>💰 Mandi Prices</Text>
      <Text style={styles.subtitle}>Check market rates for your crops</Text>

      {/* Crop chips */}
      <View style={styles.chipRow}>
        {CROPS.map(c => (
          <TouchableOpacity
            key={c.value}
            style={[styles.chip, selectedCrop === c.value && styles.chipActive]}
            onPress={() => {
              setSelectedCrop(c.value);
              setData(null);
              fetchPrices(c.value);
            }}
          >
            <Text style={[styles.chipText, selectedCrop === c.value && styles.chipTextActive]}>
              {c.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Price list */}
      <FlatList
        data={data?.prices || []}
        keyExtractor={(item: Price) => item.mandi}
        contentContainerStyle={styles.listContent}
        refreshControl={<RefreshControl refreshing={loading} onRefresh={() => fetchPrices(selectedCrop)} />}
        ListHeaderComponent={
          <>
            {data?.advice_mr && (
              <View style={styles.adviceBox}>
                <Text style={styles.adviceText}>{data.advice_mr}</Text>
                {data.best_time_to_sell === 'now' && (
                  <View style={styles.bestTimeBadge}>
                    <Text style={styles.bestTimeText}>🔥 Best time to sell</Text>
                  </View>
                )}
              </View>
            )}
            {loading && !data && (
              <ActivityIndicator size="large" color="#2E7D32" style={styles.loader} />
            )}
          </>
        }
        renderItem={({ item }: { item: Price }) => {
          const trend = TREND_LABELS[item.trend] || TREND_LABELS.stable;
          return (
            <View style={styles.priceCard}>
              <View style={styles.priceLeft}>
                <Text style={styles.mandiName}>{item.mandi}</Text>
                <Text style={[styles.trendLabel, { color: trend.color }]}>
                  {trend.icon} {trend.label}
                </Text>
              </View>
              <View style={styles.priceRight}>
                <Text style={styles.priceValue}>₹{item.price_per_quintal}</Text>
                <Text style={styles.priceUnit}>per quintal</Text>
              </View>
            </View>
          );
        }}
        ListEmptyComponent={
          !loading && (data?.prices?.length === 0 || !data) ? (
            <View style={styles.empty}>
              <Text style={styles.emptyIcon}>📊</Text>
              <Text style={styles.emptyText}>Select a crop above to view prices</Text>
            </View>
          ) : null
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  title: {
    fontSize: 24, fontWeight: 'bold', color: '#2E7D32',
    textAlign: 'center', paddingTop: 20, marginBottom: 4,
  },
  subtitle: { fontSize: 14, color: '#666', textAlign: 'center', marginBottom: 20 },
  chipRow: { flexDirection: 'row', paddingHorizontal: 16, gap: 10, marginBottom: 16 },
  chip: {
    flex: 1, paddingVertical: 10,
    backgroundColor: '#fff', borderRadius: 20, alignItems: 'center',
    borderWidth: 1, borderColor: '#ddd',
  },
  chipActive: { backgroundColor: '#2E7D32', borderColor: '#2E7D32' },
  chipText: { fontSize: 14, color: '#555', fontWeight: '500' },
  chipTextActive: { color: '#fff' },
  loader: { paddingVertical: 40 },
  listContent: { paddingHorizontal: 16, paddingBottom: 30 },
  adviceBox: {
    backgroundColor: '#E8F5E9', borderRadius: 14, padding: 16, marginBottom: 16,
    borderLeftWidth: 4, borderLeftColor: '#2E7D32',
  },
  adviceText: { fontSize: 15, color: '#333', lineHeight: 22 },
  bestTimeBadge: {
    alignSelf: 'flex-start', backgroundColor: '#2E7D32',
    paddingHorizontal: 12, paddingVertical: 4, borderRadius: 12, marginTop: 10,
  },
  bestTimeText: { color: '#fff', fontSize: 12, fontWeight: 'bold' },
  priceCard: {
    flexDirection: 'row', backgroundColor: '#fff', borderRadius: 14,
    padding: 16, marginBottom: 10, alignItems: 'center',
    shadowColor: '#000', shadowOpacity: 0.04, shadowOffset: { width: 0, height: 2 }, elevation: 2,
  },
  priceLeft: { flex: 1 },
  mandiName: { fontSize: 17, fontWeight: 'bold', color: '#333', marginBottom: 4 },
  trendLabel: { fontSize: 14, fontWeight: '600' },
  priceRight: { alignItems: 'flex-end' },
  priceValue: { fontSize: 22, fontWeight: 'bold', color: '#2E7D32' },
  priceUnit: { fontSize: 12, color: '#888' },
  empty: { alignItems: 'center', paddingVertical: 60 },
  emptyIcon: { fontSize: 48, marginBottom: 14 },
  emptyText: { fontSize: 16, color: '#888' },
});