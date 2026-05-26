import messaging from '@react-native-firebase/messaging';
import { store } from '../store';
import { addNotification, setUnreadCount } from '../store/notificationSlice';

/**
 * Register FCM token with backend after login.
 * Call this right after OTP verification succeeds.
 */
export async function registerFCMToken(farmerId: string, authToken: string): Promise<boolean> {
  try {
    const authStatus = await messaging().requestPermission();
    const granted =
      authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
      authStatus === messaging.AuthorizationStatus.PROVISIONAL;

    if (granted) {
      const fcmToken = await messaging().getToken();
      
      // Send to backend
      const response = await fetch(`${process.env.API_BASE_URL}/auth/register-token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`
        },
        body: JSON.stringify({
          farmer_id: farmerId,
          fcm_token: fcmToken
        })
      });

      if (response.ok) {
        console.log('[FCM] Token registered successfully');
        return true;
      } else {
        console.warn('[FCM] Token registration failed:', response.status);
        return false;
      }
    }
    
    return false;
  } catch (error) {
    console.warn('[FCM] Registration error:', error);
    return false;
  }
}

/**
 * Setup foreground message listener.
 * Call once in App.tsx root component.
 */
export function setupForegroundListener() {
  return messaging().onMessage(async remoteMessage => {
    console.log('[FCM] Foreground message received:', remoteMessage);
    
    const { alert_type, priority, advisory_id } = remoteMessage.data ?? {};
    
    // Add to Redux store and show in-app notification
    store.dispatch(addNotification({
      id: advisory_id,
      alertType: alert_type,
      message: remoteMessage.notification?.body ?? '',
      priority,
      isRead: false,
      createdAt: new Date().toISOString(),
    }));
    
    const currentUnread = store.getState().notifications.unreadCount;
    store.dispatch(setUnreadCount(currentUnread + 1));
  });
}

/**
 * Background message handler.
 * Add this to index.js:
 * messaging().setBackgroundMessageHandler(async remoteMessage => {
 *   console.log('[FCM] Background message:', remoteMessage);
 * });
 */
