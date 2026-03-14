<script lang="ts">
	import type { Comment, User } from '$lib/types';
	import { formatDateTime } from '$lib/utils/dates';
	import Markdown from '$lib/components/ui/Markdown.svelte';

	const MAX_IMAGE_SIZE = 5 * 1024 * 1024;
	const ALLOWED_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];

	interface Props {
		comments: Comment[];
		currentUser: User | null;
		canComment: boolean;
		onAdd: (content: string, image?: string | null) => void;
		onEdit: (commentId: string, content: string, image?: string | null) => void;
		onDelete: (commentId: string) => void;
	}

	let { comments, currentUser, canComment, onAdd, onEdit, onDelete }: Props = $props();

	let newContent = $state('');
	let newImage = $state<string | null>(null);
	let editingId = $state<string | null>(null);
	let editingContent = $state('');
	let editingImage = $state<string | null>(null);
	let editingImageChanged = $state(false);

	function handleFileSelect(event: Event, target: 'new' | 'edit') {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		if (!file) return;

		if (!ALLOWED_TYPES.includes(file.type)) {
			alert('Formato non supportato. Usa PNG, JPG, GIF o WebP.');
			input.value = '';
			return;
		}
		if (file.size > MAX_IMAGE_SIZE) {
			alert('Immagine troppo grande. Massimo 5MB.');
			input.value = '';
			return;
		}

		const reader = new FileReader();
		reader.onload = () => {
			if (target === 'new') {
				newImage = reader.result as string;
			} else {
				editingImage = reader.result as string;
				editingImageChanged = true;
			}
		};
		reader.readAsDataURL(file);
	}

	function handleAdd() {
		if (!newContent.trim()) return;
		onAdd(newContent, newImage);
		newContent = '';
		newImage = null;
	}

	function startEdit(comment: Comment) {
		editingId = comment.id;
		editingContent = comment.content;
		editingImage = comment.image;
		editingImageChanged = false;
	}

	function saveEdit(id: string) {
		if (!editingContent.trim()) return;
		onEdit(id, editingContent, editingImageChanged ? (editingImage ?? '') : undefined);
		editingId = null;
		editingContent = '';
		editingImage = null;
		editingImageChanged = false;
	}
</script>

