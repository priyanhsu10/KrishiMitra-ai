import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
} from 'react-native';
import { useSelector } from 'react-redux';
import { useNavigation } from '@react-navigation/native';
import { RootState } from '../store';
import { useAdvisoryPolling } from '../hooks/useAdvisoryPolling';

export default function HomeScreen() {
  const farmer = useSelector((state: RootState) => state.farmer);
  const unreadCount = useSelector(
    (state: RootState) => state.notifications.unreadCount
  );
  const navigation = useNavigation<any>();

  // Activate 30-second polling for advisories (FCM fallback)
  useAdvisoryPolling();

  // Navigate helper - tries tab navigation first, then stack
  const goTo = (route: string) => {
    try {
      // Navigate to the bottom tab (e.g. navigate to DiseaseTab tab screen)
      navigation.getParent()?.navigate(route);
    } catch {
      navigation.navigate(route);
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Header with bell badge */}
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>नमस्कार, {farmer.name || 'Farmer'}! 👋</Text>
          <Text style={styles.subtitle}>Welcome to KrishiMitra</Text>
        </View>
        <TouchableOpacity
          style={styles.bellContainer}
          onPress={() => navigation.navigate('Notifications')}
        >
          <Text style={styles.bellIcon}>🔔</Text>
          {unreadCount > 0 && (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{unreadCount}</Text>
            </View>
          )}
        </TouchableOpacity>
      </View>

      {/* Quick actions grid */}
      <Text style={styles.sectionTitle}>Quick Actions</Text>
      <View style={styles.actionsGrid}>
        <TouchableOpacity
          style={styles.actionCard}
          onPress={() => goTo('DiseaseTab')}
        >
          <Text style={styles.actionIcon}>🩺</Text>
          <Text style={styles.actionLabel}>Disease{'\n'}Detection</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.actionCard}
          onPress={() => goTo('AdvisoryTab')}
        >
          <Text style={styles.actionIcon}>💬</Text>
          <Text style={styles.actionLabel}>AI{'\n'}Advisory</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.actionCard}
          onPress={() => goTo('WeatherTab')}
        >
          <Text style={styles.actionIcon}>🌤️</Text>
          <Text style={styles.actionLabel}>Weather{'\n'}Alert</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.actionCard}
          onPress={() => goTo('MandiTab')}
        >
          <Text style={styles.actionIcon}>💰</Text>
          <Text style={styles.actionLabel}>Mandi{'\n'}Prices</Text>
        </TouchableOpacity>
      </View>

      {/* Onboarding actions */}
      <Text style={styles.sectionTitle}>Get Started</Text>
      <View style={styles.actionsGrid}>
        <TouchableOpacity
          style={styles.actionCard}
          onPress={() => navigation.navigate('AddFarm')}
        >
          <Text style={styles.actionIcon}>🏡</Text>
          <Text style={styles.actionLabel}>Add Your{'\n'}Farm</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.actionCard}
          onPress={() => navigation.navigate('AddCrop')}
        >
          <Text style={styles.actionIcon}>🌱</Text>
          <Text style={styles.actionLabel}>Register{'\n'}Your Crop</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.actionCard}
          onPress={() => navigation.navigate('Notifications')}
        >
          <Text style={styles.actionIcon}>🔔</Text>
          <Text style={styles.actionLabel}>View{'\n'}Notifications</Text>
        </TouchableOpacity>

        <View style={[styles.actionCard, styles.actionCardEmpty]}>
          <Text style={styles.actionIcon}>🏠</Text>
          <Text style={styles.actionLabel}>Home{'\n'}Dashboard</Text>
        </View>
      </View>

      {/* Recent advisories section */}
      <Text style={styles.sectionTitle}>Recent Advisories</Text>
      <View style={styles.advisoriesSection}>
        <Text style={styles.emptyText}>Advisories will appear here.</Text>
        <Text style={styles.emptySubtext}>Check notifications or use AI Advisory</Text>
      </View>

      <View style={{ height: 20 }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  content: { paddingHorizontal: 20, paddingBottom: 40 },
  header: {
    flexDirection: 'row', justifyContent: 'space-between',
    alignItems: 'flex-start', marginBottom: 24, marginTop: 20,
  },
  greeting: { fontSize: 22, fontWeight: 'bold', color: '#333' },
  subtitle: { fontSize: 14, color: '#888', marginTop: 4 },
  bellContainer: { position: 'relative', padding: 8 },
  bellIcon: { fontSize: 28 },
  badge: {
    position: 'absolute', top: 2, right: 2,
    backgroundColor: '#FF3D00', borderRadius: 10,
    minWidth: 20, height: 20, justifyContent: 'center',
    alignItems: 'center', paddingHorizontal: 4,
  },
  badgeText: { color: '#fff', fontSize: 12, fontWeight: 'bold' },
  sectionTitle: { fontSize: 18, fontWeight: 'bold', color: '#333', marginBottom: 14, marginTop: 12 },
  actionsGrid: {
    flexDirection: 'row', flexWrap: 'wrap',
    justifyContent: 'space-between', marginBottom: 10,
  },
  actionCard: {
    width: '48%', backgroundColor: '#fff', borderRadius: 12,
    padding: 18, alignItems: 'center', marginBottom: 12,
    shadowColor: '#000', shadowOpacity: 0.05, shadowOffset: { width: 0, height: 2 }, elevation: 2,
  },
  actionCardEmpty: { opacity: 0.4 },
  actionIcon: { fontSize: 36, marginBottom: 8 },
  actionLabel: { fontSize: 14, fontWeight: '600', color: '#333', textAlign: 'center', lineHeight: 20 },
  advisoriesSection: {
    backgroundColor: '#fff', borderRadius: 12, padding: 24,
    shadowColor: '#000', shadowOpacity: 0.05, shadowOffset: { width: 0, height: 2 }, elevation: 2,
    alignItems: 'center',
  },
  emptyText: { color: '#999', fontSize: 15, fontWeight: '600', marginBottom: 4 },
  emptySubtext: { color: '#bbb', fontSize: 13 },
});