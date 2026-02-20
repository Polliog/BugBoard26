<script lang="ts">
	import type { IssueType, IssuePriority, IssueStatus, User } from '$lib/types';
	import UserSearch from '$lib/components/ui/UserSearch.svelte';

	interface Props {
		search: string;
		selectedTypes: IssueType[];
		selectedPriorities: IssuePriority[];
		selectedStatuses: IssueStatus[];
		selectedAssignee: string;
		showArchived: boolean;
		sortBy: string;
		users: User[];
		currentUser: User | null;
		onchange: () => void;
	}

	let {
		search = $bindable(),
		selectedTypes = $bindable(),
		selectedPriorities = $bindable(),
		selectedStatuses = $bindable(),
		selectedAssignee = $bindable(),
		showArchived = $bindable(),
		sortBy = $bindable(),
		users,
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

<div class="bg-white rounded-xl shadow-sm p-4 mb-6">
	<div class="space-y-4">
		<div class="flex flex-col md:flex-row gap-4">
			<div class="flex-1">
				<label for="filter-search" class="sr-only">Cerca issue</label>
				<input
					type="text"
					id="filter-search"
					bind:value={search}
					oninput={onchange}
					placeholder="Cerca issue..."
					class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
				/>
			</div>
			<div class="flex gap-2">
				<label for="filter-sort" class="sr-only">Ordina per</label>
				<select
					id="filter-sort"
					bind:value={sortBy}
					onchange={onchange}
					class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
				>
					<option value="createdAt">Più recenti</option>
					<option value="priority">Per priorità</option>
					<option value="status">Per stato</option>
				</select>
				<label class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg">
					<input
						type="checkbox"
						bind:checked={showArchived}
						onchange={onchange}
						class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
					/>
					<span class="text-sm text-gray-700">Archiviate</span>
				</label>
			</div>
		</div>

		<div class="grid grid-cols-1 md:grid-cols-4 gap-4">
			<fieldset>
				<legend class="text-xs font-medium text-gray-700 mb-2">Tipo</legend>
				<div class="flex flex-wrap gap-2">
					{#each issueTypes as type}
						<button
							onclick={() => { selectedTypes = toggleFilter(selectedTypes, type); onchange(); }}
							aria-pressed={selectedTypes.includes(type)}
							class="px-3 py-1 text-xs rounded-full transition-colors"
							class:bg-blue-600={selectedTypes.includes(type)}
							class:text-white={selectedTypes.includes(type)}
							class:bg-gray-200={!selectedTypes.includes(type)}
							class:text-gray-700={!selectedTypes.includes(type)}
						>
							{typeLabels[type]}
						</button>
					{/each}
				</div>
			</fieldset>

			<fieldset>
				<legend class="text-xs font-medium text-gray-700 mb-2">Priorità</legend>
				<div class="flex flex-wrap gap-2">
					{#each issuePriorities as priority}
						<button
							onclick={() => { selectedPriorities = toggleFilter(selectedPriorities, priority); onchange(); }}
							aria-pressed={selectedPriorities.includes(priority)}
							class="px-3 py-1 text-xs rounded-full transition-colors"
							class:bg-blue-600={selectedPriorities.includes(priority)}
							class:text-white={selectedPriorities.includes(priority)}
							class:bg-gray-200={!selectedPriorities.includes(priority)}
							class:text-gray-700={!selectedPriorities.includes(priority)}
						>
							{priorityLabels[priority]}
						</button>
					{/each}
				</div>
			</fieldset>

			<fieldset>
				<legend class="text-xs font-medium text-gray-700 mb-2">Stato</legend>
				<div class="flex flex-wrap gap-2">
					{#each issueStatuses as status}
						<button
							onclick={() => { selectedStatuses = toggleFilter(selectedStatuses, status); onchange(); }}
							aria-pressed={selectedStatuses.includes(status)}
							class="px-3 py-1 text-xs rounded-full transition-colors"
							class:bg-blue-600={selectedStatuses.includes(status)}
							class:text-white={selectedStatuses.includes(status)}
							class:bg-gray-200={!selectedStatuses.includes(status)}
							class:text-gray-700={!selectedStatuses.includes(status)}
						>
							{statusLabels[status]}
						</button>
					{/each}
				</div>
			</fieldset>

			<div>
				<span class="text-xs font-medium text-gray-700 mb-2 block">Assegnato a</span>
				<UserSearch
					{users}
					selectedId={selectedAssignee}
					{currentUser}
					placeholder="Cerca utente..."
					onchange={(id) => { selectedAssignee = id; onchange(); }}
				/>
			</div>
		</div>
	</div>
</div>
