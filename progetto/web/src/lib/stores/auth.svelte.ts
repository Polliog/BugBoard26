import type { User } from '$lib/types';
import { authApi } from '$lib/api/auth.api';

let _user = $state<User | null>(null);

export const authStore = {
	get user() {
		return _user;
	},
	get isAdmin() {
		return _user?.role === 'ADMIN';
	},
	get isExternal() {
		return _user?.role === 'EXTERNAL';
	},
	get isLoggedIn() {
		return _user !== null;
	},

	async login(email: string, password: string) {
		const res = await authApi.login(email, password);
		localStorage.setItem('bugboard_token', res.token);
		_user = res.user;
	},

	logout() {
		localStorage.removeItem('bugboard_token');
		_user = null;
	},

	async restore() {
		const token = localStorage.getItem('bugboard_token');
		if (!token) return;
		try {
			_user = await authApi.getMe();
		} catch {
			localStorage.removeItem('bugboard_token');
		}
	}
};
