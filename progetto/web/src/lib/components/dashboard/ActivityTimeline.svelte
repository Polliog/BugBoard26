<script lang="ts">
	import type { Issue, HistoryEntry } from '$lib/types';

	type TimelineEntry = HistoryEntry & { issueId: string; issueTitle: string };

	interface Props {
		allIssues: Issue[];
	}

	let { allIssues }: Props = $props();

	let feed = $derived<TimelineEntry[]>(
		allIssues
			.flatMap((i) =>
				i.history.map((h) => ({
					...h,
					issueId: i.id,
					issueTitle: i.title
				}))
			)
			.sort((a, b) => b.timestamp.localeCompare(a.timestamp))
			.slice(0, 10)
	);

	function formatTimeAgo(dateStr: string): string {
		const diff = Date.now() - new Date(dateStr).getTime();
		const minutes = Math.floor(diff / 60000);
		if (minutes < 1) return 'ora';
		if (minutes < 60) return `${minutes}m fa`;
		const hours = Math.floor(minutes / 60);
		if (hours < 24) return `${hours}h fa`;
		const days = Math.floor(hours / 24);
		return `${days}g fa`;
	}
</script>

<div class="bg-white rounded-xl shadow-sm p-5">
	<h2 class="text-lg font-semibold text-gray-900 mb-4">Attività Recente</h2>
	{#if feed.length === 0}
		<div class="text-center py-6">
			<svg class="w-10 h-10 text-gray-300 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
			</svg>
			<p class="text-sm text-gray-500">Nessuna attività recente</p>
		</div>
	{:else}
		<div class="space-y-0">
			{#each feed as entry (entry.id)}
				<div class="flex items-start gap-3 py-2.5 border-b border-gray-100 last:border-0">
					<div class="w-7 h-7 bg-indigo-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
						<span class="text-indigo-700 font-semibold text-xs">{entry.performedBy.name.charAt(0)}</span>
					</div>
					<div class="flex-1 min-w-0">
						<p class="text-sm text-gray-900">
							<span class="font-medium">{entry.performedBy.name}</span>
							<span class="text-gray-500">&mdash;</span>
							{entry.action}
						</p>
						<a href="/issues/{entry.issueId}" class="text-xs text-blue-600 hover:underline truncate block mt-0.5">
							{entry.issueTitle}
						</a>
					</div>
					<span class="text-xs text-gray-400 flex-shrink-0 mt-0.5">{formatTimeAgo(entry.timestamp)}</span>
				</div>
			{/each}
		</div>
	{/if}
</div>
