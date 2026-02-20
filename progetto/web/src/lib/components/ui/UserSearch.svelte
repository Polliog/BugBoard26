<script lang="ts">
	import type { User } from '$lib/types';

	interface Props {
		users: User[];
		selectedId: string;
		currentUser?: User | null;
		placeholder?: string;
		onchange: (userId: string) => void;
	}

	let { users, selectedId, currentUser = null, placeholder = 'Cerca utente...', onchange }: Props = $props();

	let query = $state('');
	let isOpen = $state(false);

	let selectedUser = $derived(users.find((u) => u.id === selectedId));

	let filtered = $derived(() => {
		if (!query.trim()) {
			if (currentUser) {
				const rest = users.filter((u) => u.id !== currentUser.id);
				return [currentUser, ...rest];
			}
			return users;
		}
		const q = query.toLowerCase();
		return users.filter(
			(u) => u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q)
		);
	});

	function select(user: User) {
		onchange(user.id);
		query = '';
		isOpen = false;
	}

	function clear() {
		onchange('');
		query = '';
		isOpen = false;
	}

	function handleFocus() {
		isOpen = true;
	}

	function handleBlur() {
		setTimeout(() => { isOpen = false; }, 150);
	}
</script>

<div class="relative">
	{#if selectedUser && !isOpen}
		<div class="flex items-center gap-2 px-3 py-1.5 border border-gray-300 rounded-lg bg-white">
			<div class="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
				<span class="text-blue-700 font-semibold text-xs">{selectedUser.name.charAt(0)}</span>
			</div>
			<span class="text-sm text-gray-900 flex-1">{selectedUser.name}</span>
			<button type="button" onclick={clear} class="text-gray-400 hover:text-gray-600" aria-label="Rimuovi selezione">
				<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
				</svg>
			</button>
		</div>
	{:else}
		<input
			type="text"
			bind:value={query}
			onfocus={handleFocus}
			onblur={handleBlur}
			{placeholder}
			class="w-full px-3 py-1.5 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
		/>
	{/if}

	{#if isOpen}
		<div class="absolute z-10 mt-1 w-full bg-white rounded-lg shadow-lg border border-gray-200 max-h-48 overflow-y-auto">
			<button
				type="button"
				onclick={clear}
				class="w-full text-left px-3 py-2 text-sm text-gray-500 hover:bg-gray-50"
			>
				Nessuno
			</button>
			{#each filtered() as user (user.id)}
				<button
					type="button"
					onclick={() => select(user)}
					class="w-full text-left px-3 py-2 text-sm hover:bg-blue-50 flex items-center gap-2"
					class:bg-blue-50={user.id === selectedId}
				>
					<div class="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
						<span class="text-blue-700 font-semibold text-xs">{user.name.charAt(0)}</span>
					</div>
					<div>
						<span class="text-gray-900">{user.name}</span>
						{#if currentUser && user.id === currentUser.id}
							<span class="ml-1 text-xs text-blue-600">(tu)</span>
						{/if}
						<span class="block text-xs text-gray-500">{user.email}</span>
					</div>
				</button>
			{/each}
			{#if filtered().length === 0}
				<p class="px-3 py-2 text-sm text-gray-500">Nessun utente trovato</p>
			{/if}
		</div>
	{/if}
</div>
