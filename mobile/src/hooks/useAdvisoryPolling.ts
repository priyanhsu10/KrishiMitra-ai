import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { advisoryApi } from '../api/advisoryApi';
import { setAdvisories } from '../store/notificationSlice';
import { RootState } from '../store';

/**
 * Hook to poll advisories every 30 seconds.
 * Acts as FCM fallback to ensure notifications are never missed.
 */
export function useAdvisoryPolling() {
  const dispatch = useDispatch();
  const { id: farmerId, authToken } = useSelector((state: RootState) => state.farmer);

  useEffect(() => {
    if (!farmerId || !authToken) return;

    // Fetch immediately on mount
    const fetchAdvisories = async () => {
      try {
        const response = await advisoryApi.getAll(farmerId, authToken);
        dispatch(setAdvisories(response.advisories));
      } catch (error) {
        console.warn('[Polling] Advisory fetch failed:', error);
      }
    };

    fetchAdvisories();
    
    // Poll every 30 seconds
    const interval = setInterval(fetchAdvisories, 30000);
    
    return () => clearInterval(interval);
  }, [farmerId, authToken, dispatch]);
}
