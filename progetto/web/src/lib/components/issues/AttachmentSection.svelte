<script lang="ts">
	import { toast } from 'svelte-sonner';
	import { attachmentsApi } from '$lib/api/attachments.api';
	import type { Attachment } from '$lib/types';

	let {
		issueId,
		attachments = $bindable(),
		canModify,
		onUpdate
	}: {
		issueId: string;
		attachments: Attachment[];
		canModify: boolean;
		onUpdate: () => void;
	} = $props();

	let uploading = $state(false);

	const MAX_SIZE = 5 * 1024 * 1024;
	const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'application/pdf'];

	function formatSize(bytes: number): string {
		if (bytes < 1024) return bytes + ' B';
		if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
		return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
	}

	function getFileIcon(contentType: string): string {
		if (contentType === 'application/pdf') return 'PDF';
		if (contentType.startsWith('image/')) return 'IMG';
		return 'FILE';
	}

	async function handleFileSelect(event: Event) {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		if (!file) return;
		input.value = '';

		if (!ALLOWED_TYPES.includes(file.type)) {
			toast.error('Tipo file non ammesso. Usa JPG, PNG, GIF, WebP o PDF.');
			return;
		}
		if (file.size > MAX_SIZE) {
			toast.error('File troppo grande. Massimo 5MB.');
			return;
		}

		uploading = true;
		try {
			await attachmentsApi.upload(issueId, file);
			toast.success('Allegato caricato');
			onUpdate();
		} catch (e) {
			toast.error(e instanceof Error ? e.message : 'Errore upload');
		} finally {
			uploading = false;
		}
	}

	async function handleDelete(storedFilename: string, originalFilename: string) {
		try {
			await attachmentsApi.delete(issueId, storedFilename);
			toast.success(`Allegato "${originalFilename}" rimosso`);
			onUpdate();
		} catch {
			toast.error('Errore rimozione allegato');
		}
	}
</script>

<div class="mb-6">
	<div class="flex items-center gap-3 mb-3">
		<h2 class="text-lg font-semibold text-gray-900 dark:text-gray-100 transition-colors">Allegati</h2>
		{#if canModify}
			<label
				class="inline-flex items-center gap-1 px-3 py-1 text-sm bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 hover:bg-blue-100 dark:hover:bg-blue-900/40 rounded-lg cursor-pointer transition-colors {uploading
					? 'opacity-50 pointer-events-none'
					: ''}"
			>
				{uploading ? 'Caricamento...' : 'Aggiungi'}
				<input
					type="file"
					accept=".jpg,.jpeg,.png,.gif,.webp,.pdf"
					class="hidden"
					onchange={handleFileSelect}
					disabled={uploading}
				/>
			</label>
		{/if}
	</div>

	{#if attachments.length === 0}
		<p class="text-gray-400 dark:text-gray-600 text-sm transition-colors">Nessun allegato</p>
	{:else}
		<div class="space-y-2">
			{#each attachments as att}
				<div
					class="flex items-center justify-between gap-3 p-3 bg-gray-50 dark:bg-gray-800/50 rounded-lg border border-gray-200 dark:border-gray-700 transition-colors"
				>
					<div class="flex items-center gap-3 min-w-0">
						<span
							class="flex-shrink-0 w-10 h-10 rounded-lg bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 flex items-center justify-center text-xs font-bold"
						>
							{getFileIcon(att.contentType)}
						</span>
						<div class="min-w-0">
							<a
								href={attachmentsApi.getDownloadUrl(issueId, att.storedFilename)}
								target="_blank"
								rel="noopener noreferrer"
								class="text-sm font-medium text-blue-600 dark:text-blue-400 hover:underline truncate block"
							>
								{att.originalFilename}
							</a>
							<p class="text-xs text-gray-500 dark:text-gray-400">{formatSize(att.fileSize)}</p>
						</div>
					</div>
					{#if canModify}
						<button
							onclick={() => handleDelete(att.storedFilename, att.originalFilename)}
							class="flex-shrink-0 p-1 text-red-500 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 transition-colors"
							aria-label="Rimuovi allegato {att.originalFilename}"
						>
							<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
								/>
							</svg>
						</button>
					{/if}
				</div>
			{/each}
		</div>
	{/if}
</div>
