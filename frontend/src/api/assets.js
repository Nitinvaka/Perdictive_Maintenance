import apiClient from './client';

export const assetsApi = {
  // Get all assets with optional name and type filters
  getAll: (name, type) => {
    const params = {};
    if (name && name.trim()) params.name = name.trim();
    if (type && type.trim()) params.type = type.trim();
    return apiClient.get('/assets', { params }).then(r => r.data);
  },

  getById: (id) =>
    apiClient.get(`/assets/${id}`).then(r => r.data),

  create: (payload) =>
    apiClient.post('/assets', payload).then(r => r.data),

  update: (id, payload) =>
    apiClient.put(`/assets/${id}`, payload).then(r => r.data),

  setStatus: (id, active) =>
    apiClient.patch(`/assets/${id}/status`, { active }).then(r => r.data),

  delete: (id) =>
    apiClient.delete(`/assets/${id}`).then(r => r.data),

  getViolations: () =>
    apiClient.get('/assets/violations').then(r => r.data),

  getAvgRms: () =>
    apiClient.get('/assets/avg-rms').then(r => r.data),

  getTypes: () =>
    apiClient.get('/assets/types').then(r => r.data),
};
