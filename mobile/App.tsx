import React, { useEffect } from 'react';
import { Provider } from 'react-redux';
import { store } from './src/store';
import RootNavigator from './src/navigation/AppNavigator';
import { setupForegroundListener } from './src/services/notifications';

/**
 * App.tsx — The application entry point.
 *
 * ANALOGY FOR JAVA DEV:
 * ======================
 * This is like your Spring Boot main class (KrishiMitraApplication.java).
 * It bootstraps the app, sets up the "container" (Redux = like Spring IoC),
 * and mounts the root component.
 *
 * Key pieces:
 * 1. <Provider> — Wraps the app with Redux store (like @EnableAutoConfiguration)
 * 2. <RootNavigator> — Handles all screen routing (like @RequestMapping routing)
 * 3. setupForegroundListener() — Registers FCM push handler (like a MessageListener)
 */
export default function App() {
  useEffect(() => {
    // Setup FCM foreground listener as soon as the app starts.
    // This ensures we catch push notifications even if the user
    // hasn't logged in yet (the listener will just no-op until
    // the Redux store has farmer data).
    const unsubscribe = setupForegroundListener();

    // Cleanup on unmount (like @PreDestroy / shutdown hook)
    return () => {
      unsubscribe();
    };
  }, []);

  return (
    <Provider store={store}>
      <RootNavigator />
    </Provider>
  );
}
