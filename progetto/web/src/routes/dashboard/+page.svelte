<script lang="ts">
	import { onMount } from 'svelte';
	import { toast } from 'svelte-sonner';
	import Navbar from '$lib/components/Navbar.svelte';
	import Spinner from '$lib/components/ui/Spinner.svelte';
	import Badge from '$lib/components/ui/Badge.svelte';
	import ActivityTimeline from '$lib/components/dashboard/ActivityTimeline.svelte';
	import UserManagementTab from '$lib/components/dashboard/UserManagementTab.svelte';
	import { issuesApi } from '$lib/api/issues.api';
	import { authStore } from '$lib/stores/auth.svelte';
	import { can } from '$lib/utils/permissions';
	import { formatDate } from '$lib/utils/dates';
	import type { Issue, IssueStatus, IssuePriority, IssueType } from '$lib/types';

	let loading = $state(true);
	let allIssues = $state<Issue[]>([]);
	let activeTab = $state<'panoramica' | 'utenti'>('panoramica');

	const statusLabels: Record<IssueStatus, string> = {
		TODO: 'Todo',
		IN_PROGRESS: 'In Progress',
		RISOLTA: 'Risolta'
	};
	const statusBarColors: Record<IssueStatus, string> = {
		TODO: 'bg-gray-400 dark:bg-gray-500',
		IN_PROGRESS: 'bg-blue-500',
		RISOLTA: 'bg-green-500'
	};
	const priorityLabels: Record<IssuePriority, string> = {
		BASSA: 'Bassa',
		MEDIA: 'Media',
		ALTA: 'Alta',
		CRITICA: 'Critica'
	};
	const priorityBarColors: Record<IssuePriority, string> = {
		BASSA: 'bg-blue-400',
		MEDIA: 'bg-yellow-400',
		ALTA: 'bg-orange-500',
		CRITICA: 'bg-red-500'
	};
	const typeLabels: Record<IssueType, string> = {
		BUG: 'Bug',
		FEATURE: 'Feature',
		QUESTION: 'Question',
		DOCUMENTATION: 'Documentation'
	};
	const typeBarColors: Record<IssueType, string> = {
		BUG: 'bg-red-500',
		FEATURE: 'bg-green-500',
		QUESTION: 'bg-purple-500',
		DOCUMENTATION: 'bg-blue-500'
	};
	const statusColors: Record<IssueStatus, string> = {
		TODO: 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300',
		IN_PROGRESS: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400',
		RISOLTA: 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400'
	};
	const priorityColors: Record<IssuePriority, string> = {
		BASSA: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400',
		MEDIA: 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400',
		ALTA: 'bg-orange-100 dark:bg-orange-900/30 text-orange-800 dark:text-orange-400',
		CRITICA: 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400'
	};
	const typeColors: Record<IssueType, string> = {
		BUG: 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400',
		FEATURE: 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400',
		QUESTION: 'bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-400',
		DOCUMENTATION: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400'
	};

	let byStatus = $derived(countBy(allIssues, 'status'));
	let byPriority = $derived(countBy(allIssues, 'priority'));
	let byType = $derived(countBy(allIssues, 'type'));
	let myIssues = $derived(allIssues.filter((i) => i.createdBy?.id === authStore.user?.id));
	let recentIssues = $derived(allIssues.slice(0, 5));

	function countBy<K extends keyof Issue>(items: Issue[], key: K): Record<string, number> {
		const counts: Record<string, number> = {};
		for (const item of items) {
			const val = String(item[key] ?? 'N/A');
			counts[val] = (counts[val] || 0) + 1;
		}
		return counts;
	}

	onMount(async () => {
		try {
			const res = await issuesApi.getAll({ pageSize: 1000, sortBy: 'createdAt', order: 'desc' });
			allIssues = res.data;
		} catch {
			toast.error('Errore caricamento dati dashboard');
		} finally {
			loading = false;
		}
	});
</script>

<svelte:head>
	<title>Dashboard - BugBoard26</title>
</svelte:head>

