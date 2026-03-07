import { api } from './client';
import type { Notification } from '$lib/types';

export const notificationsApi = {
	getAll: () => api.get<Notification[]>('/api/notifications'),

	markAsRead: (id: string) =>
		api.patch<Notification>(`/api/notifications/${id}/read`, {}),

	markAllAsRead: () =>
		api.patch<void>('/api/notifications/read-all', {}),

	delete: (id: string) =>
		api.delete<void>(`/api/notifications/${id}`),

	deleteAll: () =>
		api.delete<void>('/api/notifications')
};
