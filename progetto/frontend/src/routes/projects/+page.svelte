<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
	import { goto } from '$app/navigation';
	import { onMount } from 'svelte';
	import { toast } from 'svelte-sonner';
	import Navbar from '$lib/components/Navbar.svelte';
	import Modal from '$lib/components/Modal.svelte';
	import Badge from '$lib/components/Badge.svelte';
	import { ProjectsService } from '$lib/services/projects';
	import { mockUsers, initializeMockData } from '$lib/services/mockData';
	import type { Project, ProjectMember } from '$lib/types';

	let projects = $state<Project[]>([]);
	let filteredProjects = $state<Project[]>([]);
	let searchQuery = $state('');
	let showOnlyMyProjects = $state(false);

	// Modal creazione progetto
	let isModalOpen = $state(false);
	let projectName = $state('');
	let projectDescription = $state('');
	let selectedMembers = $state<{ userId: string; role: 'assigned' | 'external' }[]>([]);
	let formErrors = $state({ name: '', description: '' });

	onMount(() => {
		if (!authStore.isAuthenticated) {
			goto('/login');
			return;
		}

		initializeMockData();
		loadProjects();
	});

	function loadProjects() {
		if (!authStore.user) return;
		projects = ProjectsService.getProjectsForUser(authStore.user);
		applyFilters();
	}

	function applyFilters() {
		let result = [...projects];

		// Filtro ricerca
		if (searchQuery) {
			result = result.filter(
				(p) =>
					p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
					p.description.toLowerCase().includes(searchQuery.toLowerCase())
			);
		}

		// Filtro "Solo miei progetti"
		if (showOnlyMyProjects && authStore.user) {
			result = result.filter((p) =>
				p.members.some((m) => m.userId === authStore.user!.id && m.role === 'assigned')
			);
		}

		filteredProjects = result;
	}

	$effect(() => {
		// Reattività: quando cambiano search o filtro
		applyFilters();
	});

	function openCreateModal() {
		isModalOpen = true;
		projectName = '';
		projectDescription = '';
		selectedMembers = [];
		formErrors = { name: '', description: '' };
	}

	function closeModal() {
		isModalOpen = false;
	}

	function validateForm(): boolean {
		formErrors = { name: '', description: '' };
		let isValid = true;

		if (!projectName || projectName.length < 3) {
			formErrors.name = 'Nome progetto deve essere di almeno 3 caratteri';
			isValid = false;
		}

		if (projectName.length > 100) {
			formErrors.name = 'Nome progetto non può superare 100 caratteri';
			isValid = false;
		}

		if (!projectDescription || projectDescription.length < 10) {
			formErrors.description = 'Descrizione deve essere di almeno 10 caratteri';
			isValid = false;
		}

		return isValid;
	}

	function handleCreateProject() {
		if (!validateForm() || !authStore.user) return;

		const members: ProjectMember[] = selectedMembers.map((m) => ({
			...m,
			addedAt: new Date()
		}));

		ProjectsService.createProject(projectName, projectDescription, members, authStore.user.id);

		toast.success('Progetto creato con successo!', {
			description: projectName
		});
		loadProjects();
		closeModal();
	}

	function toggleMember(userId: string, role: 'assigned' | 'external') {
		const index = selectedMembers.findIndex((m) => m.userId === userId);
		if (index >= 0) {
			// Se già presente, rimuovi
			selectedMembers = selectedMembers.filter((m) => m.userId !== userId);
		} else {
			// Altrimenti aggiungi
			selectedMembers = [...selectedMembers, { userId, role }];
		}
	}

	function getUserRole(project: Project): string {
		if (!authStore.user) return '';
		if (authStore.isAdmin) return 'admin';

		const member = project.members.find((m) => m.userId === authStore.user!.id);
		return member?.role || 'none';
	}

	function getIssueCount(projectId: string): number {
		return ProjectsService.getIssueCount(projectId);
	}

	function getUserName(userId: string): string {
		return mockUsers.find((u) => u.id === userId)?.name || 'Unknown';
	}
</script>

<svelte:head>
	<title>Progetti - BugBoard26</title>
</svelte:head>

