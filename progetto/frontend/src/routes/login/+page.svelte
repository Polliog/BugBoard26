<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
	import { goto } from '$app/navigation';
	import { toast } from 'svelte-sonner';

	let email = $state('');
	let password = $state('');
	let rememberMe = $state(false);
	let emailError = $state('');
	let passwordError = $state('');

	function validateEmail(value: string): boolean {
		emailError = '';
		if (!value) {
			emailError = 'Email richiesta';
			return false;
		}
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(value)) {
			emailError = 'Formato email non valido';
			return false;
		}
		return true;
	}

	function validatePassword(value: string): boolean {
		passwordError = '';
		if (!value) {
			passwordError = 'Password richiesta';
			return false;
		}
		if (value.length < 6) {
			passwordError = 'Password deve essere di almeno 6 caratteri';
			return false;
		}
		return true;
	}

	async function handleSubmit(e: Event) {
		e.preventDefault();
		authStore.clearError();

		// Validazione
		const isEmailValid = validateEmail(email);
		const isPasswordValid = validatePassword(password);

		if (!isEmailValid || !isPasswordValid) {
			return;
		}

		try {
			await authStore.login(email, password, rememberMe);
			toast.success('Login effettuato con successo!');
			goto('/projects');
		} catch (err) {
			// Errore già gestito nello store
			console.error('Login failed:', err);
		}
	}
</script>

<svelte:head>
	<title>Login - BugBoard26</title>
</svelte:head>

<div class="min-h-screen bg-gray-50 flex items-center justify-center px-4 py-12">
	<div class="w-full max-w-md">
		<!-- Logo e Header -->
		<div class="text-center mb-8">
			<div class="inline-flex items-center justify-center w-16 h-16 bg-blue-600 rounded-xl mb-4">
				<svg
					class="w-10 h-10 text-white"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
					></path>
				</svg>
			</div>
			<h1 class="text-3xl font-bold text-gray-900">BugBoard26</h1>
			<p class="mt-2 text-gray-600">Accedi al tuo account</p>
		</div>

		<!-- Form Card -->
		<div class="bg-white rounded-xl shadow-md p-8">
			<form onsubmit={handleSubmit} class="space-y-6">
				<!-- Email Input -->
				<div>
					<label for="email" class="block text-sm font-medium text-gray-900 mb-2">
						Email
					</label>
					<input
						type="email"
						id="email"
						bind:value={email}
						onblur={() => validateEmail(email)}
						class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
						class:border-red-500={emailError}
						class:focus:ring-red-500={emailError}
						placeholder="admin@bugboard.com"
						disabled={authStore.isLoading}
					/>
					{#if emailError}
						<p class="mt-1.5 text-sm text-red-600">{emailError}</p>
					{/if}
				</div>

				<!-- Password Input -->
				<div>
					<label for="password" class="block text-sm font-medium text-gray-900 mb-2">
						Password
					</label>
					<input
						type="password"
						id="password"
						bind:value={password}
						onblur={() => validatePassword(password)}
						class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors"
						class:border-red-500={passwordError}
						class:focus:ring-red-500={passwordError}
						placeholder="••••••"
						disabled={authStore.isLoading}
					/>
					{#if passwordError}
						<p class="mt-1.5 text-sm text-red-600">{passwordError}</p>
					{/if}
				</div>

				<!-- Remember Me -->
				<div class="flex items-center">
					<input
						type="checkbox"
						id="remember"
						bind:checked={rememberMe}
						class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
						disabled={authStore.isLoading}
					/>
					<label for="remember" class="ml-2 text-sm text-gray-700">
						Ricordami
					</label>
				</div>

				<!-- Error Message -->
				{#if authStore.error}
					<div class="p-4 bg-red-50 border border-red-200 rounded-lg">
						<div class="flex items-start">
							<svg
								class="w-5 h-5 text-red-600 mt-0.5 mr-3 flex-shrink-0"
								fill="currentColor"
								viewBox="0 0 20 20"
							>
								<path
									fill-rule="evenodd"
									d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
									clip-rule="evenodd"
								></path>
							</svg>
							<p class="text-sm text-red-800">{authStore.error}</p>
						</div>
					</div>
				{/if}

				<!-- Submit Button -->
				<button
					type="submit"
					disabled={authStore.isLoading}
					class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2.5 px-4 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
				>
					{#if authStore.isLoading}
						<svg
							class="animate-spin h-5 w-5 mr-2"
							fill="none"
							viewBox="0 0 24 24"
						>
							<circle
								class="opacity-25"
								cx="12"
								cy="12"
								r="10"
								stroke="currentColor"
								stroke-width="4"
							></circle>
							<path
								class="opacity-75"
								fill="currentColor"
								d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
							></path>
						</svg>
						Accesso in corso...
					{:else}
						Accedi
					{/if}
				</button>
			</form>
		</div>
	</div>
</div>
