// RF01 — Auth API
import { api } from './client';
import type { AuthResponse, User } from '$lib/types';

export const authApi = {
	login: (email: string, password: string) =>
		api.post<AuthResponse>('/api/auth/login', { email, password }),

	getMe: () => api.get<User>('/api/auth/me')
};
