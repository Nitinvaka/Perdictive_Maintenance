import apiClient from './client';

export const ticketsApi = {
  // Get paginated tickets, optional status filter
  getPage: (params) =>
    apiClient.get('/tickets', { params }).then(r => r.data),

  getById: (id) =>
    apiClient.get(`/tickets/${id}`).then(r => r.data),

  getByAsset: (assetId) =>
    apiClient.get(`/tickets/by-asset/${assetId}`).then(r => r.data),

  // Active tickets = OPEN + IN_PROGRESS (never CLOSED)
  getActive: () =>
    apiClient.get('/tickets/active').then(r => r.data),

  updateStatus: (id, status) =>
    apiClient.patch(`/tickets/${id}/status`, { status }).then(r => r.data),
};
