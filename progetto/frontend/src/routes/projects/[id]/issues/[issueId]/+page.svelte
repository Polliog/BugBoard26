<script lang="ts">
	import { page } from '$app/stores';
	import { authStore } from '$lib/stores/auth.svelte';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { toast } from 'svelte-sonner';
	import Navbar from '$lib/components/Navbar.svelte';
	import Modal from '$lib/components/Modal.svelte';
	import Badge from '$lib/components/Badge.svelte';
	import { ProjectsService } from '$lib/services/projects';
	import { IssuesService } from '$lib/services/issues';
	import { CommentsService } from '$lib/services/comments';
	import { mockUsers, initializeMockData } from '$lib/services/mockData';
	import type { Project, Issue, Comment, IssueStatus } from '$lib/types';

	let projectId = $derived($page.params.id);
	let issueId = $derived($page.params.issueId);
	let project = $state<Project | null>(null);
	let issue = $state<Issue | null>(null);
	let comments = $state<Comment[]>([]);
	let userRole = $state<'admin' | 'assigned' | 'external' | 'none'>('none');

	// Commenti
	let newCommentContent = $state('');
	let editingCommentId = $state<string | null>(null);
	let editingContent = $state('');

	// Modal modifica issue
	let isEditModalOpen = $state(false);

	onMount(() => {
		if (!authStore.isAuthenticated) {
			goto('/login');
			return;
		}

		initializeMockData();
		loadData();
	});

	function loadData() {
		if (!authStore.user) return;

		project = ProjectsService.getProjectById(projectId);
		if (!project) {
			goto('/projects');
			return;
		}

		if (!ProjectsService.userHasAccess(projectId, authStore.user)) {
			goto('/projects');
			return;
		}

		if (authStore.isAdmin) {
			userRole = 'admin';
		} else {
			const member = project.members.find((m) => m.userId === authStore.user!.id);
			userRole = member?.role || 'none';
		}

		issue = IssuesService.getIssueById(issueId);
		if (!issue) {
			goto(`/projects/${projectId}/issues`);
			return;
		}

		loadComments();
	}

	function loadComments() {
		comments = CommentsService.getCommentsByIssue(issueId);
	}

	function handleAddComment() {
		if (!newCommentContent.trim() || !authStore.user) return;

		CommentsService.createComment(issueId, authStore.user.id, newCommentContent);
		toast.success('Commento aggiunto!');
		newCommentContent = '';
		loadComments();
	}

	function startEditComment(comment: Comment) {
		editingCommentId = comment.id;
		editingContent = comment.content;
	}

	function cancelEditComment() {
		editingCommentId = null;
		editingContent = '';
	}

	function saveEditComment(commentId: string) {
		if (!editingContent.trim()) return;

		CommentsService.updateComment(commentId, editingContent);
		toast.success('Commento modificato!');
		editingCommentId = null;
		editingContent = '';
		loadComments();
	}

	function deleteComment(commentId: string) {
		if (!confirm('Sei sicuro di voler eliminare questo commento?')) return;

		CommentsService.deleteComment(commentId);
		toast.success('Commento eliminato!');
		loadComments();
	}

	function changeStatus(newStatus: IssueStatus) {
		if (!issue) return;

		IssuesService.updateIssue(issueId, { status: newStatus });
		toast.success('Stato aggiornato!', {
			description: `Nuovo stato: ${newStatus}`
		});
		issue = IssuesService.getIssueById(issueId);
	}

	function archiveIssue() {
		if (!confirm('Sei sicuro di voler archiviare questa issue?') || !authStore.user) return;

		IssuesService.archiveIssue(issueId, authStore.user.id);
		toast.success('Issue archiviata!');
		goto(`/projects/${projectId}/issues`);
	}

	function getUserName(userId: string): string {
		return mockUsers.find((u) => u.id === userId)?.name || 'Unknown';
	}

	function canEdit(): boolean {
		return userRole === 'admin' || userRole === 'assigned';
	}

	function canComment(): boolean {
		return userRole === 'admin' || userRole === 'assigned';
	}

	function isCommentAuthor(comment: Comment): boolean {
		return authStore.user?.id === comment.userId;
	}

	function formatDate(date: Date): string {
		return new Date(date).toLocaleString('it-IT', {
			day: '2-digit',
			month: 'long',
			year: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
	}
</script>

<svelte:head>
	<title>{issue?.title || 'Issue'} - BugBoard26</title>
</svelte:head>

<div class="min-h-screen bg-gray-50">
	<Navbar />

	<div class="max-w-5xl mx-auto px-4 py-8">
		<!-- Breadcrumb -->
		<div class="mb-6">
			<nav class="flex items-center gap-2 text-sm text-gray-600">
				<a href="/projects" class="hover:text-blue-600 transition-colors">Progetti</a>
				<span>/</span>
				<a
					href="/projects/{projectId}/issues"
					class="hover:text-blue-600 transition-colors"
				>
					{project?.name}
				</a>
				<span>/</span>
				<span class="text-gray-900 font-medium">Issue #{issue?.id.split('-')[1]}</span>
			</nav>
		</div>

		{#if issue}
			<!-- Header Issue -->
			<div class="bg-white rounded-xl shadow-sm p-6 mb-6">
				<div class="flex items-start justify-between mb-4">
					<h1 class="text-3xl font-bold text-gray-900 flex-1">{issue.title}</h1>
					<div class="flex gap-2 flex-shrink-0">
						{#if canEdit()}
							<button
								onclick={archiveIssue}
								class="px-4 py-2 border border-orange-300 text-orange-700 rounded-lg hover:bg-orange-50 transition-colors text-sm"
							>
								Archivia
							</button>
						{/if}
					</div>
				</div>

				<!-- Badges -->
				<div class="flex flex-wrap gap-2 mb-6">
					<Badge variant="type" value={issue.type} />
					<Badge variant="priority" value={issue.priority} />
					<Badge variant="status" value={issue.status} />
				</div>

				<!-- Cambio stato rapido -->
				{#if canEdit()}
					<div class="mb-6">
						<p class="text-sm font-medium text-gray-700 mb-2">Cambia stato:</p>
						<div class="flex flex-wrap gap-2">
							{#each ['Aperta', 'In Progress', 'Risolta', 'Chiusa'] as status}
								<button
									onclick={() => changeStatus(status)}
									class="px-3 py-1 text-sm rounded-lg transition-colors"
									class:bg-blue-600={issue.status === status}
									class:text-white={issue.status === status}
									class:bg-gray-200={issue.status !== status}
									class:text-gray-700={issue.status !== status}
									class:hover:bg-gray-300={issue.status !== status}
								>
									{status}
								</button>
							{/each}
						</div>
					</div>
				{/if}

				<!-- Descrizione -->
				<div class="mb-6">
					<h2 class="text-lg font-semibold text-gray-900 mb-3">Descrizione</h2>
					<p class="text-gray-700 whitespace-pre-wrap">{issue.description}</p>
				</div>

				<!-- Immagine -->
				{#if issue.image}
					<div class="mb-6">
						<h2 class="text-lg font-semibold text-gray-900 mb-3">Allegato</h2>
						<img
							src={issue.image}
							alt="Allegato issue: {issue.title}"
							class="max-w-full rounded-lg border border-gray-200"
						/>
					</div>
				{/if}

				<!-- Metadata -->
				<div class="pt-6 border-t border-gray-200">
					<div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
						<div>
							<p class="text-gray-500 mb-1">Creato da</p>
							<p class="font-medium text-gray-900">{getUserName(issue.createdBy)}</p>
						</div>
						<div>
							<p class="text-gray-500 mb-1">Data creazione</p>
							<p class="font-medium text-gray-900">
								{new Date(issue.createdAt).toLocaleDateString('it-IT')}
							</p>
						</div>
						<div>
							<p class="text-gray-500 mb-1">Assegnato a</p>
							<p class="font-medium text-gray-900">
								{issue.assignedTo ? getUserName(issue.assignedTo) : 'Nessuno'}
							</p>
						</div>
						{#if issue.updatedAt}
							<div>
								<p class="text-gray-500 mb-1">Ultimo aggiornamento</p>
								<p class="font-medium text-gray-900">
									{new Date(issue.updatedAt).toLocaleDateString('it-IT')}
								</p>
							</div>
						{/if}
					</div>
				</div>
			</div>

			<!-- Sezione Commenti -->
			<div class="bg-white rounded-xl shadow-sm p-6">
				<h2 class="text-2xl font-bold text-gray-900 mb-6">
					Commenti ({comments.length})
				</h2>

				<!-- Lista Commenti -->
				{#if comments.length === 0}
					<div class="text-center py-8 text-gray-500">
						<svg
							class="w-12 h-12 mx-auto mb-3 text-gray-400"
							fill="none"
							stroke="currentColor"
							viewBox="0 0 24 24"
						>
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
							></path>
						</svg>
						<p>Nessun commento ancora. Sii il primo a commentare!</p>
					</div>
				{:else}
					<div class="space-y-4 mb-6">
						{#each comments as comment (comment.id)}
							<div class="border border-gray-200 rounded-lg p-4">
								<div class="flex items-start gap-3">
									<!-- Avatar -->
									<div
										class="w-10 h-10 min-w-10 min-h-10 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0"
									>
										<span class="text-blue-700 font-semibold text-sm">
											{getUserName(comment.userId).charAt(0)}
										</span>
									</div>

									<!-- Contenuto -->
									<div class="flex-1 min-w-0">
										<div class="flex items-start justify-between mb-2">
											<div>
												<p class="font-medium text-gray-900">
													{getUserName(comment.userId)}
												</p>
												<p class="text-xs text-gray-500">
													{formatDate(comment.createdAt)}
													{#if comment.updatedAt}
														<span class="ml-2 text-orange-600">(modificato)</span>
													{/if}
												</p>
											</div>

											<!-- Azioni (solo autore) -->
											{#if isCommentAuthor(comment)}
												<div class="flex gap-2">
													<button
														onclick={() => startEditComment(comment)}
														aria-label="Modifica commento"
														class="text-blue-600 hover:text-blue-700 text-sm"
													>
														Modifica
													</button>
													<button
														onclick={() => deleteComment(comment.id)}
														aria-label="Elimina commento"
														class="text-red-600 hover:text-red-700 text-sm"
													>
														Elimina
													</button>
												</div>
											{/if}
										</div>

										<!-- Contenuto commento -->
										{#if editingCommentId === comment.id}
											<div class="space-y-2">
												<textarea
													bind:value={editingContent}
													rows="3"
													class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
												></textarea>
												<div class="flex gap-2">
													<button
														onclick={() => saveEditComment(comment.id)}
														class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg"
													>
														Salva
													</button>
													<button
														onclick={cancelEditComment}
														class="px-3 py-1 bg-gray-200 hover:bg-gray-300 text-gray-700 text-sm rounded-lg"
													>
														Annulla
													</button>
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

				<!-- Form Nuovo Commento -->
				{#if canComment()}
					<div class="pt-6 border-t border-gray-200">
						<h3 class="text-lg font-semibold text-gray-900 mb-3">Aggiungi Commento</h3>
						<form
							onsubmit={(e) => {
								e.preventDefault();
								handleAddComment();
							}}
						>
							<textarea
								bind:value={newCommentContent}
								rows="4"
								placeholder="Scrivi un commento..."
								class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none mb-3"
							></textarea>
							<div class="flex justify-end">
								<button
									type="submit"
									disabled={!newCommentContent.trim()}
									class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
								>
									Aggiungi Commento
								</button>
							</div>
						</form>
					</div>
				{/if}
			</div>
		{/if}
	</div>
</div>
