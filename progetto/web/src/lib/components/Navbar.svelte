<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
	import { uiStore } from '$lib/stores/ui.svelte';
	import { goto } from '$app/navigation';
	import { toast } from 'svelte-sonner';
	import { notificationsApi } from '$lib/api/notifications.api';
	import { onMount } from 'svelte';
	import type { Notification } from '$lib/types';

	let showUserMenu = $state(false);
	let showNotifications = $state(false);
	let notifications = $state<Notification[]>([]);
	let unreadCount = $derived(notifications.filter((n) => !n.read).length);

	async function loadNotifications() {
		try {
			notifications = await notificationsApi.getAll();
		} catch { /* ignore */ }
	}

	onMount(() => {
		loadNotifications();
		const interval = setInterval(loadNotifications, 30000);
		return () => clearInterval(interval);
	});

	function handleLogout() {
		authStore.logout();
		toast.success('Logout effettuato!');
		goto('/login');
	}

	async function handleNotificationClick(notification: Notification) {
		if (!notification.read) {
			try {
				await notificationsApi.markAsRead(notification.id);
				notifications = notifications.map((n) =>
					n.id === notification.id ? { ...n, read: true } : n
				);
			} catch { /* ignore */ }
		}
		showNotifications = false;
		if (notification.issueId) {
			goto(`/issues/${notification.issueId}`);
		}
	}

	async function handleMarkAllRead() {
		try {
			await notificationsApi.markAllAsRead();
			notifications = notifications.map((n) => ({ ...n, read: true }));
		} catch { /* ignore */ }
	}

	async function handleDeleteNotification(e: Event, id: string) {
		e.stopPropagation();
		try {
			await notificationsApi.delete(id);
			notifications = notifications.filter((n) => n.id !== id);
		} catch { /* ignore */ }
	}

	async function handleDeleteAll() {
		try {
			await notificationsApi.deleteAll();
			notifications = [];
		} catch { /* ignore */ }
	}

	const roleLabels: Record<string, string> = {
		ADMIN: 'Admin',
		USER: 'User',
		EXTERNAL: 'External'
	};

	function formatTimeAgo(dateStr: string): string {
		const diff = Date.now() - new Date(dateStr).getTime();
		const minutes = Math.floor(diff / 60000);
		if (minutes < 1) return 'ora';
		if (minutes < 60) return `${minutes}m fa`;
		const hours = Math.floor(minutes / 60);
		if (hours < 24) return `${hours}h fa`;
		const days = Math.floor(hours / 24);
		return `${days}g fa`;
	}
</script>

