import axios from 'axios';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1';

export const diseaseApi = {
  /**
   * Upload a leaf image for disease detection.
   * Multipart form-data POST to /api/v1/disease/detect
   */
  async detect(params: {
    file: { uri: string; name: string; type: string };
    farmer_id: string;
    crop_id?: string;
    crop_type: string;
    language?: string;
    authToken: string;
  }) {
    const formData = new FormData();

    formData.append('file', {
      uri: params.file.uri,
      name: params.file.name || 'leaf.jpg',
      type: params.file.type || 'image/jpeg',
    } as any);

    formData.append('farmer_id', params.farmer_id);
    formData.append('crop_type', params.crop_type);
    formData.append('language', params.language || 'marathi');

    if (params.crop_id) {
      formData.append('crop_id', params.crop_id);
    }

    const response = await axios.post(
      `${API_BASE_URL}/disease/detect`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${params.authToken}`,
        },
      }
    );

    return response.data;
  },
};