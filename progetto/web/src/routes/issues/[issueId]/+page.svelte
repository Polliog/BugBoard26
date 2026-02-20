<script lang="ts">
	import { page } from '$app/stores';
	import { authStore } from '$lib/stores/auth.svelte';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { toast } from 'svelte-sonner';
	import Navbar from '$lib/components/Navbar.svelte';
	import Spinner from '$lib/components/ui/Spinner.svelte';
	import Badge from '$lib/components/ui/Badge.svelte';
	import ConfirmDialog from '$lib/components/ui/ConfirmDialog.svelte';
	import Markdown from '$lib/components/ui/Markdown.svelte';
	import CommentSection from '$lib/components/issues/CommentSection.svelte';
	import { issuesApi } from '$lib/api/issues.api';
	import { commentsApi } from '$lib/api/comments.api';
	import { can } from '$lib/utils/permissions';
	import { formatDate } from '$lib/utils/dates';
	import type { Issue, Comment, IssueStatus } from '$lib/types';

	const MAX_IMAGE_SIZE = 5 * 1024 * 1024;
	const ALLOWED_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp'];

	let issueId = $derived($page.params.issueId!);
	let issue = $state<Issue | null>(null);
	let comments = $state<Comment[]>([]);
	let loading = $state(true);
	let confirmDialog = $state<{ open: boolean; title: string; message: string; variant: 'danger' | 'warning' | 'default'; action: () => void }>({
		open: false, title: '', message: '', variant: 'default', action: () => {}
	});

	const statuses: { value: IssueStatus; label: string }[] = [
		{ value: 'TODO', label: 'Todo' },
		{ value: 'IN_PROGRESS', label: 'In Progress' },
		{ value: 'RISOLTA', label: 'Risolta' }
	];

	onMount(() => loadData());

	async function loadData() {
		loading = true;
		try {
			issue = await issuesApi.getById(issueId);
			comments = await commentsApi.getByIssue(issueId);
		} catch {
			toast.error('Issue non trovata');
			goto('/issues');
		} finally {
			loading = false;
		}
	}

	async function changeStatus(status: IssueStatus) {
		try {
			issue = await issuesApi.update(issueId, { status });
			toast.success(`Stato aggiornato: ${status}`);
		} catch {
			toast.error('Errore aggiornamento stato');
		}
	}

	function showConfirm(title: string, message: string, variant: 'danger' | 'warning' | 'default', action: () => void) {
		confirmDialog = { open: true, title, message, variant, action };
	}

	function archiveIssue() {
		showConfirm('Archivia Issue', 'Sei sicuro di voler archiviare questa issue?', 'warning', async () => {
			try {
				await issuesApi.update(issueId, { archived: true });
				toast.success('Issue archiviata');
				goto('/issues');
			} catch {
				toast.error('Errore archiviazione');
			}
		});
	}

	function deleteIssue() {
		showConfirm('Elimina Issue', 'Sei sicuro di voler eliminare questa issue? Potrà essere ripristinata in seguito.', 'danger', async () => {
			try {
				await issuesApi.delete(issueId);
				toast.success('Issue eliminata');
				goto('/issues');
			} catch {
				toast.error('Errore eliminazione');
			}
		});
	}

	async function restoreIssue() {
		try {
			issue = await issuesApi.restore(issueId);
			toast.success('Issue ripristinata');
		} catch {
			toast.error('Errore ripristino');
		}
	}

	function handleIssueImageSelect(event: Event) {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];
		if (!file) return;

		if (!ALLOWED_TYPES.includes(file.type)) {
			toast.error('Formato non supportato. Usa PNG, JPG, GIF o WebP.');
			input.value = '';
			return;
		}
		if (file.size > MAX_IMAGE_SIZE) {
			toast.error('Immagine troppo grande. Massimo 5MB.');
			input.value = '';
			return;
		}

		const reader = new FileReader();
		reader.onload = async () => {
			try {
				issue = await issuesApi.update(issueId, { image: reader.result as string });
				toast.success('Immagine aggiornata');
			} catch {
				toast.error('Errore aggiornamento immagine');
			}
		};
		reader.readAsDataURL(file);
	}

	async function removeIssueImage() {
		try {
			issue = await issuesApi.update(issueId, { image: '' });
			toast.success('Immagine rimossa');
		} catch {
			toast.error('Errore rimozione immagine');
		}
	}

	async function handleAddComment(content: string, image?: string | null) {
		try {
			await commentsApi.create(issueId, content, image);
			toast.success('Commento aggiunto');
			comments = await commentsApi.getByIssue(issueId);
		} catch {
			toast.error('Errore aggiunta commento');
		}
	}

	async function handleEditComment(commentId: string, content: string, image?: string | null) {
		try {
			await commentsApi.update(issueId, commentId, content, image);
			toast.success('Commento modificato');
			comments = await commentsApi.getByIssue(issueId);
		} catch {
			toast.error('Errore modifica commento');
		}
	}

	function handleDeleteComment(commentId: string) {
		showConfirm('Elimina Commento', 'Sei sicuro di voler eliminare questo commento?', 'danger', async () => {
			try {
				await commentsApi.delete(issueId, commentId);
				toast.success('Commento eliminato');
				comments = await commentsApi.getByIssue(issueId);
			} catch {
				toast.error('Errore eliminazione commento');
			}
		});
	}
</script>

<svelte:head>
	<title>{issue?.title || 'Issue'} - BugBoard26</title>
</svelte:head>

