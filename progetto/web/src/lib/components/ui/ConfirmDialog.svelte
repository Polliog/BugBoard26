<script lang="ts">
	interface Props {
		isOpen: boolean;
		title: string;
		message: string;
		confirmLabel?: string;
		cancelLabel?: string;
		variant?: 'danger' | 'warning' | 'default';
		onConfirm: () => void;
		onCancel: () => void;
	}

	let {
		isOpen,
		title,
		message,
		confirmLabel = 'Conferma',
		cancelLabel = 'Annulla',
		variant = 'default',
		onConfirm,
		onCancel
	}: Props = $props();

	const variantClasses: Record<string, string> = {
		danger: 'bg-red-600 hover:bg-red-700 text-white shadow-sm shadow-red-500/20',
		warning: 'bg-orange-600 hover:bg-orange-700 text-white shadow-sm shadow-orange-500/20',
		default: 'bg-blue-600 hover:bg-blue-700 text-white shadow-sm shadow-blue-500/20'
	};
</script>

{#if isOpen}
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div
		class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4"
		onclick={onCancel}
		onkeydown={(e) => { if (e.key === 'Escape') onCancel(); }}
	>
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			class="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl max-w-md w-full border border-gray-200 dark:border-gray-800 transition-colors"
			role="alertdialog"
			aria-modal="true"
			aria-labelledby="confirm-title"
			aria-describedby="confirm-message"
			tabindex="-1"
			onclick={(e) => e.stopPropagation()}
			onkeydown={(e) => e.stopPropagation()}
		>
			<div class="p-4 sm:p-6">
				<h2 id="confirm-title" class="text-lg sm:text-xl font-bold text-gray-900 dark:text-white mb-2 transition-colors">{title}</h2>
				<p id="confirm-message" class="text-sm sm:text-base text-gray-600 dark:text-gray-400 transition-colors">{message}</p>
			</div>
			<div class="flex flex-col sm:flex-row justify-end gap-3 px-4 pb-4 sm:px-6 sm:pb-6">
				<button
					onclick={onCancel}
					class="px-4 py-2 bg-white dark:bg-gray-900 border border-gray-300 dark:border-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors order-2 sm:order-1"
				>
					{cancelLabel}
				</button>
				<button
					onclick={onConfirm}
					class="px-4 py-2 rounded-lg transition-colors font-medium order-1 sm:order-2 {variantClasses[variant]}"
				>
					{confirmLabel}
				</button>
			</div>
		</div>
	</div>
{/if}
