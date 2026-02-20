<script lang="ts">
	import type { IssueType, IssuePriority, User } from '$lib/types';
	import Modal from '$lib/components/ui/Modal.svelte';
	import UserSearch from '$lib/components/ui/UserSearch.svelte';

	interface Props {
		isOpen: boolean;
		users: User[];
		currentUser: User | null;
		onClose: () => void;
		onSubmit: (data: {
			title: string;
			type: IssueType;
			description: string;
			priority: IssuePriority;
			assignedToId?: string;
			image?: string;
		}) => void;
	}

	let { isOpen, users, currentUser = null, onClose, onSubmit }: Props = $props();

	let title = $state('');
	let type = $state<IssueType>('BUG');
	let description = $state('');
	let priority = $state<IssuePriority>('MEDIA');
	let assignedToId = $state('');
	let image = $state('');
	let imagePreview = $state('');
	let errors = $state<Record<string, string>>({});

	const issueTypes: { value: IssueType; label: string }[] = [
		{ value: 'QUESTION', label: 'Question' },
		{ value: 'BUG', label: 'Bug' },
		{ value: 'DOCUMENTATION', label: 'Documentation' },
		{ value: 'FEATURE', label: 'Feature' }
	];

	const issuePriorities: { value: IssuePriority; label: string }[] = [
		{ value: 'BASSA', label: 'Bassa' },
		{ value: 'MEDIA', label: 'Media' },
		{ value: 'ALTA', label: 'Alta' },
		{ value: 'CRITICA', label: 'Critica' }
	];

	function reset() {
		title = '';
		type = 'BUG';
		description = '';
		priority = 'MEDIA';
		assignedToId = '';
		image = '';
		imagePreview = '';
		errors = {};
	}

	function validate(): boolean {
		errors = {};
		let valid = true;
		if (!title || title.length < 5) { errors.title = 'Minimo 5 caratteri'; valid = false; }
		if (title.length > 200) { errors.title = 'Massimo 200 caratteri'; valid = false; }
		if (!description || description.length < 20) { errors.description = 'Minimo 20 caratteri'; valid = false; }
		return valid;
	}

	function handleImageUpload(event: Event) {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		if (!file) return;
		if (file.size > 5 * 1024 * 1024) { errors.image = 'Max 5MB'; return; }
		if (!file.type.startsWith('image/')) { errors.image = 'Solo immagini'; return; }
		errors.image = '';
		const reader = new FileReader();
		reader.onload = (e) => {
			image = e.target?.result as string;
			imagePreview = image;
		};
		reader.readAsDataURL(file);
	}

	function handleSubmit() {
		if (!validate()) return;
		onSubmit({
			title, type, description, priority,
			assignedToId: assignedToId || undefined,
			image: image || undefined
		});
		reset();
		onClose();
	}
</script>

<Modal {isOpen} title="Nuova Issue" onClose={() => { reset(); onClose(); }}>
	<form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }} class="space-y-6">
		<div>
			<label for="issue-title" class="block text-sm font-medium text-gray-900 mb-2">Titolo *</label>
			<input type="text" id="issue-title" bind:value={title} maxlength="200"
				class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
				class:border-red-500={errors.title} placeholder="Breve descrizione del problema" />
			{#if errors.title}<p class="mt-1 text-sm text-red-600">{errors.title}</p>{/if}
		</div>

		<div class="grid grid-cols-2 gap-4">
			<div>
				<label for="issue-type" class="block text-sm font-medium text-gray-900 mb-2">Tipo *</label>
				<select id="issue-type" bind:value={type}
					class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
					{#each issueTypes as t}<option value={t.value}>{t.label}</option>{/each}
				</select>
			</div>
			<div>
				<label for="issue-priority" class="block text-sm font-medium text-gray-900 mb-2">Priorità</label>
				<select id="issue-priority" bind:value={priority}
					class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
					{#each issuePriorities as p}<option value={p.value}>{p.label}</option>{/each}
				</select>
			</div>
		</div>

		<div>
			<span class="block text-sm font-medium text-gray-900 mb-2">Assegnato a</span>
			<UserSearch
				{users}
				selectedId={assignedToId}
				{currentUser}
				placeholder="Cerca utente..."
				onchange={(id) => { assignedToId = id; }}
			/>
		</div>

		<div>
			<label for="issue-desc" class="block text-sm font-medium text-gray-900 mb-2">Descrizione *</label>
			<textarea id="issue-desc" bind:value={description} rows="6"
				class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
				class:border-red-500={errors.description} placeholder="Descrivi in dettaglio..."></textarea>
			{#if errors.description}<p class="mt-1 text-sm text-red-600">{errors.description}</p>{/if}
		</div>

		<div>
			<label for="issue-image" class="block text-sm font-medium text-gray-900 mb-2">Immagine</label>
			<input type="file" id="issue-image" accept="image/*" onchange={handleImageUpload}
				class="w-full px-4 py-2.5 border border-gray-300 rounded-lg" />
			{#if errors.image}<p class="mt-1 text-sm text-red-600">{errors.image}</p>{/if}
			<p class="mt-1 text-xs text-gray-500">Max 5MB</p>
			{#if imagePreview}
				<div class="mt-4 relative">
					<img src={imagePreview} alt="Preview" class="w-full rounded-lg" />
					<button type="button" onclick={() => { image = ''; imagePreview = ''; }}
						aria-label="Rimuovi" class="absolute top-2 right-2 bg-red-600 hover:bg-red-700 text-white rounded-full p-2">
						<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
						</svg>
					</button>
				</div>
			{/if}
		</div>

		<div class="flex gap-3 justify-end pt-4 border-t border-gray-200">
			<button type="button" onclick={() => { reset(); onClose(); }}
				class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
				Annulla
			</button>
			<button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors">
				Crea Issue
			</button>
		</div>
	</form>
</Modal>
