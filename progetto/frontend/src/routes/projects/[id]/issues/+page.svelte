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
	import { mockUsers, initializeMockData } from '$lib/services/mockData';
	import type {
		Project,
		Issue,
		IssueType,
		IssuePriority,
		IssueStatus,
		ProjectMember
	} from '$lib/types';

	let projectId = $derived($page.params.id);
	let project = $state<Project | null>(null);
	let issues = $state<Issue[]>([]);
	let filteredIssues = $state<Issue[]>([]);
	let userRole = $state<'admin' | 'assigned' | 'external' | 'none'>('none');

	// Filtri
	let searchQuery = $state('');
	let selectedTypes = $state<IssueType[]>([]);
	let selectedPriorities = $state<IssuePriority[]>([]);
	let selectedStatuses = $state<IssueStatus[]>([]);
	let selectedAssignee = $state<string>('');
	let showArchived = $state(false);
	let sortBy = $state<'date' | 'priority' | 'status'>('date');

	// Modal creazione issue
	let isModalOpen = $state(false);
	let issueTitle = $state('');
	let issueType = $state<IssueType>('Bug');
	let issueDescription = $state('');
	let issuePriority = $state<IssuePriority>('Media');
	let issueStatus = $state<IssueStatus>('Aperta');
	let issueAssignedTo = $state<string>('');
	let issueImage = $state<string>('');
	let imagePreview = $state<string>('');
	let formErrors = $state<{ [key: string]: string }>({});

	const issueTypes: IssueType[] = ['Question', 'Bug', 'Documentation', 'Feature'];
	const issuePriorities: IssuePriority[] = ['Bassa', 'Media', 'Alta', 'Critica'];
	const issueStatuses: IssueStatus[] = ['Aperta', 'In Progress', 'Risolta', 'Chiusa'];

	onMount(() => {
		if (!authStore.isAuthenticated) {
			goto('/login');
			return;
		}

		initializeMockData();
		loadProject();
	});

	function loadProject() {
		if (!authStore.user) return;

		project = ProjectsService.getProjectById(projectId);
		if (!project) {
			goto('/projects');
			return;
		}

		// Verifica accesso
		if (!ProjectsService.userHasAccess(projectId, authStore.user)) {
			goto('/projects');
			return;
		}

		// Determina ruolo utente
		if (authStore.isAdmin) {
			userRole = 'admin';
		} else {
			const member = project.members.find((m) => m.userId === authStore.user!.id);
			userRole = member?.role || 'none';
		}

		loadIssues();
	}

	function loadIssues() {
		issues = IssuesService.getIssuesByProject(projectId, showArchived);
		applyFilters();
	}

	function applyFilters() {
		let result = [...issues];

		// Ricerca
		if (searchQuery) {
			result = result.filter(
				(i) =>
					i.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
					i.description.toLowerCase().includes(searchQuery.toLowerCase())
			);
		}

		// Filtro tipo
		if (selectedTypes.length > 0) {
			result = result.filter((i) => selectedTypes.includes(i.type));
		}

		// Filtro priorità
		if (selectedPriorities.length > 0) {
			result = result.filter((i) => selectedPriorities.includes(i.priority));
		}

		// Filtro stato
		if (selectedStatuses.length > 0) {
			result = result.filter((i) => selectedStatuses.includes(i.status));
		}

		// Filtro assegnato
		if (selectedAssignee) {
			result = result.filter((i) => i.assignedTo === selectedAssignee);
		}

		// Ordinamento
		result.sort((a, b) => {
			if (sortBy === 'date') {
				return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
			}
			if (sortBy === 'priority') {
				const priorityOrder = { Critica: 4, Alta: 3, Media: 2, Bassa: 1 };
				return priorityOrder[b.priority] - priorityOrder[a.priority];
			}
			if (sortBy === 'status') {
				return a.status.localeCompare(b.status);
			}
			return 0;
		});

		filteredIssues = result;
	}

	$effect(() => {
		applyFilters();
	});

	function openCreateModal() {
		isModalOpen = true;
		issueTitle = '';
		issueType = 'Bug';
		issueDescription = '';
		issuePriority = 'Media';
		issueStatus = 'Aperta';
		issueAssignedTo = '';
		issueImage = '';
		imagePreview = '';
		formErrors = {};
	}

	function closeModal() {
		isModalOpen = false;
	}

	function validateForm(): boolean {
		formErrors = {};
		let isValid = true;

		if (!issueTitle || issueTitle.length < 5) {
			formErrors.title = 'Titolo deve essere di almeno 5 caratteri';
			isValid = false;
		}

		if (issueTitle.length > 200) {
			formErrors.title = 'Titolo non può superare 200 caratteri';
			isValid = false;
		}

		if (!issueDescription || issueDescription.length < 20) {
			formErrors.description = 'Descrizione deve essere di almeno 20 caratteri';
			isValid = false;
		}

		return isValid;
	}

	function handleImageUpload(event: Event) {
		const input = event.target as HTMLInputElement;
		const file = input.files?.[0];

		if (!file) return;

		// Verifica dimensione (max 5MB)
		if (file.size > 5 * 1024 * 1024) {
			formErrors.image = 'Immagine troppo grande (max 5MB)';
			return;
		}

		// Verifica formato
		if (!file.type.startsWith('image/')) {
			formErrors.image = 'Formato non valido (solo immagini)';
			return;
		}

		formErrors.image = '';

		const reader = new FileReader();
		reader.onload = (e) => {
			const result = e.target?.result as string;
			issueImage = result;
			imagePreview = result;
		};
		reader.readAsDataURL(file);
	}

	function removeImage() {
		issueImage = '';
		imagePreview = '';
	}

	function handleCreateIssue() {
		if (!validateForm() || !authStore.user) return;

		IssuesService.createIssue(
			projectId,
			issueTitle,
			issueType,
			issueDescription,
			issuePriority,
			issueStatus,
			issueAssignedTo || undefined,
			authStore.user.id,
			issueImage || undefined
		);

		toast.success('Issue creata con successo!', {
			description: issueTitle
		});
		loadIssues();
		closeModal();
	}

	function toggleFilter(array: any[], value: any) {
		const index = array.indexOf(value);
		if (index >= 0) {
			return array.filter((v) => v !== value);
		} else {
			return [...array, value];
		}
	}

	function getUserName(userId: string): string {
		return mockUsers.find((u) => u.id === userId)?.name || 'Unassigned';
	}

	function getAssignableMembers(): ProjectMember[] {
		return project?.members.filter((m) => m.role === 'assigned') || [];
	}

	function canCreateIssue(): boolean {
		return userRole === 'admin' || userRole === 'assigned';
	}
