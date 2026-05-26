import { configureStore } from '@reduxjs/toolkit';
import notificationReducer from './notificationSlice';
import farmerReducer from './farmerSlice';

export const store = configureStore({
  reducer: {
    notifications: notificationReducer,
    farmer: farmerReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
