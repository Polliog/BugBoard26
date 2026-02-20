<script lang="ts">
	import type { Issue } from '$lib/types';
	import Badge from '$lib/components/ui/Badge.svelte';
	import { formatDate } from '$lib/utils/dates';

	interface Props {
		issue: Issue;
		onclick: () => void;
	}

	let { issue, onclick }: Props = $props();
</script>

<button
	{onclick}
	class="w-full bg-white rounded-xl shadow-sm hover:shadow-md transition-all p-4 text-left border border-gray-200 hover:border-blue-300"
>
	<div class="flex items-start gap-4">
		<div class="flex-shrink-0 w-12 h-12">
			{#if issue.image}
				<img src={issue.image} alt="" class="w-full h-full rounded-lg object-cover" />
			{:else}
				<div class="w-full h-full rounded-lg bg-gray-100 flex items-center justify-center">
					<svg class="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
					</svg>
				</div>
			{/if}
		</div>

		<div class="flex-1 min-w-0">
			<div class="flex items-start justify-between gap-4 mb-2">
				<h3 class="text-lg font-semibold text-gray-900 line-clamp-1">{issue.title}</h3>
				<div class="flex gap-2 flex-shrink-0">
					<Badge variant="type" value={issue.type} />
					<Badge variant="priority" value={issue.priority} />
					<Badge variant="status" value={issue.status} />
				</div>
			</div>
			<p class="text-sm text-gray-600 mb-3 line-clamp-2">{issue.description}</p>
			<div class="flex items-center gap-4 text-xs text-gray-500">
				<span>{formatDate(issue.createdAt)}</span>
				{#if issue.assignedTo}
					<span class="flex items-center gap-1">
						<svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
							<path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"></path>
						</svg>
						{issue.assignedTo.name}
					</span>
				{/if}
				{#if issue.archived}
					<span class="text-orange-600 font-medium">Archiviata</span>
				{/if}
			</div>
		</div>
	</div>
</button>
