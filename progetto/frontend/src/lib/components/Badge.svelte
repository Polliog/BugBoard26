<script lang="ts">
	import type { IssueType, IssuePriority, IssueStatus, ProjectMemberRole } from '$lib/types';

	interface Props {
		variant: 'type' | 'priority' | 'status' | 'role';
		value: IssueType | IssuePriority | IssueStatus | ProjectMemberRole;
	}

	let { variant, value }: Props = $props();

	const typeColors: Record<IssueType, string> = {
		Bug: 'bg-red-100 text-red-800',
		Feature: 'bg-green-100 text-green-800',
		Question: 'bg-purple-100 text-purple-800',
		Documentation: 'bg-blue-100 text-blue-800'
	};

	const priorityColors: Record<IssuePriority, string> = {
		Critica: 'bg-red-500 text-white',
		Alta: 'bg-orange-500 text-white',
		Media: 'bg-yellow-500 text-white',
		Bassa: 'bg-blue-500 text-white'
	};

	const statusColors: Record<IssueStatus, string> = {
		Aperta: 'bg-gray-100 text-gray-800',
		'In Progress': 'bg-blue-100 text-blue-800',
		Risolta: 'bg-green-100 text-green-800',
		Chiusa: 'bg-gray-800 text-white'
	};

	const roleColors: Record<ProjectMemberRole, string> = {
		assigned: 'bg-blue-100 text-blue-800',
		external: 'bg-gray-100 text-gray-800'
	};

	function getColorClass(): string {
		if (variant === 'type') return typeColors[value as IssueType] || 'bg-gray-100 text-gray-800';
		if (variant === 'priority')
			return priorityColors[value as IssuePriority] || 'bg-gray-100 text-gray-800';
		if (variant === 'status')
			return statusColors[value as IssueStatus] || 'bg-gray-100 text-gray-800';
		if (variant === 'role')
			return roleColors[value as ProjectMemberRole] || 'bg-gray-100 text-gray-800';
		return 'bg-gray-100 text-gray-800';
	}
</script>

<span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium {getColorClass()}">
	{value}
</span>
