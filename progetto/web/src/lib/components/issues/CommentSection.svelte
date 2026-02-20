<script lang="ts">
	import type { Comment, User } from '$lib/types';
	import { formatDateTime } from '$lib/utils/dates';

	interface Props {
		comments: Comment[];
		currentUser: User | null;
		canComment: boolean;
		onAdd: (content: string) => void;
		onEdit: (commentId: string, content: string) => void;
		onDelete: (commentId: string) => void;
	}

	let { comments, currentUser, canComment, onAdd, onEdit, onDelete }: Props = $props();

	let newContent = $state('');
	let editingId = $state<string | null>(null);
	let editingContent = $state('');

	function handleAdd() {
		if (!newContent.trim()) return;
		onAdd(newContent);
		newContent = '';
	}

	function startEdit(comment: Comment) {
		editingId = comment.id;
		editingContent = comment.content;
	}

	function saveEdit(id: string) {
		if (!editingContent.trim()) return;
		onEdit(id, editingContent);
		editingId = null;
		editingContent = '';
	}
</script>

<div class="bg-white rounded-xl shadow-sm p-6">
	<h2 class="text-2xl font-bold text-gray-900 mb-6">Commenti ({comments.length})</h2>

	{#if comments.length === 0}
		<div class="text-center py-8 text-gray-500">
			<svg class="w-12 h-12 mx-auto mb-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
			</svg>
			<p>Nessun commento</p>
		</div>
	{:else}
		<div class="space-y-4 mb-6">
			{#each comments as comment (comment.id)}
				<div class="border border-gray-200 rounded-lg p-4">
					<div class="flex items-start gap-3">
						<div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
							<span class="text-blue-700 font-semibold text-sm">{comment.author.name.charAt(0)}</span>
						</div>
						<div class="flex-1 min-w-0">
							<div class="flex items-start justify-between mb-2">
								<div>
									<p class="font-medium text-gray-900">{comment.author.name}</p>
									<p class="text-xs text-gray-500">
										{formatDateTime(comment.createdAt)}
										{#if comment.updatedAt}
											<span class="ml-2 text-orange-600">(modificato)</span>
										{/if}
									</p>
								</div>
								{#if currentUser?.id === comment.author.id}
									<div class="flex gap-2">
										<button onclick={() => startEdit(comment)} class="text-blue-600 hover:text-blue-700 text-sm">Modifica</button>
										<button onclick={() => onDelete(comment.id)} class="text-red-600 hover:text-red-700 text-sm">Elimina</button>
									</div>
								{/if}
							</div>

							{#if editingId === comment.id}
								<div class="space-y-2">
									<textarea bind:value={editingContent} rows="3"
										class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"></textarea>
									<div class="flex gap-2">
										<button onclick={() => saveEdit(comment.id)} class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg">Salva</button>
										<button onclick={() => { editingId = null; }} class="px-3 py-1 bg-gray-200 hover:bg-gray-300 text-gray-700 text-sm rounded-lg">Annulla</button>
									</div>
								</div>
							{:else}
								<p class="text-gray-700 whitespace-pre-wrap">{comment.content}</p>
							{/if}
						</div>
					</div>
				</div>
			{/each}
		</div>
	{/if}

	{#if canComment}
		<div class="pt-6 border-t border-gray-200">
			<h3 class="text-lg font-semibold text-gray-900 mb-3">Aggiungi Commento</h3>
			<form onsubmit={(e) => { e.preventDefault(); handleAdd(); }}>
				<textarea bind:value={newContent} rows="4" placeholder="Scrivi un commento..."
					class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none mb-3"></textarea>
				<div class="flex justify-end">
					<button type="submit" disabled={!newContent.trim()}
						class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
						Aggiungi Commento
					</button>
				</div>
			</form>
		</div>
	{/if}
</div>