<div class="min-h-screen bg-gray-50">
	<Navbar />

	<div class="max-w-7xl mx-auto px-4 py-8">
		<!-- Header -->
		<div class="mb-8">
			<h1 class="text-3xl font-bold text-gray-900 mb-2">Progetti</h1>
			<p class="text-gray-600">Gestisci tutti i tuoi progetti e le issue associate</p>
		</div>

		<!-- Filtri e Azioni -->
		<div class="bg-white rounded-xl shadow-sm p-4 mb-6">
			<div class="flex flex-col md:flex-row gap-4 items-start md:items-center justify-between">
				<div class="flex-1 w-full md:w-auto">
					<!-- Barra ricerca -->
					<input
						type="text"
						bind:value={searchQuery}
						placeholder="Cerca progetti..."
						class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
					/>
				</div>

				<div class="flex flex-wrap gap-3 items-center">
					<!-- Filtro "Solo miei" -->
					{#if !authStore.isAdmin}
						<label class="flex items-center gap-2 text-sm text-gray-700">
							<input
								type="checkbox"
								bind:checked={showOnlyMyProjects}
								class="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
							/>
							Solo miei progetti
						</label>
					{/if}

					<!-- Pulsante Crea (solo admin) -->
					{#if authStore.isAdmin}
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
							Crea Progetto
						</button>
					{/if}
				</div>
			</div>
		</div>

		<!-- Lista Progetti -->
		{#if filteredProjects.length === 0}
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
						d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
					></path>
				</svg>
				<h3 class="text-lg font-medium text-gray-900 mb-2">Nessun progetto trovato</h3>
				<p class="text-gray-600">
					{searchQuery ? 'Prova con una ricerca diversa' : 'Non ci sono progetti disponibili'}
				</p>
			</div>
		{:else}
			<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
				{#each filteredProjects as project (project.id)}
					<button
						onclick={() => goto(`/projects/${project.id}/issues`)}
						class="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow p-6 text-left border border-gray-200 hover:border-blue-300"
					>
						<!-- Header Card -->
						<div class="flex items-start justify-between mb-4">
							<h3 class="text-lg font-bold text-gray-900 line-clamp-2">{project.name}</h3>
							{#if getUserRole(project) !== 'admin'}
								<Badge variant="role" value={getUserRole(project)} />
							{/if}
						</div>

						<!-- Descrizione -->
						<p class="text-sm text-gray-600 mb-4 line-clamp-2">{project.description}</p>

						<!-- Statistiche -->
						<div class="flex items-center gap-4 text-sm text-gray-500 mb-4">
							<div class="flex items-center gap-1">
								<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
									></path>
								</svg>
								<span>{getIssueCount(project.id)} issue</span>
							</div>
							<div class="flex items-center gap-1">
								<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
									<path
										stroke-linecap="round"
										stroke-linejoin="round"
										stroke-width="2"
										d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
									></path>
								</svg>
								<span>{project.members.length} membri</span>
							</div>
						</div>

						<!-- Footer -->
						<div class="pt-4 border-t border-gray-100">
							<p class="text-xs text-gray-500">
								Creato il {new Date(project.createdAt).toLocaleDateString('it-IT')}
							</p>
						</div>
					</button>
				{/each}
			</div>
		{/if}
	</div>
</div>

<!-- Modal Creazione Progetto -->
<Modal isOpen={isModalOpen} title="Crea Nuovo Progetto" onClose={closeModal}>
	<form
		onsubmit={(e) => {
			e.preventDefault();
			handleCreateProject();
		}}
		class="space-y-6"
	>
		<!-- Nome Progetto -->
		<div>
			<label for="project-name" class="block text-sm font-medium text-gray-900 mb-2">
				Nome Progetto *
			</label>
			<input
				type="text"
				id="project-name"
				bind:value={projectName}
				class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
				class:border-red-500={formErrors.name}
				placeholder="Es. BugBoard26"
				maxlength="100"
			/>
			{#if formErrors.name}
				<p class="mt-1.5 text-sm text-red-600">{formErrors.name}</p>
			{/if}
		</div>

		<!-- Descrizione -->
		<div>
			<label for="project-desc" class="block text-sm font-medium text-gray-900 mb-2">
				Descrizione *
			</label>
			<textarea
				id="project-desc"
				bind:value={projectDescription}
				rows="4"
				class="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
				class:border-red-500={formErrors.description}
				placeholder="Descrivi il progetto..."
			></textarea>
			{#if formErrors.description}
				<p class="mt-1.5 text-sm text-red-600">{formErrors.description}</p>
			{/if}
		</div>

		<!-- Membri -->
		<div>
			<p class="block text-sm font-medium text-gray-900 mb-3">Membri (opzionale)</p>
			<div class="space-y-2 max-h-64 overflow-y-auto">
				{#each mockUsers as user (user.id)}
					<div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
						<div class="flex items-center gap-3">
							<div class="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
								<span class="text-blue-700 text-sm font-semibold">
									{user.name.charAt(0)}
								</span>
							</div>
							<div>
								<p class="text-sm font-medium text-gray-900">{user.name}</p>
								<p class="text-xs text-gray-500">{user.email}</p>
							</div>
						</div>

						<div class="flex gap-2">
							<button
								type="button"
								onclick={() => toggleMember(user.id, 'assigned')}
								class="px-3 py-1 text-xs rounded-full transition-colors"
								class:bg-blue-600={selectedMembers.some(
									(m) => m.userId === user.id && m.role === 'assigned'
								)}
								class:text-white={selectedMembers.some(
									(m) => m.userId === user.id && m.role === 'assigned'
								)}
								class:bg-gray-200={!selectedMembers.some(
									(m) => m.userId === user.id && m.role === 'assigned'
								)}
								class:text-gray-700={!selectedMembers.some(
									(m) => m.userId === user.id && m.role === 'assigned'
								)}
							>
								Assigned
							</button>
							<button
								type="button"
								onclick={() => toggleMember(user.id, 'external')}
								class="px-3 py-1 text-xs rounded-full transition-colors"
								class:bg-gray-600={selectedMembers.some(
									(m) => m.userId === user.id && m.role === 'external'
								)}
								class:text-white={selectedMembers.some(
									(m) => m.userId === user.id && m.role === 'external'
								)}
								class:bg-gray-200={!selectedMembers.some(
									(m) => m.userId === user.id && m.role === 'external'
								)}
								class:text-gray-700={!selectedMembers.some(
									(m) => m.userId === user.id && m.role === 'external'
								)}
							>
								External
							</button>
						</div>
					</div>
				{/each}
			</div>
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
				Crea Progetto
			</button>
		</div>
	</form>
</Modal>