<nav aria-label="Navigazione principale" class="bg-white dark:bg-gray-900 shadow-sm border-b border-gray-200 dark:border-gray-800">
	<div class="max-w-7xl mx-auto px-4 py-4">
		<div class="flex items-center justify-between">
			<a href="/issues" class="flex items-center gap-3">
				<div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
					<svg class="w-6 h-6 text-white" aria-hidden="true" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
							d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
					</svg>
				</div>
				<span class="text-xl font-bold text-gray-900 dark:text-white hidden sm:inline">BugBoard26</span>
			</a>

			<div class="flex items-center gap-2">
				<!-- Theme Toggle -->
				<button
					onclick={() => uiStore.toggleTheme()}
					aria-label="Cambia tema"
					class="p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
				>
					{#if uiStore.theme === 'light'}
						<svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path>
						</svg>
					{:else}
						<svg class="w-6 h-6 text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4-9H3m15.364-6.364l-.707.707M6.343 17.657l-.707.707M16.243 17.657l-.707-.707M7.757 6.364l-.707-.707M12 8a4 4 0 100 8 4 4 0 000-8z"></path>
						</svg>
					{/if}
				</button>

				<!-- Notifications -->
				<div class="relative">
					<button
						onclick={() => { showNotifications = !showNotifications; showUserMenu = false; }}
						aria-label="Notifiche"
						class="relative p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
					>
						<svg class="w-6 h-6 text-gray-600 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path>
						</svg>
						{#if unreadCount > 0}
							<span class="absolute -top-0.5 -right-0.5 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center font-medium">
								{unreadCount > 9 ? '9+' : unreadCount}
							</span>
						{/if}
					</button>

					{#if showNotifications}
						<div class="fixed inset-x-4 top-16 sm:absolute sm:inset-x-auto sm:right-0 sm:top-auto mt-2 sm:w-96 bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 z-50 overflow-hidden">
							<div class="px-4 py-3 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
								<h3 class="font-semibold text-gray-900 dark:text-white">Notifiche</h3>
								{#if notifications.length > 0}
									<div class="flex gap-2">
										{#if unreadCount > 0}
											<button onclick={handleMarkAllRead}
												class="text-xs text-blue-600 dark:text-blue-400 hover:text-blue-700 font-medium">
												Segna tutte lette
											</button>
										{/if}
										<button onclick={handleDeleteAll}
											class="text-xs text-red-600 dark:text-red-400 hover:text-red-700 font-medium">
											Cancella tutte
										</button>
									</div>
								{/if}
							</div>
							{#if notifications.length === 0}
								<div class="px-4 py-8 text-center">
									<svg class="w-10 h-10 text-gray-300 dark:text-gray-600 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path>
									</svg>
									<p class="text-sm text-gray-500 dark:text-gray-400">Nessuna notifica</p>
								</div>
							{:else}
								<div class="max-h-80 overflow-y-auto divide-y divide-gray-100 dark:divide-gray-700">
									{#each notifications as notification (notification.id)}
										<div
											class="flex items-start gap-3 px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors group {!notification.read ? 'bg-blue-50 dark:bg-blue-900/20' : ''}"
										>
											<button
												onclick={() => handleNotificationClick(notification)}
												class="flex-1 text-left min-w-0"
											>
												<p class="text-sm text-gray-900 dark:text-gray-100 line-clamp-2" class:font-medium={!notification.read}>
													{notification.message}
												</p>
												{#if notification.issueTitle}
													<p class="text-xs text-blue-600 dark:text-blue-400 mt-0.5 truncate">{notification.issueTitle}</p>
												{/if}
												<p class="text-xs text-gray-400 mt-1">
													{formatTimeAgo(notification.createdAt)}
												</p>
											</button>
											<button
												onclick={(e) => handleDeleteNotification(e, notification.id)}
												class="p-1 rounded text-gray-300 dark:text-gray-500 hover:text-red-600 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors flex-shrink-0"
												aria-label="Elimina notifica"
											>
												<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
													<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
												</svg>
											</button>
										</div>
									{/each}
								</div>
							{/if}
						</div>
					{/if}
				</div>

				<!-- User menu -->
				<div class="relative">
					<button
						onclick={() => { showUserMenu = !showUserMenu; showNotifications = false; }}
						aria-expanded={showUserMenu}
						aria-haspopup="true"
						class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors cursor-pointer"
					>
						<div class="text-right hidden sm:block">
							<p class="text-sm font-medium text-gray-900 dark:text-white">{authStore.user?.name}</p>
							<p class="text-xs text-gray-500 dark:text-gray-400">{roleLabels[authStore.user?.role ?? ''] ?? ''}</p>
						</div>
						<div class="w-9 h-9 sm:w-10 sm:h-10 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center">
							<span class="text-blue-700 dark:text-blue-400 font-semibold">
								{authStore.user?.name?.charAt(0).toUpperCase()}
							</span>
						</div>
					</button>

					{#if showUserMenu}
						<div class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 py-1 z-50" role="menu">
							<a
								href="/dashboard"
								role="menuitem"
								onclick={() => { showUserMenu = false; }}
								class="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
							>
								Dashboard
							</a>
							<div class="border-t border-gray-200 dark:border-gray-700"></div>
							<button
								onclick={handleLogout}
								role="menuitem"
								class="w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
							>
								Logout
							</button>
						</div>
					{/if}
				</div>
			</div>
		</div>
	</div>
</nav>

{#if showUserMenu || showNotifications}
	<button class="fixed inset-0 z-40" onclick={() => { showUserMenu = false; showNotifications = false; }} aria-label="Chiudi menu"></button>
{/if}
