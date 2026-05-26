import axios from 'axios';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';

export const advisoryApi = {
  /**
   * Get all advisories for a farmer (for polling fallback).
   */
  async getAll(farmerId: string, authToken: string, unreadOnly: boolean = false) {
    const response = await axios.get(`${API_BASE_URL}/advisories`, {
      params: {
        farmer_id: farmerId,
        unread: unreadOnly
      },
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    return response.data;
  },

  /**
   * Mark an advisory as read.
   */
  async markAsRead(advisoryId: string, authToken: string) {
    const response = await axios.patch(
      `${API_BASE_URL}/advisories/${advisoryId}/read`,
      {},
      {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      }
    );
    return response.data;
  },

  /**
   * Get AI advisory (chat endpoint).
   */
  async getAdvisory(request: {
    farmer_id: string;
    crop_type: string;
    stage: string;
    language: string;
    question: string;
  }, authToken: string) {
    const response = await axios.post(
      `${API_BASE_URL}/advisory/chat`,
      request,
      {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      }
    );
    return response.data;
  }
};
