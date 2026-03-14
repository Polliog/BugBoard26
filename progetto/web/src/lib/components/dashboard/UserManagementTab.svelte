<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
	import { onMount } from 'svelte';
	import { toast } from 'svelte-sonner';
	import Spinner from '$lib/components/ui/Spinner.svelte';
	import Modal from '$lib/components/ui/Modal.svelte';
	import ConfirmDialog from '$lib/components/ui/ConfirmDialog.svelte';
	import { usersApi } from '$lib/api/users.api';
	import { can } from '$lib/utils/permissions';
	import type { User, GlobalRole } from '$lib/types';

	let users = $state<User[]>([]);
	let loading = $state(true);

	let isCreateOpen = $state(false);
	let editingUser = $state<User | null>(null);
	let confirmDialog = $state<{ open: boolean; title: string; message: string; action: () => void }>({
		open: false, title: '', message: '', action: () => {}
	});

	let createEmail = $state('');
	let createName = $state('');
	let createPassword = $state('');
	let createRole = $state<GlobalRole>('USER');
	let createErrors = $state<Record<string, string>>({});

	let editEmail = $state('');
	let editName = $state('');
	let editRole = $state<GlobalRole>('USER');
	let editPassword = $state('');
	let editErrors = $state<Record<string, string>>({});

	const roles: { value: GlobalRole; label: string }[] = [
		{ value: 'ADMIN', label: 'Admin' },
		{ value: 'USER', label: 'User' },
		{ value: 'EXTERNAL', label: 'External' }
	];

	const roleBadgeColors: Record<GlobalRole, string> = {
		ADMIN: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400',
		USER: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400',
		EXTERNAL: 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-400'
	};

	onMount(() => {
		if (!can(authStore.user, 'manage:users')) return;
		loadUsers();
	});

	async function loadUsers() {
		loading = true;
		try {
			users = await usersApi.getAll();
		} catch {
			toast.error('Errore caricamento utenti');
		} finally {
			loading = false;
		}
	}

	function resetCreateForm() {
		createEmail = '';
		createName = '';
		createPassword = '';
		createRole = 'USER';
		createErrors = {};
	}

	function validateCreate(): boolean {
		createErrors = {};
		let valid = true;
		if (!createEmail || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(createEmail)) {
			createErrors.email = 'Email non valida'; valid = false;
		}
		if (!createName.trim()) { createErrors.name = 'Nome richiesto'; valid = false; }
		if (!createPassword || createPassword.length < 6) {
			createErrors.password = 'Minimo 6 caratteri'; valid = false;
		}
		return valid;
	}

	async function handleCreate() {
		if (!validateCreate()) return;
		try {
			await usersApi.create({ email: createEmail, name: createName, password: createPassword, role: createRole });
			toast.success('Utente creato');
			resetCreateForm();
			isCreateOpen = false;
			loadUsers();
		} catch (err) {
			toast.error(err instanceof Error ? err.message : 'Errore creazione utente');
		}
	}

	function startEdit(user: User) {
		editingUser = user;
		editEmail = user.email;
		editName = user.name;
		editRole = user.role;
		editPassword = '';
		editErrors = {};
	}

	function validateEdit(): boolean {
		editErrors = {};
		let valid = true;
		if (!editEmail || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editEmail)) {
			editErrors.email = 'Email non valida'; valid = false;
		}
		if (!editName.trim()) { editErrors.name = 'Nome richiesto'; valid = false; }
		if (editPassword && editPassword.length < 6) {
			editErrors.password = 'Minimo 6 caratteri'; valid = false;
		}
		return valid;
	}

	async function handleEdit() {
		if (!editingUser || !validateEdit()) return;
		try {
			const data: Record<string, string> = { email: editEmail, name: editName, role: editRole };
			if (editPassword) data.password = editPassword;
			await usersApi.update(editingUser.id, data);
			toast.success('Utente aggiornato');
			editingUser = null;
			loadUsers();
		} catch (err) {
			toast.error(err instanceof Error ? err.message : 'Errore aggiornamento utente');
		}
	}

	function confirmDelete(user: User) {
		confirmDialog = {
			open: true,
			title: 'Elimina Utente',
			message: `Sei sicuro di voler eliminare ${user.name} (${user.email})?`,
			action: async () => {
				try {
					await usersApi.delete(user.id);
					toast.success('Utente eliminato');
					loadUsers();
				} catch (err) {
					toast.error(err instanceof Error ? err.message : 'Errore eliminazione utente');
				}
			}
		};
	}
