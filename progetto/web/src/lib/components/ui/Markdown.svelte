<script lang="ts">
	import { marked } from 'marked';

	interface Props {
		content: string;
		inline?: boolean;
	}

	let { content, inline = false }: Props = $props();

	marked.setOptions({
		breaks: true,
		gfm: true
	});

	let html = $derived(inline ? marked.parseInline(content) : marked.parse(content));
</script>

<div class="prose prose-sm max-w-none prose-gray">
	{@html html}
</div>