</script>

<svelte:head>
	<title>{project?.name || 'Issue'} - BugBoard26</title>
</svelte:head>

<div class="min-h-screen bg-gray-50">
	<Navbar />

	<div class="max-w-7xl mx-auto px-4 py-8">
		<!-- Breadcrumb -->
		<div class="mb-6">
			<nav class="flex items-center gap-2 text-sm text-gray-600">
				<a href="/projects" class="hover:text-blue-600 transition-colors">Progetti</a>
				<span>/</span>
				<span class="text-gray-900 font-medium">{project?.name}</span>
			</nav>
		</div>

		<!-- Header -->
		<div class="mb-8">
			<div class="flex items-start justify-between mb-4">
				<div>
					<h1 class="text-3xl font-bold text-gray-900 mb-2">{project?.name}</h1>
					<p class="text-gray-600">{project?.description}</p>
				</div>
				{#if canCreateIssue()}
					<button
						onclick={openCreateModal}
						class="bg-blue-600 hover:bg-blue-700 text-white font-medium px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
					>
						<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M12 6v6m0 0v6m0-6h6m-6 0H6"
							></path>
						</svg>
						Crea Issue
					</button>
				{/if}
			</div>
		</div>

		<!-- Filtri -->
		<div class="bg-white rounded-xl shadow-sm p-4 mb-6">
			<div class="space-y-4">
				<!-- Ricerca e Ordinamento -->
				<div class="flex flex-col md:flex-row gap-4">
					<div class="flex-1">
						<input
							type="text"
							bind:value={searchQuery}
							placeholder="Cerca issue..."
							class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
						/>
					</div>
					<div class="flex gap-2">
						<select
							bind:value={sortBy}
							class="px-4 py-2 pr-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent min-w-[150px]"
						>
							<option value="date">Più recenti</option>
							<option value="priority">Per priorità</option>
							<option value="status">Per stato</option>
						</select>
						<label class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg">
							<input
								type="checkbox"
								bind:checked={showArchived}
								onchange={loadIssues}
								class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
							/>
							<span class="text-sm text-gray-700">Archiviate</span>
						</label>
					</div>
				</div>

				<!-- Filtri avanzati -->
				<div class="grid grid-cols-1 md:grid-cols-4 gap-4">
					<!-- Tipo -->
					<div>
						<p class="text-xs font-medium text-gray-700 mb-2">Tipo</p>
						<div class="flex flex-wrap gap-2">
							{#each issueTypes as type}
								<button
									onclick={() => (selectedTypes = toggleFilter(selectedTypes, type))}
									class="px-3 py-1 text-xs rounded-full transition-colors"
									class:bg-blue-600={selectedTypes.includes(type)}
									class:text-white={selectedTypes.includes(type)}
									class:bg-gray-200={!selectedTypes.includes(type)}
									class:text-gray-700={!selectedTypes.includes(type)}
								>
									{type}
								</button>
							{/each}
						</div>
					</div>

					<!-- Priorità -->
					<div>
						<p class="text-xs font-medium text-gray-700 mb-2">Priorità</p>
						<div class="flex flex-wrap gap-2">
							{#each issuePriorities as priority}
								<button
									onclick={() =>
										(selectedPriorities = toggleFilter(selectedPriorities, priority))}
									class="px-3 py-1 text-xs rounded-full transition-colors"
									class:bg-blue-600={selectedPriorities.includes(priority)}
									class:text-white={selectedPriorities.includes(priority)}
									class:bg-gray-200={!selectedPriorities.includes(priority)}
									class:text-gray-700={!selectedPriorities.includes(priority)}
								>
									{priority}
								</button>
							{/each}
						</div>
					</div>

					<!-- Stato -->
					<div>
						<p class="text-xs font-medium text-gray-700 mb-2">Stato</p>
						<div class="flex flex-wrap gap-2">
							{#each issueStatuses as status}
								<button
									onclick={() => (selectedStatuses = toggleFilter(selectedStatuses, status))}
									class="px-3 py-1 text-xs rounded-full transition-colors"
									class:bg-blue-600={selectedStatuses.includes(status)}
									class:text-white={selectedStatuses.includes(status)}
									class:bg-gray-200={!selectedStatuses.includes(status)}
									class:text-gray-700={!selectedStatuses.includes(status)}
								>
									{status}
								</button>
							{/each}
						</div>
					</div>

					<!-- Assegnato -->
					<div>
						<p class="text-xs font-medium text-gray-700 mb-2">Assegnato a</p>
						<select
							bind:value={selectedAssignee}
							class="w-full px-3 py-1.5 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
						>
							<option value="">Tutti</option>
							{#each getAssignableMembers() as member}
								<option value={member.userId}>{getUserName(member.userId)}</option>
							{/each}
						</select>
					</div>
				</div>
			</div>
		</div>

		<!-- Lista Issue -->
		{#if filteredIssues.length === 0}
			<div class="bg-white rounded-xl shadow-sm p-12 text-center">
				<svg
					class="w-16 h-16 text-gray-400 mx-auto mb-4"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
					></path>
				</svg>
				<h3 class="text-lg font-medium text-gray-900 mb-2">Nessuna issue trovata</h3>
				<p class="text-gray-600">
					{searchQuery || selectedTypes.length > 0
						? 'Prova a modificare i filtri'
						: 'Non ci sono issue in questo progetto'}
				</p>
			</div>
		{:else}
			<div class="space-y-3">
				{#each filteredIssues as issue (issue.id)}
					<button
						onclick={() => goto(`/projects/${projectId}/issues/${issue.id}`)}
						class="w-full bg-white rounded-xl shadow-sm hover:shadow-md transition-all p-4 text-left border border-gray-200 hover:border-blue-300"
					>
						<div class="flex items-start gap-4">
							<!-- Icona immagine -->
							<div class="flex-shrink-0 w-12 h-12 min-w-12 min-h-12">
								{#if issue.image}
									<div
										class="w-full h-full rounded-lg bg-gray-100 flex items-center justify-center overflow-hidden"
									>
										<img
											src={issue.image}
											alt="Anteprima allegato"
											class="w-full h-full object-cover"
										/>
									</div>
								{:else}
									<div
										class="w-full h-full rounded-lg bg-gray-100 flex items-center justify-center"
									>
										<svg
											class="w-6 h-6 text-gray-400"
											fill="none"
											stroke="currentColor"
											viewBox="0 0 24 24"
										>
											<path
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
											></path>
										</svg>
									</div>
								{/if}
							</div>

							<!-- Contenuto -->
							<div class="flex-1 min-w-0">
								<div class="flex items-start justify-between gap-4 mb-2">
									<h3 class="text-lg font-semibold text-gray-900 line-clamp-1">
										{issue.title}
									</h3>
									<div class="flex gap-2 flex-shrink-0">
										<Badge variant="type" value={issue.type} />
										<Badge variant="priority" value={issue.priority} />
										<Badge variant="status" value={issue.status} />
									</div>
								</div>
								<p class="text-sm text-gray-600 mb-3 line-clamp-2">{issue.description}</p>
								<div class="flex items-center gap-4 text-xs text-gray-500">
									<span>Creata il {new Date(issue.createdAt).toLocaleDateString('it-IT')}</span>
									{#if issue.assignedTo}
										<span class="flex items-center gap-1">
											<svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
												<path
													fill-rule="evenodd"
													d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
													clip-rule="evenodd"
												></path>
											</svg>
											{getUserName(issue.assignedTo)}
										</span>
									{/if}
									{#if issue.isArchived}
										<span class="text-orange-600 font-medium">Archiviata</span>
									{/if}
								</div>
							</div>
						</div>
					</button>
				{/each}
			</div>
		{/if}
	</div>
</div>

<!-- Modal Creazione Issue -->
<Modal isOpen={isModalOpen} title="Crea Nuova Issue" onClose={closeModal}>
	<form
		onsubmit={(e) => {
			e.preventDefault();
			handleCreateIssue();
		}}
		class="space-y-6"
	>
		<!-- Titolo -->
		<div>
			<label for="issue-title" class="block text-sm font-medium text-gray-900 mb-2">
				Titolo *
			</label>
			<input
				type="text"
				id="issue-title"
				bind:value={issueTitle}
				class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
				class:border-red-500={formErrors.title}
				placeholder="Breve descrizione del problema"
				maxlength="200"
			/>
			{#if formErrors.title}
				<p class="mt-1.5 text-sm text-red-600">{formErrors.title}</p>
			{/if}
		</div>

		<!-- Tipo e Priorità -->
		<div class="grid grid-cols-2 gap-4">
			<div>
				<label for="issue-type" class="block text-sm font-medium text-gray-900 mb-2">
					Tipo *
				</label>
				<select
					id="issue-type"
					bind:value={issueType}
					class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
				>
					{#each issueTypes as type}
						<option value={type}>{type}</option>
					{/each}
				</select>
			</div>

			<div>
				<label for="issue-priority" class="block text-sm font-medium text-gray-900 mb-2">
					Priorità *
				</label>
				<select
					id="issue-priority"
					bind:value={issuePriority}
					class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
				>
					{#each issuePriorities as priority}
						<option value={priority}>{priority}</option>
					{/each}
				</select>
			</div>
		</div>

		<!-- Stato e Assegnato -->
		<div class="grid grid-cols-2 gap-4">
			<div>
				<label for="issue-status" class="block text-sm font-medium text-gray-900 mb-2">
					Stato
				</label>
				<select
					id="issue-status"
					bind:value={issueStatus}
					class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
				>
					{#each issueStatuses as status}
						<option value={status}>{status}</option>
					{/each}
				</select>
			</div>

			<div>
				<label for="issue-assigned" class="block text-sm font-medium text-gray-900 mb-2">
					Assegnato a
				</label>
				<select
					id="issue-assigned"
					bind:value={issueAssignedTo}
					class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
				>
					<option value="">Nessuno</option>
					{#each getAssignableMembers() as member}
						<option value={member.userId}>{getUserName(member.userId)}</option>
					{/each}
				</select>
			</div>
		</div>

		<!-- Descrizione -->
		<div>
			<label for="issue-desc" class="block text-sm font-medium text-gray-900 mb-2">
				Descrizione *
			</label>
			<textarea
				id="issue-desc"
				bind:value={issueDescription}
				rows="6"
				class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
				class:border-red-500={formErrors.description}
				placeholder="Descrivi in dettaglio il problema o la richiesta..."
			></textarea>
			{#if formErrors.description}
				<p class="mt-1.5 text-sm text-red-600">{formErrors.description}</p>
			{/if}
		</div>

		<!-- Upload Immagine -->
		<div>
			<label for="issue-image" class="block text-sm font-medium text-gray-900 mb-2">
				Immagine (opzionale)
			</label>
			<input
				type="file"
				id="issue-image"
				accept="image/*"
				onchange={handleImageUpload}
				class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
			/>
			{#if formErrors.image}
				<p class="mt-1.5 text-sm text-red-600">{formErrors.image}</p>
			{/if}
			<p class="mt-1 text-xs text-gray-500">Max 5MB - JPG, PNG, GIF</p>

			{#if imagePreview}
				<div class="mt-4 relative">
					<img src={imagePreview} alt="Anteprima immagine da caricare" class="w-full rounded-lg" />
					<button
						type="button"
						onclick={removeImage}
						aria-label="Rimuovi immagine"
						class="absolute top-2 right-2 bg-red-600 hover:bg-red-700 text-white rounded-full p-2"
					>
						<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path
								stroke-linecap="round"
								stroke-linejoin="round"
								stroke-width="2"
								d="M6 18L18 6M6 6l12 12"
							></path>
						</svg>
					</button>
				</div>
			{/if}
		</div>

		<!-- Azioni -->
		<div class="flex gap-3 justify-end pt-4 border-t border-gray-200">
			<button
				type="button"
				onclick={closeModal}
				class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
			>
				Annulla
			</button>
			<button
				type="submit"
				class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
			>
				Crea Issue
			</button>
		</div>
	</form>
</Modal>
