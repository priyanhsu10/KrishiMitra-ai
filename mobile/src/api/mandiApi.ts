import axios from 'axios';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';

export const mandiApi = {
  /**
   * Get mandi prices for a crop.
   * GET /api/v1/mandi?crop=soybean&state=Maharashtra
   */
  async getPrices(params: {
    crop: string;
    state?: string;
    authToken: string;
  }) {
    const response = await axios.get(`${API_BASE_URL}/mandi`, {
      params: {
        crop: params.crop,
        state: params.state || 'Maharashtra',
      },
      headers: {
        Authorization: `Bearer ${params.authToken}`,
      },
    });
    return response.data;
  },
};