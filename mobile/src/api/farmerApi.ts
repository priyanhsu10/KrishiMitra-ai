import axios from 'axios';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';

export const farmerApi = {
  /**
   * Create or update farmer profile.
   */
  async create(farmer: {
    name: string;
    language: string;
    village?: string;
    state?: string;
  }, authToken: string) {
    const response = await axios.post(
      `${API_BASE_URL}/farmers`,
      farmer,
      {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      }
    );
    return response.data;
  },

  /**
   * Get farmer profile with farms.
   */
  async get(farmerId: string, authToken: string) {
    const response = await axios.get(
      `${API_BASE_URL}/farmers/${farmerId}`,
      {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      }
    );
    return response.data;
  },

  /**
   * Create a new farm.
   */
  async createFarm(farm: {
    farmer_id: string;
    name: string;
    latitude: number;
    longitude: number;
    area_acres: number;
    soil_type: string;
  }, authToken: string) {
    const response = await axios.post(
      `${API_BASE_URL}/farms`,
      farm,
      {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      }
    );
    return response.data;
  },

  /**
   * Create a new crop on a farm.
   */
  async createCrop(crop: {
    farm_id: string;
    crop_type: string;
    sowing_date: string;
    stage: string;
  }, authToken: string) {
    const response = await axios.post(
      `${API_BASE_URL}/crops`,
      crop,
      {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      }
    );
    return response.data;
  }
};