<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
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

<nav aria-label="Navigazione principale" class="bg-white shadow-sm border-b border-gray-200">
	<div class="max-w-7xl mx-auto px-4 py-4">
		<div class="flex items-center justify-between">
			<a href="/issues" class="flex items-center gap-3">
				<div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
					<svg class="w-6 h-6 text-white" aria-hidden="true" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
							d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
					</svg>
				</div>
				<span class="text-xl font-bold text-gray-900 hidden sm:inline">BugBoard26</span>
			</a>

			<div class="flex items-center gap-2">
				<!-- Notifications -->
				<div class="relative">
					<button
						onclick={() => { showNotifications = !showNotifications; showUserMenu = false; }}
						aria-label="Notifiche"
						class="relative p-2 rounded-lg hover:bg-gray-100 transition-colors"
					>
						<svg class="w-6 h-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
							<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path>
						</svg>
						{#if unreadCount > 0}
							<span class="absolute -top-0.5 -right-0.5 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center font-medium">
								{unreadCount > 9 ? '9+' : unreadCount}
							</span>
						{/if}
					</button>

					{#if showNotifications}
						<div class="fixed inset-x-4 top-16 sm:absolute sm:inset-x-auto sm:right-0 sm:top-auto mt-2 sm:w-96 bg-white rounded-xl shadow-lg border border-gray-200 z-50 overflow-hidden">
							<div class="px-4 py-3 border-b border-gray-200 flex items-center justify-between">
								<h3 class="font-semibold text-gray-900">Notifiche</h3>
								{#if notifications.length > 0}
									<div class="flex gap-2">
										{#if unreadCount > 0}
											<button onclick={handleMarkAllRead}
												class="text-xs text-blue-600 hover:text-blue-700 font-medium">
												Segna tutte lette
											</button>
										{/if}
										<button onclick={handleDeleteAll}
											class="text-xs text-red-600 hover:text-red-700 font-medium">
											Cancella tutte
										</button>
									</div>
								{/if}
							</div>
							{#if notifications.length === 0}
								<div class="px-4 py-8 text-center">
									<svg class="w-10 h-10 text-gray-300 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
										<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path>
									</svg>
									<p class="text-sm text-gray-500">Nessuna notifica</p>
								</div>
							{:else}
								<div class="max-h-80 overflow-y-auto divide-y divide-gray-100">
									{#each notifications as notification (notification.id)}
										<div
											class="flex items-start gap-3 px-4 py-3 hover:bg-gray-50 transition-colors group"
											class:bg-blue-50={!notification.read}
										>
											<button
												onclick={() => handleNotificationClick(notification)}
												class="flex-1 text-left min-w-0"
											>
												<p class="text-sm text-gray-900 line-clamp-2" class:font-medium={!notification.read}>
													{notification.message}
												</p>
												{#if notification.issueTitle}
													<p class="text-xs text-blue-600 mt-0.5 truncate">{notification.issueTitle}</p>
												{/if}
												<p class="text-xs text-gray-400 mt-1">
													{formatTimeAgo(notification.createdAt)}
												</p>
											</button>
											<button
												onclick={(e) => handleDeleteNotification(e, notification.id)}
												class="p-1 rounded text-gray-300 hover:text-red-600 hover:bg-red-50 transition-colors flex-shrink-0"
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
						class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
					>
						<div class="text-right hidden sm:block">
							<p class="text-sm font-medium text-gray-900">{authStore.user?.name}</p>
							<p class="text-xs text-gray-500">{roleLabels[authStore.user?.role ?? ''] ?? ''}</p>
						</div>
						<div class="w-9 h-9 sm:w-10 sm:h-10 bg-blue-100 rounded-full flex items-center justify-center">
							<span class="text-blue-700 font-semibold">
								{authStore.user?.name?.charAt(0).toUpperCase()}
							</span>
						</div>
					</button>

					{#if showUserMenu}
						<div class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50" role="menu">
							<a
								href="/dashboard"
								role="menuitem"
								onclick={() => { showUserMenu = false; }}
								class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
							>
								Dashboard
							</a>
							<div class="border-t border-gray-200"></div>
							<button
								onclick={handleLogout}
								role="menuitem"
								class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
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