<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-4 sm:p-6 transition-colors">
	<h2 class="text-xl sm:text-2xl font-bold text-gray-900 dark:text-gray-100 mb-4 sm:mb-6 transition-colors">Commenti ({comments.length})</h2>

	{#if comments.length === 0}
		<div class="text-center py-8 text-gray-500 dark:text-gray-400">
			<svg class="w-12 h-12 mx-auto mb-3 text-gray-400 dark:text-gray-600" aria-hidden="true" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
			</svg>
			<p>Nessun commento</p>
		</div>
	{:else}
		<div class="space-y-4 mb-6">
			{#each comments as comment (comment.id)}
				<div class="border border-gray-200 dark:border-gray-800 rounded-lg p-3 sm:p-4 transition-colors">
					<div class="flex items-start gap-2 sm:gap-3">
						<div class="w-8 h-8 sm:w-10 sm:h-10 bg-blue-100 dark:bg-blue-900/50 rounded-full flex items-center justify-center flex-shrink-0 transition-colors">
							<span class="text-blue-700 dark:text-blue-300 font-semibold text-xs sm:text-sm transition-colors">{comment.author.name.charAt(0)}</span>
						</div>
						<div class="flex-1 min-w-0">
							<div class="flex items-start justify-between mb-2">
								<div>
									<p class="font-medium text-gray-900 dark:text-gray-100 text-sm sm:text-base transition-colors">{comment.author.name}</p>
									<p class="text-xs text-gray-500 dark:text-gray-400 transition-colors">
										{formatDateTime(comment.createdAt)}
										{#if comment.updatedAt && comment.updatedAt !== comment.createdAt}
											<span class="ml-2 text-orange-600 dark:text-orange-400">(modificato)</span>
										{/if}
									</p>
								</div>
								{#if currentUser?.id === comment.author.id || currentUser?.role === 'ADMIN'}
									<div class="flex gap-2">
										{#if currentUser?.id === comment.author.id}
											<button onclick={() => startEdit(comment)} class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm transition-colors">Modifica</button>
										{/if}
										<button onclick={() => onDelete(comment.id)} class="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 text-sm transition-colors">Elimina</button>
									</div>
								{/if}
							</div>

							{#if editingId === comment.id}
								<div class="space-y-2">
									<label for="edit-comment-{comment.id}" class="sr-only">Modifica commento</label>
									<textarea id="edit-comment-{comment.id}" bind:value={editingContent} rows="3"
										class="w-full px-3 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors resize-none"></textarea>

									{#if editingImage}
										<div class="relative inline-block">
											<img src={editingImage} alt="Allegato" class="max-h-40 rounded-lg border border-gray-200 dark:border-gray-700" />
											<button onclick={() => { editingImage = null; editingImageChanged = true; }}
												class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center text-xs hover:bg-red-600" aria-label="Rimuovi immagine">
												&times;
											</button>
										</div>
									{:else}
										<label class="inline-flex items-center gap-2 px-3 py-1.5 text-sm text-gray-600 dark:text-gray-400 border border-gray-300 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors">
											<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
											Immagine
											<input type="file" accept="image/png,image/jpeg,image/gif,image/webp" class="hidden" onchange={(e) => handleFileSelect(e, 'edit')} />
										</label>
									{/if}

									<div class="flex gap-2">
										<button onclick={() => saveEdit(comment.id)} class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg transition-colors">Salva</button>
										<button onclick={() => { editingId = null; }} class="px-3 py-1 bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 text-sm rounded-lg transition-colors">Annulla</button>
									</div>
								</div>
							{:else}
								<Markdown content={comment.content} />
								{#if comment.image}
									<img src={comment.image} alt="Allegato commento" class="mt-2 max-w-full max-h-80 rounded-lg border border-gray-200 dark:border-gray-700" />
								{/if}
							{/if}
						</div>
					</div>
				</div>
			{/each}
		</div>
	{/if}

	{#if canComment}
		<div class="pt-4 sm:pt-6 border-t border-gray-200 dark:border-gray-800 transition-colors">
			<h3 class="text-base sm:text-lg font-semibold text-gray-900 dark:text-gray-100 mb-3 transition-colors">Aggiungi Commento</h3>
			<form onsubmit={(e) => { e.preventDefault(); handleAdd(); }}>
				<label for="new-comment" class="sr-only">Scrivi un commento</label>
				<textarea id="new-comment" bind:value={newContent} rows="3" placeholder="Scrivi un commento..."
					class="w-full px-3 py-2 sm:px-4 sm:py-3 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors resize-none mb-3"></textarea>

				{#if newImage}
					<div class="relative inline-block mb-3">
						<img src={newImage} alt="Anteprima allegato" class="max-h-40 rounded-lg border border-gray-200 dark:border-gray-700" />
						<button type="button" onclick={() => { newImage = null; }}
							class="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center text-xs hover:bg-red-600" aria-label="Rimuovi immagine">
							&times;
						</button>
					</div>
				{/if}

				<div class="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-2">
					<label class="inline-flex items-center justify-center gap-2 px-3 py-2 text-sm text-gray-600 dark:text-gray-400 border border-gray-300 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors">
						<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
						Allega immagine
						<input type="file" accept="image/png,image/jpeg,image/gif,image/webp" class="hidden" onchange={(e) => handleFileSelect(e, 'new')} />
					</label>
					<button type="submit" disabled={!newContent.trim()}
						class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed text-sm sm:text-base">
						Aggiungi Commento
					</button>
				</div>
			</form>
		</div>
	{/if}
</div>
