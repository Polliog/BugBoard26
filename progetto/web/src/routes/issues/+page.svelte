<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
	import { toast } from 'svelte-sonner';
	import Navbar from '$lib/components/Navbar.svelte';
	import Spinner from '$lib/components/ui/Spinner.svelte';
	import IssueCard from '$lib/components/issues/IssueCard.svelte';
	import IssueFilters from '$lib/components/issues/IssueFilters.svelte';
	import IssueForm from '$lib/components/issues/IssueForm.svelte';
	import { issuesApi, type IssueFilters as Filters } from '$lib/api/issues.api';
	import { usersApi } from '$lib/api/users.api';
	import { can } from '$lib/utils/permissions';
	import type { Issue, User, IssueType, IssuePriority, IssueStatus } from '$lib/types';

	let issues = $state<Issue[]>([]);
	let users = $state<User[]>([]);
	let total = $state(0);
	let loading = $state(true);
	let isFormOpen = $state(false);

	let search = $state('');
	let selectedTypes = $state<IssueType[]>([]);
	let selectedPriorities = $state<IssuePriority[]>([]);
	let selectedStatuses = $state<IssueStatus[]>([]);
	let selectedAssignee = $state('');
	let showArchived = $state(false);
	let showDeleted = $state(false);
	let sortBy = $state('createdAt');
	let currentPage = $state(0);
	const pageSize = 20;

	async function loadIssues() {
		loading = true;
		try {
			const filters: Filters = {
				search: search || undefined,
				type: selectedTypes.length ? selectedTypes : undefined,
				priority: selectedPriorities.length ? selectedPriorities : undefined,
				status: selectedStatuses.length ? selectedStatuses : undefined,
				assignedToId: selectedAssignee || undefined,
				archived: showArchived || undefined,
				deleted: showDeleted || undefined,
				page: currentPage,
				pageSize,
				sortBy,
				order: 'desc'
			};
			const res = await issuesApi.getAll(filters);
			issues = res.data;
			total = res.total;
		} catch (err) {
			toast.error('Errore caricamento issue');
		} finally {
			loading = false;
		}
	}

	async function loadUsers() {
		try {
			users = await usersApi.getAll();
		} catch { /* ignore */ }
	}

	import { onMount } from 'svelte';
	onMount(() => {
		loadIssues();
		loadUsers();
	});

	function handleFiltersChange() {
		currentPage = 0;
		loadIssues();
	}

	async function handleCreateIssue(data: Parameters<typeof issuesApi.create>[0]) {
		try {
			await issuesApi.create(data);
			toast.success('Issue creata!');
			loadIssues();
		} catch (err) {
			toast.error('Errore creazione issue');
		}
	}

	async function handleExport(format: 'csv' | 'pdf') {
		try {
			const blob = await issuesApi.exportFile(format);
			const url = URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = `issues.${format}`;
			a.click();
			URL.revokeObjectURL(url);
		} catch {
			toast.error('Errore export');
		}
	}
</script>

<svelte:head>
	<title>Issue - BugBoard26</title>
</svelte:head>

<div class="min-h-screen bg-gray-50">
	<Navbar />

	<div class="max-w-7xl mx-auto px-4 py-8">
		<div class="mb-8">
			<div class="flex items-start justify-between mb-4">
				<div>
					<h1 class="text-3xl font-bold text-gray-900 mb-2">Issue</h1>
					<p class="text-gray-600">Gestisci le issue del team</p>
				</div>
				<div class="flex gap-2">
					<button onclick={() => handleExport('csv')}
						class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm">
						Export CSV
					</button>
					<button onclick={() => handleExport('pdf')}
						class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors text-sm">
						Export PDF
					</button>
					{#if can(authStore.user, 'create:issue')}
						<button onclick={() => (isFormOpen = true)}
							class="bg-blue-600 hover:bg-blue-700 text-white font-medium px-4 py-2 rounded-lg transition-colors flex items-center gap-2">
							<svg class="w-5 h-5" aria-hidden="true" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
							</svg>
							Crea Issue
						</button>
					{/if}
				</div>
			</div>
		</div>

		<IssueFilters
			bind:search bind:selectedTypes bind:selectedPriorities
			bind:selectedStatuses bind:selectedAssignee bind:showArchived bind:showDeleted bind:sortBy
			{users} currentUser={authStore.user} onchange={handleFiltersChange}
		/>

		{#if loading}
			<div class="flex justify-center py-12">
				<Spinner size="lg" />
			</div>
		{:else if issues.length === 0}
			<div class="bg-white rounded-xl shadow-sm p-12 text-center">
				<svg class="w-16 h-16 text-gray-400 mx-auto mb-4" aria-hidden="true" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
						d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
				</svg>
				<h3 class="text-lg font-medium text-gray-900 mb-2">Nessuna issue trovata</h3>
				<p class="text-gray-600">Prova a modificare i filtri</p>
			</div>
		{:else}
			<div class="space-y-3">
				{#each issues as issue (issue.id)}
					<IssueCard {issue} href={`/issues/${issue.id}`} />
				{/each}
			</div>

			{#if total > pageSize}
				<nav aria-label="Paginazione" class="flex justify-center gap-2 mt-6">
					<button onclick={() => { currentPage = Math.max(0, currentPage - 1); loadIssues(); }}
						disabled={currentPage === 0}
						aria-label="Pagina precedente"
						class="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50">
						Precedente
					</button>
					<span class="px-4 py-2 text-gray-600" aria-live="polite" aria-atomic="true">
						Pagina {currentPage + 1} di {Math.ceil(total / pageSize)}
					</span>
					<button onclick={() => { currentPage++; loadIssues(); }}
						disabled={(currentPage + 1) * pageSize >= total}
						aria-label="Pagina successiva"
						class="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50">
						Successiva
					</button>
				</nav>
			{/if}
		{/if}
	</div>
</div>

<IssueForm isOpen={isFormOpen} {users} currentUser={authStore.user}
	onClose={() => (isFormOpen = false)}
	onSubmit={handleCreateIssue} />