<div class="min-h-screen bg-gray-50">
	<Navbar />

	<div class="max-w-5xl mx-auto px-4 py-8">
		<div class="mb-6">
			<nav aria-label="Breadcrumb" class="text-sm text-gray-600">
				<ol class="flex items-center gap-2">
					<li><a href="/issues" class="hover:text-blue-600 transition-colors">Issue</a></li>
					<li aria-hidden="true">/</li>
					<li aria-current="page" class="text-gray-900 font-medium">{issue?.title ?? '...'}</li>
				</ol>
			</nav>
		</div>

		{#if loading}
			<div class="flex justify-center py-12"><Spinner size="lg" /></div>
		{:else if issue}
			{#if issue.deletedAt}
				<div class="bg-red-50 border border-red-200 rounded-xl p-4 mb-6 flex items-center justify-between">
					<div class="flex items-center gap-2 text-red-800">
						<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
						</svg>
						<span class="font-medium">Questa issue è stata eliminata{issue.deletedBy ? ` da ${issue.deletedBy.name}` : ''}</span>
					</div>
					{#if can(authStore.user, 'delete:issue')}
						<button onclick={restoreIssue}
							class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors text-sm font-medium">
							Ripristina
						</button>
					{/if}
				</div>
			{/if}

			<div class="bg-white rounded-xl shadow-sm p-6 mb-6">
				<div class="flex items-start justify-between mb-4">
					<h1 class="text-3xl font-bold text-gray-900 flex-1">{issue.title}</h1>
					<div class="flex gap-2">
						{#if !issue.deletedAt && can(authStore.user, 'archive')}
							<button onclick={archiveIssue}
								class="px-4 py-2 border border-orange-300 text-orange-700 rounded-lg hover:bg-orange-50 transition-colors text-sm">
								Archivia
							</button>
						{/if}
						{#if !issue.deletedAt && can(authStore.user, 'delete:issue')}
							<button onclick={deleteIssue}
								class="px-4 py-2 border border-red-300 text-red-700 rounded-lg hover:bg-red-50 transition-colors text-sm">
								Elimina
							</button>
						{/if}
					</div>
				</div>

				<div class="flex flex-wrap gap-2 mb-6">
					<Badge variant="type" value={issue.type} />
					<Badge variant="priority" value={issue.priority} />
					<Badge variant="status" value={issue.status} />
				</div>

				{#if !issue.deletedAt && can(authStore.user, 'change:status', issue)}
					<div class="mb-6">
						<p class="text-sm font-medium text-gray-700 mb-2">Cambia stato:</p>
						<div class="flex flex-wrap gap-2">
							{#each statuses as s}
								<button onclick={() => changeStatus(s.value)}
									aria-pressed={issue.status === s.value}
									class="px-3 py-1 text-sm rounded-lg transition-colors"
									class:bg-blue-600={issue.status === s.value}
									class:text-white={issue.status === s.value}
									class:bg-gray-200={issue.status !== s.value}
									class:text-gray-700={issue.status !== s.value}
									class:hover:bg-gray-300={issue.status !== s.value}>
									{s.label}
								</button>
							{/each}
						</div>
					</div>
				{/if}

				<div class="mb-6">
					<h2 class="text-lg font-semibold text-gray-900 mb-3">Descrizione</h2>
					<Markdown content={issue.description} />
				</div>

				<div class="mb-6">
					<div class="flex items-center gap-3 mb-3">
						<h2 class="text-lg font-semibold text-gray-900">Allegato</h2>
						{#if !issue.deletedAt && can(authStore.user, 'change:status', issue)}
							{#if issue.image}
								<button onclick={removeIssueImage} class="text-red-600 hover:text-red-700 text-sm">Rimuovi</button>
							{/if}
							<label class="inline-flex items-center gap-1 px-2 py-1 text-sm text-blue-600 hover:text-blue-700 cursor-pointer">
								{issue.image ? 'Cambia' : 'Aggiungi'}
								<input type="file" accept="image/png,image/jpeg,image/gif,image/webp" class="hidden" onchange={handleIssueImageSelect} />
							</label>
						{/if}
					</div>
					{#if issue.image}
						<img src={issue.image} alt="Allegato issue: {issue.title}" class="max-w-full rounded-lg border border-gray-200" />
					{:else}
						<p class="text-gray-400 text-sm">Nessun allegato</p>
					{/if}
				</div>

				<div class="pt-6 border-t border-gray-200">
					<div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
						<div>
							<p class="text-gray-500 mb-1">Creato da</p>
							<p class="font-medium text-gray-900">{issue.createdBy.name}</p>
						</div>
						<div>
							<p class="text-gray-500 mb-1">Data creazione</p>
							<p class="font-medium text-gray-900">{formatDate(issue.createdAt)}</p>
						</div>
						<div>
							<p class="text-gray-500 mb-1">Assegnato a</p>
							<p class="font-medium text-gray-900">{issue.assignedTo?.name ?? 'Nessuno'}</p>
						</div>
						{#if issue.updatedAt}
							<div>
								<p class="text-gray-500 mb-1">Ultimo aggiornamento</p>
								<p class="font-medium text-gray-900">{formatDate(issue.updatedAt)}</p>
							</div>
						{/if}
					</div>
				</div>
			</div>

			<CommentSection
				{comments}
				currentUser={authStore.user}
				canComment={!issue.deletedAt && can(authStore.user, 'comment')}
				onAdd={handleAddComment}
				onEdit={handleEditComment}
				onDelete={handleDeleteComment}
			/>
		{/if}
	</div>
</div>

<ConfirmDialog
	isOpen={confirmDialog.open}
	title={confirmDialog.title}
	message={confirmDialog.message}
	variant={confirmDialog.variant}
	onConfirm={() => { confirmDialog.open = false; confirmDialog.action(); }}
	onCancel={() => { confirmDialog.open = false; }}
/>
