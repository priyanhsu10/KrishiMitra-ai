import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface Notification {
  id: string;
  alertType: string;
  message: string;
  priority: string;
  isRead: boolean;
  createdAt: string;
}

interface NotificationState {
  items: Notification[];
  unreadCount: number;
}

const initialState: NotificationState = {
  items: [],
  unreadCount: 0,
};

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    setAdvisories: (state, action: PayloadAction<Notification[]>) => {
      state.items = action.payload;
      state.unreadCount = action.payload.filter(a => !a.isRead).length;
    },
    addNotification: (state, action: PayloadAction<Notification>) => {
      state.items.unshift(action.payload);
    },
    setUnreadCount: (state, action: PayloadAction<number>) => {
      state.unreadCount = action.payload;
    },
    markAsRead: (state, action: PayloadAction<string>) => {
      const notification = state.items.find(n => n.id === action.payload);
      if (notification && !notification.isRead) {
        notification.isRead = true;
        state.unreadCount = Math.max(0, state.unreadCount - 1);
      }
    },
  },
});

export const { setAdvisories, addNotification, setUnreadCount, markAsRead } = notificationSlice.actions;
export default notificationSlice.reducer;
