import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import { useAdvisoryPolling } from '../hooks/useAdvisoryPolling';

/**
 * HomeScreen — Main tab the farmer sees after login.
 *
 * Demonstrates:
 * - Bell icon badge (unread notification count)
 * - Polling activated automatically via useAdvisoryPolling() hook
 * - Redux state read via useSelector
 */
export default function HomeScreen() {
  const farmer = useSelector((state: RootState) => state.farmer);
  const unreadCount = useSelector(
    (state: RootState) => state.notifications.unreadCount
  );

  // Activate 30-second polling for advisories (FCM fallback)
  useAdvisoryPolling();

  return (
    <View style={styles.container}>
      {/* Header with bell badge */}
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>नमस्कार, {farmer.name || 'Farmer'}! 👋</Text>
          <Text style={styles.subtitle}>Welcome to KrishiMitra</Text>
        </View>
        <View style={styles.bellContainer}>
          <Text style={styles.bellIcon}>🔔</Text>
          {unreadCount > 0 && (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{unreadCount}</Text>
            </View>
          )}
        </View>
      </View>

      {/* Quick actions grid */}
      <View style={styles.actionsGrid}>
        <TouchableOpacity style={styles.actionCard}>
          <Text style={styles.actionIcon}>🩺</Text>
          <Text style={styles.actionLabel}>Disease{'\n'}Detection</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.actionCard}>
          <Text style={styles.actionIcon}>💬</Text>
          <Text style={styles.actionLabel}>AI{'\n'}Advisory</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.actionCard}>
          <Text style={styles.actionIcon}>🌤️</Text>
          <Text style={styles.actionLabel}>Weather{'\n'}Alert</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.actionCard}>
          <Text style={styles.actionIcon}>💰</Text>
          <Text style={styles.actionLabel}>Mandi{'\n'}Prices</Text>
        </TouchableOpacity>
      </View>

      {/* Recent advisories placeholder */}
      <View style={styles.advisoriesSection}>
        <Text style={styles.sectionTitle}>Recent Advisories</Text>
        <Text style={styles.emptyText}>No advisories yet. They'll appear here!</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    paddingHorizontal: 20,
    paddingTop: 10,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 25,
    marginTop: 10,
  },
  greeting: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#333',
  },
  subtitle: {
    fontSize: 14,
    color: '#888',
    marginTop: 4,
  },
  bellContainer: {
    position: 'relative',
    padding: 8,
  },
  bellIcon: {
    fontSize: 28,
  },
  badge: {
    position: 'absolute',
    top: 2,
    right: 2,
    backgroundColor: '#FF3D00',
    borderRadius: 10,
    width: 20,
    height: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  badgeText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: 'bold',
  },
  actionsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 25,
  },
  actionCard: {
    width: '48%',
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 20,
    alignItems: 'center',
    marginBottom: 12,
    shadowColor: '#000',
    shadowOpacity: 0.05,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  actionIcon: {
    fontSize: 36,
    marginBottom: 8,
  },
  actionLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    textAlign: 'center',
  },
  advisoriesSection: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 20,
    shadowColor: '#000',
    shadowOpacity: 0.05,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 10,
  },
  emptyText: {
    color: '#999',
    fontSize: 14,
    textAlign: 'center',
    paddingVertical: 20,
  },
});