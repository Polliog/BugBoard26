<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
	import { goto } from '$app/navigation';
	import { toast } from 'svelte-sonner';

	let showUserMenu = $state(false);

	function handleLogout() {
		authStore.logout();
		toast.success('Logout effettuato!');
		goto('/login');
	}
</script>

<nav class="bg-white shadow-sm border-b border-gray-200">
	<div class="max-w-7xl mx-auto px-4 py-4">
		<div class="flex items-center justify-between">
			<!-- Logo -->
			<a href="/projects" class="flex items-center gap-3">
				<div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
					<svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
						></path>
					</svg>
				</div>
				<span class="text-xl font-bold text-gray-900">BugBoard26</span>
			</a>

			<!-- User Menu -->
			<div class="relative">
				<button
					onclick={() => (showUserMenu = !showUserMenu)}
					class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors"
				>
					<div class="text-right">
						<p class="text-sm font-medium text-gray-900">{authStore.user?.name}</p>
						<p class="text-xs text-gray-500 capitalize">{authStore.user?.role}</p>
					</div>
					<div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
						<span class="text-blue-700 font-semibold">
							{authStore.user?.name?.charAt(0).toUpperCase()}
						</span>
					</div>
				</button>

				<!-- Dropdown -->
				{#if showUserMenu}
					<div
						class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50"
					>
						<button
							onclick={handleLogout}
							class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
						>
							Logout
						</button>
					</div>
				{/if}
			</div>
		</div>
	</div>
</nav>

<!-- Click outside per chiudere menu -->
{#if showUserMenu}
	<button
		class="fixed inset-0 z-40"
		onclick={() => (showUserMenu = false)}
		aria-label="Close menu"
	></button>
{/if}
