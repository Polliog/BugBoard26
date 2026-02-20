import { api } from './client';
import type { User } from '$lib/types';

export const usersApi = {
	getAll: () => api.get<User[]>('/api/users'),

	create: (data: { email: string; password: string; name: string; role: string }) =>
		api.post<User>('/api/users', data),

	update: (id: string, data: { email?: string; name?: string; role?: string; password?: string }) =>
		api.patch<User>(`/api/users/${id}`, data),

	delete: (id: string) => api.delete<void>(`/api/users/${id}`)
};
