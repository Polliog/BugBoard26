// Auth Store (Svelte 5 Runes)
import type { User } from '$lib/types';
import { AuthService } from '$lib/services/auth';

interface AuthState {
	user: User | null;
	isLoading: boolean;
	error: string | null;
}

function createAuthStore() {
	let state = $state<AuthState>({
		user: null,
		isLoading: false,
		error: null
	});

	// Inizializza con utente esistente
	if (typeof window !== 'undefined') {
		state.user = AuthService.getCurrentUser();
	}

	return {
		get user() {
			return state.user;
		},
		get isLoading() {
			return state.isLoading;
		},
		get error() {
			return state.error;
		},
		get isAuthenticated() {
			return !!state.user;
		},
		get isAdmin() {
			return state.user?.role === 'admin';
		},

		async login(email: string, password: string, rememberMe: boolean = false) {
			state.isLoading = true;
			state.error = null;

			try {
				const user = await AuthService.login({ email, password, rememberMe });
				state.user = user;
				return user;
			} catch (err) {
				const error = err instanceof Error ? err.message : 'Errore durante il login';
				state.error = error;
				throw err;
			} finally {
				state.isLoading = false;
			}
		},

		logout() {
			AuthService.logout();
			state.user = null;
			state.error = null;
		},

		clearError() {
			state.error = null;
		}
	};
}

export const authStore = createAuthStore();
