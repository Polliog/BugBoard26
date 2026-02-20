// RF06 — Notifications API
import { api } from './client';
import type { Notification } from '$lib/types';

export const notificationsApi = {
	getAll: () => api.get<Notification[]>('/api/notifications'),

	markAsRead: (id: string) =>
		api.patch<Notification>(`/api/notifications/${id}/read`, {})
};
