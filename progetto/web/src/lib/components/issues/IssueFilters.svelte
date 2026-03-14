<script lang="ts">
	import type { IssueType, IssuePriority, IssueStatus, User } from '$lib/types';

	interface Props {
		search: string;
		selectedTypes: IssueType[];
		selectedPriorities: IssuePriority[];
		selectedStatuses: IssueStatus[];
		showArchived: boolean;
		showDeleted: boolean;
		sortBy: string;
		currentUser: User | null;
		onchange: () => void;
	}

	let {
		search = $bindable(),
		selectedTypes = $bindable(),
		selectedPriorities = $bindable(),
		selectedStatuses = $bindable(),
		showArchived = $bindable(),
		showDeleted = $bindable(),
		sortBy = $bindable(),
		currentUser = null,
		onchange
	}: Props = $props();

	const issueTypes: IssueType[] = ['QUESTION', 'BUG', 'DOCUMENTATION', 'FEATURE'];
	const issuePriorities: IssuePriority[] = ['BASSA', 'MEDIA', 'ALTA', 'CRITICA'];
	const issueStatuses: IssueStatus[] = ['TODO', 'IN_PROGRESS', 'RISOLTA'];

	const typeLabels: Record<IssueType, string> = {
		QUESTION: 'Question', BUG: 'Bug', DOCUMENTATION: 'Docs', FEATURE: 'Feature'
	};
	const priorityLabels: Record<IssuePriority, string> = {
		BASSA: 'Bassa', MEDIA: 'Media', ALTA: 'Alta', CRITICA: 'Critica'
	};
	const statusLabels: Record<IssueStatus, string> = {
		TODO: 'Todo', IN_PROGRESS: 'In Progress', RISOLTA: 'Risolta'
	};

	function toggleFilter<T>(array: T[], value: T): T[] {
		const index = array.indexOf(value);
		if (index >= 0) return array.filter((v) => v !== value);
		return [...array, value];
	}
</script>

<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-4 mb-6 border border-gray-200 dark:border-gray-800 transition-colors">
	<div class="space-y-4">
		<div class="flex flex-col gap-3">
			<div class="flex flex-col sm:flex-row gap-3">
				<div class="flex-1">
					<label for="filter-search" class="sr-only">Cerca issue</label>
					<input
						type="text"
						id="filter-search"
						bind:value={search}
						oninput={onchange}
						placeholder="Cerca issue..."
						class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
					/>
				</div>
				<div>
					<label for="filter-sort" class="sr-only">Ordina per</label>
					<select
						id="filter-sort"
						bind:value={sortBy}
						onchange={onchange}
						class="w-full sm:w-auto px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent min-w-[160px] text-gray-900 dark:text-gray-100 transition-colors"
					>
						<option value="createdAt">Più recenti</option>
						<option value="priority">Per priorità</option>
						<option value="status">Per stato</option>
					</select>
				</div>
			</div>
			<div class="flex flex-wrap gap-2">
				<label class="flex items-center gap-2 px-3 py-1.5 border border-gray-300 dark:border-gray-700 rounded-lg text-sm cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
					<input
						type="checkbox"
						bind:checked={showArchived}
						onchange={onchange}
						class="w-4 h-4 text-blue-600 border-gray-300 dark:border-gray-600 rounded focus:ring-blue-500 bg-white dark:bg-gray-700"
					/>
					<span class="text-gray-700 dark:text-gray-300">Archiviate</span>
				</label>
				{#if currentUser?.role === 'ADMIN'}
					<label class="flex items-center gap-2 px-3 py-1.5 border border-gray-300 dark:border-gray-700 rounded-lg text-sm cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
						<input
							type="checkbox"
							bind:checked={showDeleted}
							onchange={onchange}
							class="w-4 h-4 text-red-600 border-gray-300 dark:border-gray-600 rounded focus:ring-red-500 bg-white dark:bg-gray-700"
						/>
						<span class="text-gray-700 dark:text-gray-300">Eliminate</span>
					</label>
				{/if}
			</div>
		</div>

		<div class="grid grid-cols-1 md:grid-cols-3 gap-4">
			<fieldset>
				<legend class="text-xs font-medium text-gray-700 dark:text-gray-400 mb-2">Tipo</legend>
				<div class="flex flex-wrap gap-2">
					{#each issueTypes as type}
						<button
							onclick={() => { selectedTypes = toggleFilter(selectedTypes, type); onchange(); }}
							aria-pressed={selectedTypes.includes(type)}
							class="px-3 py-1 text-xs rounded-full transition-all {selectedTypes.includes(type) ? 'bg-blue-600 text-white' : 'bg-gray-200 dark:bg-gray-800 text-gray-700 dark:text-gray-300'}"
						>
							{typeLabels[type]}
						</button>
					{/each}
				</div>
			</fieldset>

			<fieldset>
				<legend class="text-xs font-medium text-gray-700 dark:text-gray-400 mb-2">Priorità</legend>
				<div class="flex flex-wrap gap-2">
					{#each issuePriorities as priority}
						<button
							onclick={() => { selectedPriorities = toggleFilter(selectedPriorities, priority); onchange(); }}
							aria-pressed={selectedPriorities.includes(priority)}
							class="px-3 py-1 text-xs rounded-full transition-all {selectedPriorities.includes(priority) ? 'bg-blue-600 text-white' : 'bg-gray-200 dark:bg-gray-800 text-gray-700 dark:text-gray-300'}"
						>
							{priorityLabels[priority]}
						</button>
					{/each}
				</div>
			</fieldset>

			<fieldset>
				<legend class="text-xs font-medium text-gray-700 dark:text-gray-400 mb-2">Stato</legend>
				<div class="flex flex-wrap gap-2">
					{#each issueStatuses as status}
						<button
							onclick={() => { selectedStatuses = toggleFilter(selectedStatuses, status); onchange(); }}
							aria-pressed={selectedStatuses.includes(status)}
							class="px-3 py-1 text-xs rounded-full transition-all {selectedStatuses.includes(status) ? 'bg-blue-600 text-white' : 'bg-gray-200 dark:bg-gray-800 text-gray-700 dark:text-gray-300'}"
						>
							{statusLabels[status]}
						</button>
					{/each}
				</div>
			</fieldset>
		</div>
	</div>
</div>
