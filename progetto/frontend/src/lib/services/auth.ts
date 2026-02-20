// Authentication Service
import type { User, LoginCredentials } from '$lib/types';
import { mockUsers } from './mockData';

const AUTH_TOKEN_KEY = 'bugboard_auth_token';
const AUTH_USER_KEY = 'bugboard_auth_user';

export class AuthService {
	/**
	 * Login con email e password
	 */
	static async login(credentials: LoginCredentials): Promise<User> {
		// Simula delay API
		await new Promise((resolve) => setTimeout(resolve, 800));

		const user = mockUsers.find(
			(u) => u.email === credentials.email && u.password === credentials.password
		);

		if (!user) {
			throw new Error('Email o password non corretti');
		}

		// Genera token fittizio
		const token = btoa(`${user.id}:${Date.now()}`);

		// Salva in localStorage
		if (credentials.rememberMe) {
			localStorage.setItem(AUTH_TOKEN_KEY, token);
			localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
		} else {
			sessionStorage.setItem(AUTH_TOKEN_KEY, token);
			sessionStorage.setItem(AUTH_USER_KEY, JSON.stringify(user));
		}

		return user;
	}

	/**
	 * Logout
	 */
	static logout(): void {
		localStorage.removeItem(AUTH_TOKEN_KEY);
		localStorage.removeItem(AUTH_USER_KEY);
		sessionStorage.removeItem(AUTH_TOKEN_KEY);
		sessionStorage.removeItem(AUTH_USER_KEY);
	}

	/**
	 * Recupera utente autenticato
	 */
	static getCurrentUser(): User | null {
		if (typeof window === 'undefined') return null;

		const userJson =
			localStorage.getItem(AUTH_USER_KEY) || sessionStorage.getItem(AUTH_USER_KEY);
		if (!userJson) return null;

		try {
			return JSON.parse(userJson);
		} catch {
			return null;
		}
	}

	/**
	 * Verifica se utente è loggato
	 */
	static isAuthenticated(): boolean {
		if (typeof window === 'undefined') return false;
		const token =
			localStorage.getItem(AUTH_TOKEN_KEY) || sessionStorage.getItem(AUTH_TOKEN_KEY);
		return !!token;
	}

	/**
	 * Verifica se utente è admin
	 */
	static isAdmin(): boolean {
		const user = this.getCurrentUser();
		return user?.role === 'admin';
	}
}
