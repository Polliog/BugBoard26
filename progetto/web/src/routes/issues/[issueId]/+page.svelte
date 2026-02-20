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

	async function handleAddComment(content: string) {
		try {
			await commentsApi.create(issueId, content);
			toast.success('Commento aggiunto');
			comments = await commentsApi.getByIssue(issueId);
		} catch {
			toast.error('Errore aggiunta commento');
		}
	}

	async function handleEditComment(commentId: string, content: string) {
		try {
			await commentsApi.update(issueId, commentId, content);
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
			<div class="bg-white rounded-xl shadow-sm p-6 mb-6">
				<div class="flex items-start justify-between mb-4">
					<h1 class="text-3xl font-bold text-gray-900 flex-1">{issue.title}</h1>
					{#if can(authStore.user, 'archive')}
						<button onclick={archiveIssue}
							class="px-4 py-2 border border-orange-300 text-orange-700 rounded-lg hover:bg-orange-50 transition-colors text-sm">
							Archivia
						</button>
					{/if}
				</div>

				<div class="flex flex-wrap gap-2 mb-6">
					<Badge variant="type" value={issue.type} />
					<Badge variant="priority" value={issue.priority} />
					<Badge variant="status" value={issue.status} />
				</div>

				{#if can(authStore.user, 'change:status', issue)}
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

				{#if issue.image}
					<div class="mb-6">
						<h2 class="text-lg font-semibold text-gray-900 mb-3">Allegato</h2>
						<img src={issue.image} alt="Allegato issue: {issue.title}" class="max-w-full rounded-lg border border-gray-200" />
					</div>
				{/if}

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
				canComment={can(authStore.user, 'comment')}
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
