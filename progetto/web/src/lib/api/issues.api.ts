import { api } from './client';
import type { Issue, PagedResponse } from '$lib/types';

export interface IssueFilters {
	type?: string[];
	status?: string[];
	priority?: string[];
	assignedToId?: string;
	search?: string;
	archived?: boolean;
	deleted?: boolean;
	page?: number;
	pageSize?: number;
	sortBy?: string;
	order?: 'asc' | 'desc';
}

function buildQuery(filters: IssueFilters): string {
	const params = new URLSearchParams();
	if (filters.type?.length) filters.type.forEach((v) => params.append('type', v));
	if (filters.status?.length) filters.status.forEach((v) => params.append('status', v));
	if (filters.priority?.length) filters.priority.forEach((v) => params.append('priority', v));
	if (filters.assignedToId) params.set('assignedToId', filters.assignedToId);
	if (filters.search) params.set('search', filters.search);
	if (filters.archived) params.set('archived', 'true');
	if (filters.deleted) params.set('deleted', 'true');
	if (filters.page !== undefined) params.set('page', String(filters.page));
	if (filters.pageSize !== undefined) params.set('pageSize', String(filters.pageSize));
	if (filters.sortBy) params.set('sortBy', filters.sortBy);
	if (filters.order) params.set('order', filters.order);
	const qs = params.toString();
	return qs ? `?${qs}` : '';
}

export const issuesApi = {
	getAll: (filters: IssueFilters = {}) =>
		api.get<PagedResponse<Issue>>(`/api/issues${buildQuery(filters)}`),

	getById: (id: string) => api.get<Issue>(`/api/issues/${id}`),

	create: (data: {
		title: string;
		type: string;
		description: string;
		priority?: string;
		assignedToId?: string;
		image?: string;
	}) => api.post<Issue>('/api/issues', data),

	update: (
		id: string,
		data: {
			title?: string;
			type?: string;
			description?: string;
			priority?: string;
			status?: string;
			assignedToId?: string;
			labelIds?: string[];
			archived?: boolean;
			image?: string | null;
		}
	) => api.patch<Issue>(`/api/issues/${id}`, data),

	delete: (id: string) => api.delete<void>(`/api/issues/${id}`),

	restore: (id: string) => api.patch<Issue>(`/api/issues/${id}/restore`, {}),

	exportFile: async (format: 'csv' | 'pdf', filters: IssueFilters = {}) => {
		const query = buildQuery(filters);
		const sep = query ? '&' : '?';
		const path = `/api/issues/export${query}${query ? sep : '?'}format=${format}`;
		const res = await api.getRaw(path);
		if (!res.ok) throw new Error(`Export failed: HTTP ${res.status}`);
		return res.blob();
	}
};
