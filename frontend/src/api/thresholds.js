import apiClient from './client';

export const thresholdsApi = {
  getAll: () =>
    apiClient.get('/thresholds').then(r => r.data),

  getById: (id) =>
    apiClient.get(`/thresholds/${id}`).then(r => r.data),

  getByAsset: (assetId) =>
    apiClient.get(`/thresholds/by-asset/${assetId}`).then(r => r.data),

  create: (payload) =>
    apiClient.post('/thresholds', payload).then(r => r.data),

  update: (id, payload) =>
    apiClient.put(`/thresholds/${id}`, payload).then(r => r.data),

  delete: (id) =>
    apiClient.delete(`/thresholds/${id}`).then(r => r.data),
};
