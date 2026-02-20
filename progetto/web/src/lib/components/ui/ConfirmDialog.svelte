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
		danger: 'bg-red-600 hover:bg-red-700 text-white',
		warning: 'bg-orange-600 hover:bg-orange-700 text-white',
		default: 'bg-blue-600 hover:bg-blue-700 text-white'
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
			class="bg-white rounded-xl shadow-2xl max-w-md w-full"
			role="alertdialog"
			aria-modal="true"
			aria-labelledby="confirm-title"
			aria-describedby="confirm-message"
			tabindex="-1"
			onclick={(e) => e.stopPropagation()}
			onkeydown={(e) => e.stopPropagation()}
		>
			<div class="p-6">
				<h2 id="confirm-title" class="text-lg font-bold text-gray-900 mb-2">{title}</h2>
				<p id="confirm-message" class="text-gray-600">{message}</p>
			</div>
			<div class="flex justify-end gap-3 px-6 pb-6">
				<button
					onclick={onCancel}
					class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
				>
					{cancelLabel}
				</button>
				<button
					onclick={onConfirm}
					class="px-4 py-2 rounded-lg transition-colors {variantClasses[variant]}"
				>
					{confirmLabel}
				</button>
			</div>
		</div>
	</div>
{/if}
