<script lang="ts">
	import type { IssueType, IssuePriority } from '$lib/types';
	import Modal from '$lib/components/ui/Modal.svelte';

	interface Props {
		isOpen: boolean;
		onClose: () => void;
		onSubmit: (data: {
			title: string;
			type: IssueType;
			description: string;
			priority: IssuePriority;
			image?: string;
		}, files?: File[]) => void;
	}

	let { isOpen, onClose, onSubmit }: Props = $props();

	let title = $state('');
	let type = $state<IssueType>('BUG');
	let description = $state('');
	let priority = $state<IssuePriority>('MEDIA');
	let image = $state('');
	let imagePreview = $state('');
	let pendingFiles = $state<File[]>([]);
	let errors = $state<Record<string, string>>({});

	const ALLOWED_ATTACHMENT_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'application/pdf'];
	const MAX_ATTACHMENT_SIZE = 5 * 1024 * 1024;

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
		image = '';
		imagePreview = '';
		pendingFiles = [];
		errors = {};
	}

	function handleAttachmentSelect(event: Event) {
		const input = event.target as HTMLInputElement;
		const files = input.files;
		if (!files) return;
		input.value = '';

		for (const file of files) {
			if (!ALLOWED_ATTACHMENT_TYPES.includes(file.type)) {
				errors.attachments = `"${file.name}": tipo non ammesso. Usa JPG, PNG, GIF, WebP o PDF.`;
				return;
			}
			if (file.size > MAX_ATTACHMENT_SIZE) {
				errors.attachments = `"${file.name}": troppo grande. Massimo 5MB.`;
				return;
			}
			if (pendingFiles.length >= 10) {
				errors.attachments = 'Massimo 10 allegati per issue.';
				return;
			}
			pendingFiles = [...pendingFiles, file];
		}
		errors.attachments = '';
	}

	function removeFile(index: number) {
		pendingFiles = pendingFiles.filter((_, i) => i !== index);
	}

	function formatSize(bytes: number): string {
		if (bytes < 1024) return bytes + ' B';
		if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
		return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
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
			image: image || undefined
		}, pendingFiles.length > 0 ? pendingFiles : undefined);
		reset();
		onClose();
	}
</script>

<Modal {isOpen} title="Nuova Issue" onClose={() => { reset(); onClose(); }}>
	<form onsubmit={(e) => { e.preventDefault(); handleSubmit(); }} class="space-y-6">
		<div>
			<label for="issue-title" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-2">Titolo *</label>
			<input type="text" id="issue-title" bind:value={title} maxlength="200"
				class="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
				class:border-red-500={errors.title} placeholder="Breve descrizione del problema" />
			{#if errors.title}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{errors.title}</p>{/if}
		</div>

		<div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
			<div>
				<label for="issue-type" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-2">Tipo *</label>
				<select id="issue-type" bind:value={type}
					class="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors">
					{#each issueTypes as t}<option value={t.value} class="dark:bg-gray-800">{t.label}</option>{/each}
				</select>
			</div>
			<div>
				<label for="issue-priority" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-2">Priorità</label>
				<select id="issue-priority" bind:value={priority}
					class="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors">
					{#each issuePriorities as p}<option value={p.value} class="dark:bg-gray-800">{p.label}</option>{/each}
				</select>
			</div>
		</div>

		<div>
			<label for="issue-desc" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-2">Descrizione *</label>
			<textarea id="issue-desc" bind:value={description} rows="6"
				class="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none text-gray-900 dark:text-gray-100 transition-colors"
				class:border-red-500={errors.description} placeholder="Descrivi in dettaglio..."></textarea>
			{#if errors.description}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{errors.description}</p>{/if}
		</div>

		<div>
			<label for="issue-image" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-2">Immagine</label>
			<input type="file" id="issue-image" accept="image/*" onchange={handleImageUpload}
				class="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg text-gray-900 dark:text-gray-100 transition-colors" />
			{#if errors.image}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{errors.image}</p>{/if}
			<p class="mt-1 text-xs text-gray-500 dark:text-gray-400">Max 5MB</p>
			{#if imagePreview}
				<div class="mt-4 relative">
					<img src={imagePreview} alt="Preview" class="w-full rounded-lg border border-gray-200 dark:border-gray-700 transition-colors" />
					<button type="button" onclick={() => { image = ''; imagePreview = ''; }}
						aria-label="Rimuovi" class="absolute top-2 right-2 bg-red-600 hover:bg-red-700 text-white rounded-full p-2 transition-colors">
						<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
						</svg>
					</button>
				</div>
			{/if}
		</div>

		<div>
			<label for="issue-attachments" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-2">Allegati</label>
			<input type="file" id="issue-attachments" accept=".jpg,.jpeg,.png,.gif,.webp,.pdf" multiple onchange={handleAttachmentSelect}
				class="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg text-gray-900 dark:text-gray-100 transition-colors" />
			{#if errors.attachments}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{errors.attachments}</p>{/if}
			<p class="mt-1 text-xs text-gray-500 dark:text-gray-400">JPG, PNG, GIF, WebP, PDF — max 5MB ciascuno, max 10 file</p>
			{#if pendingFiles.length > 0}
				<div class="mt-2 space-y-1">
					{#each pendingFiles as file, i}
						<div class="flex items-center justify-between gap-2 px-3 py-1.5 bg-gray-50 dark:bg-gray-800 rounded-lg text-sm">
							<span class="truncate text-gray-700 dark:text-gray-300">{file.name} <span class="text-gray-400">({formatSize(file.size)})</span></span>
							<button type="button" onclick={() => removeFile(i)} class="text-red-500 hover:text-red-700 dark:text-red-400 dark:hover:text-red-300 flex-shrink-0" aria-label="Rimuovi {file.name}">
								<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
								</svg>
							</button>
						</div>
					{/each}
				</div>
			{/if}
		</div>

		<div class="flex gap-3 justify-end pt-4 border-t border-gray-200 dark:border-gray-800 transition-colors">
			<button type="button" onclick={() => { reset(); onClose(); }}
				class="px-4 py-2 bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
				Annulla
			</button>
			<button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm">
				Crea Issue
			</button>
		</div>
	</form>
</Modal>
