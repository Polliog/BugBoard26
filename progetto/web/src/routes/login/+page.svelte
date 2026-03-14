<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
	import { goto } from '$app/navigation';
	import { toast } from 'svelte-sonner';
	import Spinner from '$lib/components/ui/Spinner.svelte';

	let email = $state('');
	let password = $state('');
	let emailError = $state('');
	let passwordError = $state('');
	let loading = $state(false);
	let loginError = $state('');

	function validateEmail(value: string): boolean {
		emailError = '';
		if (!value) { emailError = 'Email richiesta'; return false; }
		if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) { emailError = 'Formato email non valido'; return false; }
		return true;
	}

	function validatePassword(value: string): boolean {
		passwordError = '';
		if (!value) { passwordError = 'Password richiesta'; return false; }
		if (value.length < 6) { passwordError = 'Minimo 6 caratteri'; return false; }
		return true;
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		loginError = '';
		if (!validateEmail(email) || !validatePassword(password)) return;

		loading = true;
		try {
			await authStore.login(email, password);
			toast.success('Login effettuato!');
			goto('/issues');
		} catch (err) {
			loginError = err instanceof Error ? err.message : 'Errore durante il login';
		} finally {
			loading = false;
		}
	}
</script>

<svelte:head>
	<title>Login - BugBoard26</title>
</svelte:head>

<div class="min-h-screen bg-gray-50 dark:bg-gray-950 flex items-center justify-center px-4 py-12 transition-colors">
	<div class="w-full max-w-md">
		<div class="text-center mb-8">
			<div class="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-xl mb-4 shadow-lg shadow-blue-500/20">
				<svg class="w-10 h-10 text-white" aria-hidden="true" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
						d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
				</svg>
			</div>
			<h1 class="text-3xl font-bold text-gray-900 dark:text-white transition-colors">BugBoard26</h1>
			<p class="mt-2 text-gray-600 dark:text-gray-400 transition-colors">Accedi al tuo account</p>
		</div>

		<div class="bg-white dark:bg-gray-900 rounded-2xl shadow-xl p-8 border border-gray-200 dark:border-gray-800 transition-colors">
			<form onsubmit={handleSubmit} class="space-y-6">
				<div>
					<label for="email" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-2">Email</label>
					<input type="email" id="email" bind:value={email}
						onblur={() => validateEmail(email)}
						class="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
						class:border-red-500={emailError}
						placeholder="admin@bugboard.com" disabled={loading} />
					{#if emailError}<p class="mt-1.5 text-sm text-red-600 dark:text-red-400">{emailError}</p>{/if}
				</div>

				<div>
					<label for="password" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-2">Password</label>
					<input type="password" id="password" bind:value={password}
						onblur={() => validatePassword(password)}
						class="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
						class:border-red-500={passwordError}
						placeholder="••••••" disabled={loading} />
					{#if passwordError}<p class="mt-1.5 text-sm text-red-600 dark:text-red-400">{passwordError}</p>{/if}
				</div>

				{#if loginError}
					<div class="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800/50 rounded-lg transition-colors">
						<p class="text-sm text-red-800 dark:text-red-400">{loginError}</p>
					</div>
				{/if}

				<button type="submit" disabled={loading}
					class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-3 px-4 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center shadow-md shadow-blue-500/20">
					{#if loading}
						<Spinner size="sm" />
						<span class="ml-2">Accesso in corso...</span>
					{:else}
						Accedi
					{/if}
				</button>
			</form>
		</div>
	</div>
</div>
