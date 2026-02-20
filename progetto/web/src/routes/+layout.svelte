<script lang="ts">
	import '../app.css';
	import { browser } from '$app/environment';
	import { Toaster } from 'svelte-sonner';
	import { authStore } from '$lib/stores/auth.svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';

	let { children } = $props();
	let restoring = $state(true);

	onMount(async () => {
		await authStore.restore();
		restoring = false;
	});

	$effect(() => {
		if (restoring) return;
		const path = $page.url.pathname;
		if (!authStore.isLoggedIn && path !== '/login') {
			goto('/login');
		}
	});
</script>

{#if browser}
	<Toaster richColors position="top-right" />
{/if}

{#if restoring}
	<div class="min-h-screen flex items-center justify-center" role="status">
		<div class="animate-spin h-8 w-8 border-4 border-blue-600 border-t-transparent rounded-full" aria-hidden="true"></div>
		<span class="sr-only">Caricamento applicazione...</span>
	</div>
{:else}
	{@render children?.()}
{/if}
