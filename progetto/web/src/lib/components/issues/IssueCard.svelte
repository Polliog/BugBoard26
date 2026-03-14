<script lang="ts">
	import type { Issue } from '$lib/types';
	import Badge from '$lib/components/ui/Badge.svelte';
	import { formatDate } from '$lib/utils/dates';

	interface Props {
		issue: Issue;
		href: string;
	}

	let { issue, href }: Props = $props();
</script>

<a
	{href}
	class="block w-full bg-white dark:bg-gray-900 rounded-xl shadow-sm hover:shadow-md transition-all p-4 border border-gray-200 dark:border-gray-800 hover:border-blue-300 dark:hover:border-blue-700 transition-colors"
>
	<div class="flex items-start gap-3 sm:gap-4">
		<div class="flex-shrink-0 w-10 h-10 sm:w-12 sm:h-12 hidden sm:block">
			{#if issue.image}
				<img src={issue.image} alt="Allegato: {issue.title}" class="w-full h-full rounded-lg object-cover border border-gray-200 dark:border-gray-700 transition-colors" />
			{:else}
				<div class="w-full h-full rounded-lg bg-gray-100 dark:bg-gray-800 flex items-center justify-center transition-colors">
					<svg class="w-5 h-5 sm:w-6 sm:h-6 text-gray-400 dark:text-gray-500" aria-hidden="true" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
					</svg>
				</div>
			{/if}
		</div>

		<div class="flex-1 min-w-0">
			<h3 class="text-base sm:text-lg font-semibold text-gray-900 dark:text-gray-100 line-clamp-1 mb-1.5 transition-colors">{issue.title}</h3>
			<div class="flex flex-wrap gap-1.5 mb-2">
				<Badge variant="type" value={issue.type} />
				<Badge variant="priority" value={issue.priority} />
				<Badge variant="status" value={issue.status} />
				{#if issue.archived}
					<span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-orange-100 dark:bg-orange-900/30 text-orange-700 dark:text-orange-400 transition-colors">
						<svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
						</svg>
						Archiviata
					</span>
				{/if}
			</div>
			<p class="text-sm text-gray-600 dark:text-gray-400 mb-2 line-clamp-2 hidden sm:block transition-colors">{issue.description}</p>
			<div class="flex items-center gap-3 text-xs text-gray-500 dark:text-gray-400 transition-colors">
				<span>{formatDate(issue.createdAt)}</span>
				<span class="flex items-center gap-1">
					<svg class="w-3.5 h-3.5" aria-hidden="true" fill="currentColor" viewBox="0 0 20 20">
						<path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"></path>
					</svg>
					{issue.createdBy.name}
				</span>
			</div>
		</div>
	</div>
</a>
