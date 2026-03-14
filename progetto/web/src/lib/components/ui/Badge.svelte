<script lang="ts">
	import type { IssueType, IssuePriority, IssueStatus } from '$lib/types';

	interface Props {
		variant: 'type' | 'priority' | 'status';
		value: IssueType | IssuePriority | IssueStatus;
	}

	let { variant, value }: Props = $props();

	const typeColors: Record<IssueType, string> = {
		BUG: 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400',
		FEATURE: 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400',
		QUESTION: 'bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-400',
		DOCUMENTATION: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400'
	};

	const priorityColors: Record<IssuePriority, string> = {
		CRITICA: 'bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400',
		ALTA: 'bg-orange-100 dark:bg-orange-900/30 text-orange-800 dark:text-orange-400',
		MEDIA: 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400',
		BASSA: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400'
	};

	const statusColors: Record<IssueStatus, string> = {
		TODO: 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300',
		IN_PROGRESS: 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400',
		RISOLTA: 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400'
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
		if (variant === 'type') return typeColors[value as IssueType] || 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200';
		if (variant === 'priority') return priorityColors[value as IssuePriority] || 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200';
		if (variant === 'status') return statusColors[value as IssueStatus] || 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200';
		return 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-200';
	}
</script>

<span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium {getColorClass()} transition-colors">
	{labels[value] || value}
</span>
