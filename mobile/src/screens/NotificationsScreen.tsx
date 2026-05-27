import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  RefreshControl,
} from 'react-native';
import { useSelector, useDispatch } from 'react-redux';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootState, AppDispatch } from '../store';
import { AppTabParamList } from '../navigation/AppNavigator';
import { advisoryApi } from '../api/advisoryApi';
import { markAsRead, setAdvisories } from '../store/notificationSlice';

type NotifScreenNavigationProp = StackNavigationProp<AppTabParamList, 'Notifications'>;

interface Props {
  navigation: NotifScreenNavigationProp;
}

const PRIORITY_ICONS: Record<string, string> = {
  weather: '🌧️',
  disease: '🦠',
  irrigation: '💧',
  fertilizer: '🧪',
  market: '💰',
  general: '📋',
};

const PRIORITY_COLORS: Record<string, string> = {
  high: '#FF3D00',
  medium: '#FF9800',
  low: '#4CAF50',
};

export default function NotificationsScreen({ navigation }: Props) {
  const dispatch = useDispatch<AppDispatch>();
  const farmer = useSelector((state: RootState) => state.farmer);
  const { items, unreadCount } = useSelector((state: RootState) => state.notifications);
  const [loading, setLoading] = useState(false);

  const fetchAdvisories = useCallback(async () => {
    if (!farmer.id || !farmer.authToken) return;
    setLoading(true);
    try {
      const data = await advisoryApi.getAll(farmer.id, farmer.authToken);
      dispatch(setAdvisories(data.advisories || []));
    } catch (err) {
      console.warn('[Notifications] Fetch failed:', err);
    } finally {
      setLoading(false);
    }
  }, [farmer, dispatch]);

  useEffect(() => {
    fetchAdvisories();
  }, [fetchAdvisories]);

  const handleMarkAsRead = async (id: string) => {
    if (!farmer.authToken) return;
    try {
      await advisoryApi.markAsRead(id, farmer.authToken);
      dispatch(markAsRead(id));
    } catch (err) {
      console.warn('[Notifications] Mark read failed:', err);
    }
  };

  const renderAdvisory = ({ item }: { item: any }) => {
    const isUnread = !item.isRead;
    const icon = PRIORITY_ICONS[item.alertType] || '📋';
    const priorityColor = PRIORITY_COLORS[item.priority] || '#999';

    return (
      <TouchableOpacity
        style={[styles.card, isUnread && styles.cardUnread]}
        onPress={() => handleMarkAsRead(item.id)}
        activeOpacity={0.7}
      >
        {/* Left: icon + priority indicator */}
        <View style={styles.cardLeft}>
          <Text style={styles.cardIcon}>{icon}</Text>
          <View style={[styles.priorityDot, { backgroundColor: priorityColor }]} />
        </View>

        {/* Center: message */}
        <View style={styles.cardBody}>
          <Text style={[styles.cardMessage, isUnread && styles.cardMessageUnread]} numberOfLines={3}>
            {item.messageMr || item.messageEn}
          </Text>
          {item.messageEn && item.messageMr ? (
            <Text style={styles.cardMessageEn} numberOfLines={2}>
              {item.messageEn}
            </Text>
          ) : null}
          <View style={styles.cardMeta}>
            <Text style={styles.cardType}>{item.alertType}</Text>
            <Text style={styles.cardDate}>
              {new Date(item.createdAt).toLocaleDateString('en-IN', {
                day: 'numeric',
                month: 'short',
                hour: '2-digit',
                minute: '2-digit',
              })}
            </Text>
          </View>
        </View>

        {/* Right: unread indicator */}
        {isUnread && (
          <View style={styles.unreadDot} />
        )}
      </TouchableOpacity>
    );
  };

  return (
    <View style={styles.container}>
      {/* Header with count */}
      <View style={styles.header}>
        <Text style={styles.title}>🔔 Notifications</Text>
        {unreadCount > 0 && (
          <View style={styles.unreadBadge}>
            <Text style={styles.unreadBadgeText}>{unreadCount} new</Text>
          </View>
        )}
      </View>

      <FlatList
        data={items}
        renderItem={renderAdvisory}
        keyExtractor={item => item.id}
        contentContainerStyle={styles.listContent}
        refreshControl={<RefreshControl refreshing={loading} onRefresh={fetchAdvisories} colors={['#2E7D32']} />}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <Text style={styles.emptyIcon}>📭</Text>
            <Text style={styles.emptyText}>No notifications yet</Text>
            <Text style={styles.emptySubtext}>Advisories will appear here as they arrive</Text>
          </View>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 12,
  },
  title: { fontSize: 24, fontWeight: 'bold', color: '#2E7D32' },
  unreadBadge: { backgroundColor: '#FF3D00', borderRadius: 12, paddingHorizontal: 12, paddingVertical: 4 },
  unreadBadgeText: { color: '#fff', fontSize: 13, fontWeight: 'bold' },
  listContent: { padding: 16, paddingTop: 4 },
  card: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    borderRadius: 14,
    padding: 16,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOpacity: 0.04,
    shadowOffset: { width: 0, height: 2 },
    elevation: 2,
  },
  cardUnread: { borderLeftWidth: 4, borderLeftColor: '#2E7D32' },
  cardLeft: { alignItems: 'center', marginRight: 14, width: 36 },
  cardIcon: { fontSize: 28, marginBottom: 4 },
  priorityDot: { width: 8, height: 8, borderRadius: 4 },
  cardBody: { flex: 1 },
  cardMessage: { fontSize: 15, color: '#555', lineHeight: 21 },
  cardMessageUnread: { color: '#222', fontWeight: '600' },
  cardMessageEn: { fontSize: 13, color: '#888', marginTop: 4, lineHeight: 18 },
  cardMeta: { flexDirection: 'row', alignItems: 'center', marginTop: 8, gap: 10 },
  cardType: {
    fontSize: 12,
    color: '#2E7D32',
    backgroundColor: '#E8F5E9',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 8,
    overflow: 'hidden',
  },
  cardDate: { fontSize: 12, color: '#aaa' },
  unreadDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#2E7D32',
    alignSelf: 'center',
    marginLeft: 8,
  },
  emptyState: { alignItems: 'center', paddingVertical: 60 },
  emptyIcon: { fontSize: 48, marginBottom: 14 },
  emptyText: { fontSize: 18, fontWeight: '600', color: '#555', marginBottom: 6 },
  emptySubtext: { fontSize: 14, color: '#888' },
});