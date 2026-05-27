import axios from 'axios';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';

export const weatherApi = {
  /**
   * Get weather advisory for a farmer's farm.
   * GET /api/v1/weather?farmer_id={}&farm_id={}
   */
  async getWeather(params: {
    farmer_id: string;
    farm_id?: string;
    authToken: string;
  }) {
    const response = await axios.get(`${API_BASE_URL}/weather`, {
      params: {
        farmer_id: params.farmer_id,
        farm_id: params.farm_id,
      },
      headers: {
        Authorization: `Bearer ${params.authToken}`,
      },
    });
    return response.data;
  },
};