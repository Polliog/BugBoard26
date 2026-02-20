<script lang="ts">
	interface Props {
		isOpen: boolean;
		title: string;
		onClose: () => void;
		children?: import('svelte').Snippet;
	}

	let { isOpen, title, onClose, children }: Props = $props();
</script>

{#if isOpen}
	<!-- svelte-ignore a11y_no_static_element_interactions -->
	<div
		class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4"
		onclick={onClose}
		onkeydown={(e) => { if (e.key === 'Escape') onClose(); }}
	>
		<!-- svelte-ignore a11y_no_static_element_interactions -->
		<div
			class="bg-white rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
			role="dialog"
			aria-modal="true"
			aria-labelledby="modal-title"
			tabindex="-1"
			onclick={(e) => e.stopPropagation()}
			onkeydown={(e) => e.stopPropagation()}
		>
			<div class="flex items-center justify-between p-6 border-b border-gray-200">
				<h2 id="modal-title" class="text-2xl font-bold text-gray-900">{title}</h2>
				<button
					onclick={onClose}
					class="text-gray-400 hover:text-gray-600 transition-colors"
					aria-label="Chiudi"
				>
					<svg class="w-6 h-6" aria-hidden="true" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
					</svg>
				</button>
			</div>
			<div class="p-6">
				{@render children?.()}
			</div>
		</div>
	</div>
{/if}
