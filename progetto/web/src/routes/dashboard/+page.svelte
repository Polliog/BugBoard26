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
		TODO: 'bg-gray-400',
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
		TODO: 'bg-gray-100 text-gray-700',
		IN_PROGRESS: 'bg-blue-100 text-blue-800',
		RISOLTA: 'bg-green-100 text-green-800'
	};
	const priorityColors: Record<IssuePriority, string> = {
		BASSA: 'bg-blue-100 text-blue-800',
		MEDIA: 'bg-yellow-100 text-yellow-800',
		ALTA: 'bg-orange-100 text-orange-800',
		CRITICA: 'bg-red-100 text-red-800'
	};
	const typeColors: Record<IssueType, string> = {
		BUG: 'bg-red-100 text-red-800',
		FEATURE: 'bg-green-100 text-green-800',
		QUESTION: 'bg-purple-100 text-purple-800',
		DOCUMENTATION: 'bg-blue-100 text-blue-800'
	};

	let byStatus = $derived(countBy(allIssues, 'status'));
	let byPriority = $derived(countBy(allIssues, 'priority'));
	let byType = $derived(countBy(allIssues, 'type'));
	let myIssues = $derived(allIssues.filter((i) => i.assignedTo?.id === authStore.user?.id));
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

<div class="min-h-screen bg-gray-50">
	<Navbar />

	<div class="max-w-7xl mx-auto px-4 py-8">
		<div class="mb-6">
			<h1 class="text-3xl font-bold text-gray-900 mb-1">Dashboard</h1>
			<p class="text-gray-600">Panoramica delle issue del team</p>
		</div>

		<!-- Tab bar -->
		<div class="flex gap-1 mb-6 border-b border-gray-200">
			<button
				onclick={() => activeTab = 'panoramica'}
				class="px-4 py-2.5 text-sm font-medium transition-colors border-b-2 -mb-px
					{activeTab === 'panoramica'
						? 'border-blue-600 text-blue-600'
						: 'border-transparent text-gray-500 hover:text-gray-900'}"
			>
				Panoramica
			</button>
			{#if can(authStore.user, 'manage:users')}
				<button
					onclick={() => activeTab = 'utenti'}
					class="px-4 py-2.5 text-sm font-medium transition-colors border-b-2 -mb-px
						{activeTab === 'utenti'
							? 'border-blue-600 text-blue-600'
							: 'border-transparent text-gray-500 hover:text-gray-900'}"
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
				<div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
					<div class="bg-white rounded-xl shadow-sm p-5 flex items-start gap-4">
						<div class="p-2.5 bg-gray-100 rounded-lg flex-shrink-0">
							<svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
							</svg>
						</div>
						<div>
							<p class="text-3xl font-bold text-gray-900">{allIssues.length}</p>
							<p class="text-sm font-medium text-gray-600 mt-0.5">Totale Issue</p>
						</div>
					</div>

					<div class="bg-white rounded-xl shadow-sm p-5 flex items-start gap-4">
						<div class="p-2.5 bg-blue-100 rounded-lg flex-shrink-0">
							<svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
							</svg>
						</div>
						<div>
							<p class="text-3xl font-bold text-blue-600">{(byStatus['TODO'] || 0) + (byStatus['IN_PROGRESS'] || 0)}</p>
							<p class="text-sm font-medium text-gray-600 mt-0.5">Aperte</p>
							<p class="text-xs text-gray-400">TODO + In Progress</p>
						</div>
					</div>

					<div class="bg-white rounded-xl shadow-sm p-5 flex items-start gap-4">
						<div class="p-2.5 bg-green-100 rounded-lg flex-shrink-0">
							<svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
							</svg>
						</div>
						<div>
							<p class="text-3xl font-bold text-green-600">{byStatus['RISOLTA'] || 0}</p>
							<p class="text-sm font-medium text-gray-600 mt-0.5">Risolte</p>
							<p class="text-xs text-gray-400">Completate</p>
						</div>
					</div>

					<div class="bg-white rounded-xl shadow-sm p-5 flex items-start gap-4">
						<div class="p-2.5 bg-purple-100 rounded-lg flex-shrink-0">
							<svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path>
							</svg>
						</div>
						<div>
							<p class="text-3xl font-bold text-purple-600">{myIssues.length}</p>
							<p class="text-sm font-medium text-gray-600 mt-0.5">Assegnate a me</p>
							<p class="text-xs text-gray-400">In carico a te</p>
						</div>
					</div>
				</div>

				<!-- Breakdown panels -->
				<div class="grid md:grid-cols-3 gap-6 mb-8">
					<div class="bg-white rounded-xl shadow-sm p-5">
						<h2 class="text-lg font-semibold text-gray-900 mb-4">Per Stato</h2>
						<div class="space-y-3">
							{#each Object.entries(statusLabels) as [key, label]}
								{@const count = byStatus[key] || 0}
								{@const pct = allIssues.length ? Math.round((count / allIssues.length) * 100) : 0}
								<div>
									<div class="flex items-center justify-between mb-1">
										<span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium {statusColors[key as IssueStatus]}">{label}</span>
										<span class="text-sm font-medium text-gray-700">{count}</span>
									</div>
									<div class="w-full bg-gray-100 rounded-full h-2">
										<div class="h-2 rounded-full transition-all {statusBarColors[key as IssueStatus]}" style="width: {pct}%"></div>
									</div>
								</div>
							{/each}
						</div>
					</div>

					<div class="bg-white rounded-xl shadow-sm p-5">
						<h2 class="text-lg font-semibold text-gray-900 mb-4">Per Priorità</h2>
						<div class="space-y-3">
							{#each Object.entries(priorityLabels) as [key, label]}
								{@const count = byPriority[key] || 0}
								{@const pct = allIssues.length ? Math.round((count / allIssues.length) * 100) : 0}
								<div>
									<div class="flex items-center justify-between mb-1">
										<span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium {priorityColors[key as IssuePriority]}">{label}</span>
										<span class="text-sm font-medium text-gray-700">{count}</span>
									</div>
									<div class="w-full bg-gray-100 rounded-full h-2">
										<div class="h-2 rounded-full transition-all {priorityBarColors[key as IssuePriority]}" style="width: {pct}%"></div>
									</div>
								</div>
							{/each}
						</div>
					</div>

					<div class="bg-white rounded-xl shadow-sm p-5">
						<h2 class="text-lg font-semibold text-gray-900 mb-4">Per Tipo</h2>
						<div class="space-y-3">
							{#each Object.entries(typeLabels) as [key, label]}
								{@const count = byType[key] || 0}
								{@const pct = allIssues.length ? Math.round((count / allIssues.length) * 100) : 0}
								<div>
									<div class="flex items-center justify-between mb-1">
										<span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium {typeColors[key as IssueType]}">{label}</span>
										<span class="text-sm font-medium text-gray-700">{count}</span>
									</div>
									<div class="w-full bg-gray-100 rounded-full h-2">
										<div class="h-2 rounded-full transition-all {typeBarColors[key as IssueType]}" style="width: {pct}%"></div>
									</div>
								</div>
							{/each}
						</div>
					</div>
				</div>

				<!-- Bottom section: Recent Issues + My Issues + Activity Timeline -->
				<div class="grid md:grid-cols-3 gap-6">
					<div class="bg-white rounded-xl shadow-sm p-5">
						<h2 class="text-lg font-semibold text-gray-900 mb-4">Issue Recenti</h2>
						{#if recentIssues.length === 0}
							<p class="text-sm text-gray-500">Nessuna issue</p>
						{:else}
							<div class="space-y-3">
								{#each recentIssues as issue (issue.id)}
									<a href="/issues/{issue.id}" class="block p-3 rounded-lg hover:bg-gray-50 transition-colors border border-gray-100">
										<div class="flex items-start justify-between gap-2">
											<p class="text-sm font-medium text-gray-900 line-clamp-1">{issue.title}</p>
											<Badge variant="status" value={issue.status} />
										</div>
										<div class="flex items-center gap-2 mt-1.5">
											<Badge variant="type" value={issue.type} />
											<Badge variant="priority" value={issue.priority} />
											<span class="text-xs text-gray-500 ml-auto">{formatDate(issue.createdAt)}</span>
										</div>
									</a>
								{/each}
							</div>
						{/if}
					</div>

					<div class="bg-white rounded-xl shadow-sm p-5">
						<h2 class="text-lg font-semibold text-gray-900 mb-4">Le Mie Issue</h2>
						{#if myIssues.length === 0}
							<p class="text-sm text-gray-500">Nessuna issue assegnata</p>
						{:else}
							<div class="space-y-3">
								{#each myIssues.slice(0, 5) as issue (issue.id)}
									<a href="/issues/{issue.id}" class="block p-3 rounded-lg hover:bg-gray-50 transition-colors border border-gray-100">
										<div class="flex items-start justify-between gap-2">
											<p class="text-sm font-medium text-gray-900 line-clamp-1">{issue.title}</p>
											<Badge variant="status" value={issue.status} />
										</div>
										<div class="flex items-center gap-2 mt-1.5">
											<Badge variant="type" value={issue.type} />
											<Badge variant="priority" value={issue.priority} />
										</div>
									</a>
								{/each}
								{#if myIssues.length > 5}
									<a href="/issues?assignedToId={authStore.user?.id}" class="block text-center text-sm text-blue-600 hover:text-blue-700 py-2">
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