<div class="min-h-screen bg-gray-50 dark:bg-gray-950 transition-colors">
	<Navbar />

	<div class="max-w-7xl mx-auto px-4 py-4 sm:py-8">
		<div class="mb-4 sm:mb-6">
			<h1 class="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-white transition-colors">Dashboard</h1>
			<p class="text-gray-600 dark:text-gray-400 text-sm sm:text-base transition-colors">Panoramica delle issue del team</p>
		</div>

		<!-- Tab bar -->
		<div class="flex gap-1 mb-6 border-b border-gray-200 dark:border-gray-800 transition-colors">
			<button
				onclick={() => activeTab = 'panoramica'}
				class="px-4 py-2.5 text-sm font-medium transition-all border-b-2 -mb-px
					{activeTab === 'panoramica'
						? 'border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400'
						: 'border-transparent text-gray-500 hover:text-gray-900 dark:hover:text-gray-100'}"
			>
				Panoramica
			</button>
			{#if can(authStore.user, 'manage:users')}
				<button
					onclick={() => activeTab = 'utenti'}
					class="px-4 py-2.5 text-sm font-medium transition-all border-b-2 -mb-px
						{activeTab === 'utenti'
							? 'border-blue-600 text-blue-600 dark:text-blue-400 dark:border-blue-400'
							: 'border-transparent text-gray-500 hover:text-gray-900 dark:hover:text-gray-100'}"
				>
					Utenti
				</button>
			{/if}
		</div>

		{#if activeTab === 'panoramica'}
			{#if loading}
				<div class="flex justify-center py-12"><Spinner size="lg" /></div>
			{:else}
				<!-- Summary cards -->
				<div class="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4 mb-6 sm:mb-8">
					<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-3 sm:p-5 flex items-start gap-3 sm:gap-4 border border-gray-100 dark:border-gray-800 transition-colors">
						<div class="p-2 sm:p-2.5 bg-gray-100 dark:bg-gray-800 rounded-lg flex-shrink-0 transition-colors">
							<svg class="w-5 h-5 sm:w-6 sm:h-6 text-gray-600 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
							</svg>
						</div>
						<div>
							<p class="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-white transition-colors">{allIssues.length}</p>
							<p class="text-xs sm:text-sm font-medium text-gray-600 dark:text-gray-400 mt-0.5 transition-colors">Totale Issue</p>
						</div>
					</div>

					<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-3 sm:p-5 flex items-start gap-3 sm:gap-4 border border-gray-100 dark:border-gray-800 transition-colors">
						<div class="p-2 sm:p-2.5 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex-shrink-0 transition-colors">
							<svg class="w-5 h-5 sm:w-6 sm:h-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
							</svg>
						</div>
						<div>
							<p class="text-2xl sm:text-3xl font-bold text-blue-600 dark:text-blue-400 transition-colors">{(byStatus['TODO'] || 0) + (byStatus['IN_PROGRESS'] || 0)}</p>
							<p class="text-xs sm:text-sm font-medium text-gray-600 dark:text-gray-400 mt-0.5 transition-colors">Aperte</p>
							<p class="text-xs text-gray-400 dark:text-gray-500 hidden sm:block transition-colors">TODO + In Progress</p>
						</div>
					</div>

					<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-3 sm:p-5 flex items-start gap-3 sm:gap-4 border border-gray-100 dark:border-gray-800 transition-colors">
						<div class="p-2 sm:p-2.5 bg-green-100 dark:bg-green-900/30 rounded-lg flex-shrink-0 transition-colors">
							<svg class="w-5 h-5 sm:w-6 sm:h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
							</svg>
						</div>
						<div>
							<p class="text-2xl sm:text-3xl font-bold text-green-600 dark:text-green-400 transition-colors">{byStatus['RISOLTA'] || 0}</p>
							<p class="text-xs sm:text-sm font-medium text-gray-600 dark:text-gray-400 mt-0.5 transition-colors">Risolte</p>
							<p class="text-xs text-gray-400 dark:text-gray-500 hidden sm:block transition-colors">Completate</p>
						</div>
					</div>

					<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-3 sm:p-5 flex items-start gap-3 sm:gap-4 border border-gray-100 dark:border-gray-800 transition-colors">
						<div class="p-2 sm:p-2.5 bg-purple-100 dark:bg-purple-900/30 rounded-lg flex-shrink-0 transition-colors">
							<svg class="w-5 h-5 sm:w-6 sm:h-6 text-purple-600 dark:text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
							</svg>
						</div>
						<div>
							<p class="text-2xl sm:text-3xl font-bold text-purple-600 dark:text-purple-400 transition-colors">{myIssues.length}</p>
							<p class="text-xs sm:text-sm font-medium text-gray-600 dark:text-gray-400 mt-0.5 transition-colors">Create da me</p>
							<p class="text-xs text-gray-400 dark:text-gray-500 hidden sm:block transition-colors">Le tue segnalazioni</p>
						</div>
					</div>
				</div>

				<!-- Breakdown panels -->
				<div class="grid sm:grid-cols-2 md:grid-cols-3 gap-4 sm:gap-6 mb-6 sm:mb-8">
					<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-800 transition-colors">
						<h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4 transition-colors">Per Stato</h2>
						<div class="space-y-3">
							{#each Object.entries(statusLabels) as [key, label]}
								{@const count = byStatus[key] || 0}
								{@const pct = allIssues.length ? Math.round((count / allIssues.length) * 100) : 0}
								<div>
									<div class="flex items-center justify-between mb-1">
										<span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium {statusColors[key as IssueStatus]} transition-colors">{label}</span>
										<span class="text-sm font-medium text-gray-700 dark:text-gray-300 transition-colors">{count}</span>
									</div>
									<div class="w-full bg-gray-100 dark:bg-gray-800 rounded-full h-2 transition-colors">
										<div class="h-2 rounded-full transition-all {statusBarColors[key as IssueStatus]}" style="width: {pct}%"></div>
									</div>
								</div>
							{/each}
						</div>
					</div>

					<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-800 transition-colors">
						<h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4 transition-colors">Per Priorità</h2>
						<div class="space-y-3">
							{#each Object.entries(priorityLabels) as [key, label]}
								{@const count = byPriority[key] || 0}
								{@const pct = allIssues.length ? Math.round((count / allIssues.length) * 100) : 0}
								<div>
									<div class="flex items-center justify-between mb-1">
										<span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium {priorityColors[key as IssuePriority]} transition-colors">{label}</span>
										<span class="text-sm font-medium text-gray-700 dark:text-gray-300 transition-colors">{count}</span>
									</div>
									<div class="w-full bg-gray-100 dark:bg-gray-800 rounded-full h-2 transition-colors">
										<div class="h-2 rounded-full transition-all {priorityBarColors[key as IssuePriority]}" style="width: {pct}%"></div>
									</div>
								</div>
							{/each}
						</div>
					</div>

					<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-800 transition-colors">
						<h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4 transition-colors">Per Tipo</h2>
						<div class="space-y-3">
							{#each Object.entries(typeLabels) as [key, label]}
								{@const count = byType[key] || 0}
								{@const pct = allIssues.length ? Math.round((count / allIssues.length) * 100) : 0}
								<div>
									<div class="flex items-center justify-between mb-1">
										<span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium {typeColors[key as IssueType]} transition-colors">{label}</span>
										<span class="text-sm font-medium text-gray-700 dark:text-gray-300 transition-colors">{count}</span>
									</div>
									<div class="w-full bg-gray-100 dark:bg-gray-800 rounded-full h-2 transition-colors">
										<div class="h-2 rounded-full transition-all {typeBarColors[key as IssueType]}" style="width: {pct}%"></div>
									</div>
								</div>
							{/each}
						</div>
					</div>
				</div>

				<!-- Bottom section: Recent Issues + My Issues + Activity Timeline -->
				<div class="grid sm:grid-cols-2 md:grid-cols-3 gap-4 sm:gap-6">
					<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-800 transition-colors">
						<h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4 transition-colors">Issue Recenti</h2>
						{#if recentIssues.length === 0}
							<p class="text-sm text-gray-500 dark:text-gray-400">Nessuna issue</p>
						{:else}
							<div class="space-y-3">
								{#each recentIssues as issue (issue.id)}
									<a href="/issues/{issue.id}" class="block p-3 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors border border-gray-100 dark:border-gray-800">
										<div class="flex items-start justify-between gap-2">
											<p class="text-sm font-medium text-gray-900 dark:text-gray-100 line-clamp-1 transition-colors">{issue.title}</p>
											<Badge variant="status" value={issue.status} />
										</div>
										<div class="flex items-center gap-2 mt-1.5">
											<Badge variant="type" value={issue.type} />
											<Badge variant="priority" value={issue.priority} />
											<span class="text-xs text-gray-500 dark:text-gray-400 ml-auto transition-colors">{formatDate(issue.createdAt)}</span>
										</div>
									</a>
								{/each}
							</div>
						{/if}
					</div>

					<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-5 border border-gray-100 dark:border-gray-800 transition-colors">
						<h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-4 transition-colors">Le Mie Issue</h2>
						{#if myIssues.length === 0}
							<p class="text-sm text-gray-500 dark:text-gray-400">Nessuna issue creata</p>
						{:else}
							<div class="space-y-3">
								{#each myIssues.slice(0, 5) as issue (issue.id)}
									<a href="/issues/{issue.id}" class="block p-3 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors border border-gray-100 dark:border-gray-800">
										<div class="flex items-start justify-between gap-2">
											<p class="text-sm font-medium text-gray-900 dark:text-gray-100 line-clamp-1 transition-colors">{issue.title}</p>
											<Badge variant="status" value={issue.status} />
										</div>
										<div class="flex items-center gap-2 mt-1.5">
											<Badge variant="type" value={issue.type} />
											<Badge variant="priority" value={issue.priority} />
										</div>
									</a>
								{/each}
								{#if myIssues.length > 5}
									<a href="/issues" class="block text-center text-sm text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 py-2 transition-colors">
										Vedi tutte ({myIssues.length})
									</a>
								{/if}
							</div>
						{/if}
					</div>

					<ActivityTimeline {allIssues} />
				</div>
			{/if}

		{:else if activeTab === 'utenti'}
			<UserManagementTab />
		{/if}
	</div>
</div>
