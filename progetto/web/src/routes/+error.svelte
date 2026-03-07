<script lang="ts">
	import { page } from '$app/stores';
</script>

<svelte:head>
	<title>Errore {$page.status} - BugBoard26</title>
</svelte:head>

<div class="min-h-screen bg-gray-50 flex items-center justify-center px-4">
	<div class="text-center">
		<p class="text-7xl font-bold text-blue-600 mb-4">{$page.status}</p>
		<h1 class="text-2xl font-bold text-gray-900 mb-2">
			{#if $page.status === 404}
				Pagina non trovata
			{:else}
				Si è verificato un errore
			{/if}
		</h1>
		<p class="text-gray-600 mb-8">
			{$page.error?.message || 'La pagina che stai cercando non esiste.'}
		</p>
		<a href="/issues" class="inline-flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors">
			<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path>
			</svg>
			Torna alle Issue
		</a>
	</div>
</div>
