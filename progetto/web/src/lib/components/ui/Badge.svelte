<script lang="ts">
	import type { IssueType, IssuePriority, IssueStatus } from '$lib/types';

	interface Props {
		variant: 'type' | 'priority' | 'status';
		value: IssueType | IssuePriority | IssueStatus;
	}

	let { variant, value }: Props = $props();

	const typeColors: Record<IssueType, string> = {
		BUG: 'bg-red-100 text-red-800',
		FEATURE: 'bg-green-100 text-green-800',
		QUESTION: 'bg-purple-100 text-purple-800',
		DOCUMENTATION: 'bg-blue-100 text-blue-800'
	};

	const priorityColors: Record<IssuePriority, string> = {
		CRITICA: 'bg-red-100 text-red-800',
		ALTA: 'bg-orange-100 text-orange-800',
		MEDIA: 'bg-yellow-100 text-yellow-800',
		BASSA: 'bg-blue-100 text-blue-800'
	};

	const statusColors: Record<IssueStatus, string> = {
		TODO: 'bg-gray-100 text-gray-700',
		IN_PROGRESS: 'bg-blue-100 text-blue-800',
		RISOLTA: 'bg-green-100 text-green-800'
	};

	const labels: Record<string, string> = {
		BUG: 'Bug',
		FEATURE: 'Feature',
		QUESTION: 'Question',
		DOCUMENTATION: 'Docs',
		CRITICA: 'Critica',
		ALTA: 'Alta',
		MEDIA: 'Media',
		BASSA: 'Bassa',
		TODO: 'Todo',
		IN_PROGRESS: 'In Progress',
		RISOLTA: 'Risolta'
	};

	function getColorClass(): string {
		if (variant === 'type') return typeColors[value as IssueType] || 'bg-gray-100 text-gray-800';
		if (variant === 'priority') return priorityColors[value as IssuePriority] || 'bg-gray-100 text-gray-800';
		if (variant === 'status') return statusColors[value as IssueStatus] || 'bg-gray-100 text-gray-800';
		return 'bg-gray-100 text-gray-800';
	}
</script>

<span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium {getColorClass()}">
	{labels[value] || value}
</span>