</script>

{#if !can(authStore.user, 'manage:users')}
	<!-- no-op -->
{:else if loading}
	<div class="flex justify-center py-12"><Spinner size="lg" /></div>
{:else}
	<div class="flex items-center justify-between mb-4 sm:mb-6">
		<div>
			<h2 class="text-lg sm:text-xl font-semibold text-gray-900 dark:text-gray-100 transition-colors">Gestione Utenti</h2>
			<p class="text-sm text-gray-600 dark:text-gray-400 mt-1 transition-colors">{users.length} utenti registrati</p>
		</div>
		<button onclick={() => { resetCreateForm(); isCreateOpen = true; }}
			class="bg-blue-600 hover:bg-blue-700 text-white font-medium px-3 py-2 sm:px-4 rounded-lg transition-colors flex items-center gap-2 text-sm shadow-sm">
			<svg class="w-5 h-5" aria-hidden="true" fill="none" stroke="currentColor" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
			</svg>
			<span class="hidden sm:inline">Nuovo Utente</span>
			<span class="sm:hidden">Nuovo</span>
		</button>
	</div>

	<!-- Mobile card layout -->
	<div class="sm:hidden space-y-3">
		{#each users as user (user.id)}
			<div class="bg-white dark:bg-gray-900 rounded-xl shadow-sm p-4 border border-gray-200 dark:border-gray-800 transition-colors">
				<div class="flex items-center justify-between mb-2">
					<div class="flex items-center gap-3">
						<div class="w-8 h-8 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center flex-shrink-0 transition-colors">
							<span class="text-blue-700 dark:text-blue-300 font-semibold text-sm transition-colors">{user.name.charAt(0)}</span>
						</div>
						<div>
							<p class="font-medium text-gray-900 dark:text-gray-100 transition-colors">{user.name}</p>
							<p class="text-sm text-gray-500 dark:text-gray-400 transition-colors">{user.email}</p>
						</div>
					</div>
					<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {roleBadgeColors[user.role]} transition-colors">
						{user.role}
					</span>
				</div>
				<div class="flex justify-end gap-3 pt-2 border-t border-gray-100 dark:border-gray-800 transition-colors">
					<button onclick={() => startEdit(user)} class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm transition-colors">Modifica</button>
					{#if user.id !== authStore.user?.id}
						<button onclick={() => confirmDelete(user)} class="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 text-sm transition-colors">Elimina</button>
					{/if}
				</div>
			</div>
		{/each}
	</div>

	<!-- Desktop table layout -->
	<div class="hidden sm:block bg-white dark:bg-gray-900 rounded-xl shadow-sm overflow-hidden border border-gray-200 dark:border-gray-800 transition-colors">
		<table class="w-full">
			<thead class="bg-gray-50 dark:bg-gray-950 border-b border-gray-200 dark:border-gray-800 transition-colors">
				<tr>
					<th class="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Nome</th>
					<th class="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Email</th>
					<th class="text-left px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Ruolo</th>
					<th class="text-right px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Azioni</th>
				</tr>
			</thead>
			<tbody class="divide-y divide-gray-200 dark:divide-gray-800 transition-colors">
				{#each users as user (user.id)}
					<tr class="hover:bg-gray-50 dark:hover:bg-gray-800/50 transition-colors">
						<td class="px-6 py-4">
							<div class="flex items-center gap-3">
								<div class="w-8 h-8 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center flex-shrink-0 transition-colors">
									<span class="text-blue-700 dark:text-blue-300 font-semibold text-sm transition-colors">{user.name.charAt(0)}</span>
								</div>
								<span class="font-medium text-gray-900 dark:text-gray-100 transition-colors">{user.name}</span>
							</div>
						</td>
						<td class="px-6 py-4 text-sm text-gray-600 dark:text-gray-400 transition-colors">{user.email}</td>
						<td class="px-6 py-4">
							<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium {roleBadgeColors[user.role]} transition-colors">
								{user.role}
							</span>
						</td>
						<td class="px-6 py-4 text-right">
							<div class="flex justify-end gap-2">
								<button onclick={() => startEdit(user)} class="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 text-sm transition-colors">
									Modifica
								</button>
								{#if user.id !== authStore.user?.id}
									<button onclick={() => confirmDelete(user)} class="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 text-sm transition-colors">
										Elimina
									</button>
								{/if}
							</div>
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>
{/if}

<Modal isOpen={isCreateOpen} title="Nuovo Utente" onClose={() => { resetCreateForm(); isCreateOpen = false; }}>
	<form onsubmit={(e) => { e.preventDefault(); handleCreate(); }} class="space-y-4">
		<div>
			<label for="create-name" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-1 transition-colors">Nome</label>
			<input type="text" id="create-name" bind:value={createName}
				class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
				class:border-red-500={createErrors.name} />
			{#if createErrors.name}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{createErrors.name}</p>{/if}
		</div>
		<div>
			<label for="create-email" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-1 transition-colors">Email</label>
			<input type="email" id="create-email" bind:value={createEmail}
				class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
				class:border-red-500={createErrors.email} />
			{#if createErrors.email}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{createErrors.email}</p>{/if}
		</div>
		<div>
			<label for="create-password" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-1 transition-colors">Password</label>
			<input type="password" id="create-password" bind:value={createPassword}
				class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
				class:border-red-500={createErrors.password} />
			{#if createErrors.password}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{createErrors.password}</p>{/if}
		</div>
		<div>
			<label for="create-role" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-1 transition-colors">Ruolo</label>
			<select id="create-role" bind:value={createRole}
				class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors">
				{#each roles as r}<option value={r.value} class="dark:bg-gray-800">{r.label}</option>{/each}
			</select>
		</div>
		<div class="flex gap-3 justify-end pt-4 border-t border-gray-200 dark:border-gray-800 transition-colors">
			<button type="button" onclick={() => { resetCreateForm(); isCreateOpen = false; }}
				class="px-4 py-2 bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
				Annulla
			</button>
			<button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm">
				Crea Utente
			</button>
		</div>
	</form>
</Modal>

<Modal isOpen={editingUser !== null} title="Modifica Utente" onClose={() => { editingUser = null; }}>
	<form onsubmit={(e) => { e.preventDefault(); handleEdit(); }} class="space-y-4">
		<div>
			<label for="edit-name" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-1 transition-colors">Nome</label>
			<input type="text" id="edit-name" bind:value={editName}
				class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
				class:border-red-500={editErrors.name} />
			{#if editErrors.name}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{editErrors.name}</p>{/if}
		</div>
		<div>
			<label for="edit-email" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-1 transition-colors">Email</label>
			<input type="email" id="edit-email" bind:value={editEmail}
				class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
				class:border-red-500={editErrors.email} />
			{#if editErrors.email}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{editErrors.email}</p>{/if}
		</div>
		<div>
			<label for="edit-role" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-1 transition-colors">Ruolo</label>
			<select id="edit-role" bind:value={editRole}
				class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors">
				{#each roles as r}<option value={r.value} class="dark:bg-gray-800">{r.label}</option>{/each}
			</select>
		</div>
		<div>
			<label for="edit-password" class="block text-sm font-medium text-gray-900 dark:text-gray-200 mb-1 transition-colors">Nuova Password</label>
			<input type="password" id="edit-password" bind:value={editPassword} placeholder="Lascia vuoto per non cambiare"
				class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 dark:text-gray-100 transition-colors"
				class:border-red-500={editErrors.password} />
			{#if editErrors.password}<p class="mt-1 text-sm text-red-600 dark:text-red-400">{editErrors.password}</p>{/if}
			<p class="mt-1 text-xs text-gray-500 dark:text-gray-400 transition-colors">Lascia vuoto per mantenere la password attuale</p>
		</div>
		<div class="flex gap-3 justify-end pt-4 border-t border-gray-200 dark:border-gray-800 transition-colors">
			<button type="button" onclick={() => { editingUser = null; }}
				class="px-4 py-2 bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors">
				Annulla
			</button>
			<button type="submit" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors shadow-sm">
				Salva Modifiche
			</button>
		</div>
	</form>
</Modal>

<ConfirmDialog
	isOpen={confirmDialog.open}
	title={confirmDialog.title}
	message={confirmDialog.message}
	variant="danger"
	confirmLabel="Elimina"
	onConfirm={() => { confirmDialog.open = false; confirmDialog.action(); }}
	onCancel={() => { confirmDialog.open = false; }}
/>
