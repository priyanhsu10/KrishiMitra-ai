import axios from 'axios';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';

export const authApi = {
  /**
   * Request OTP for login.
   */
  async login(mobile: string) {
    const response = await axios.post(`${API_BASE_URL}/auth/login`, {
      mobile
    });
    return response.data;
  },

  /**
   * Verify OTP and get auth token.
   */
  async verify(mobile: string, otp: string) {
    const response = await axios.post(`${API_BASE_URL}/auth/verify`, {
      mobile,
      otp
    });
    return response.data;
  },

  /**
   * Register FCM token after login.
   */
  async registerToken(farmerId: string, fcmToken: string, authToken: string) {
    const response = await axios.post(
      `${API_BASE_URL}/auth/register-token`,
      {
        farmer_id: farmerId,
        fcm_token: fcmToken
      },
      {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      }
    );
    return response.data;
  }
};
