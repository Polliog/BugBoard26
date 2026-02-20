// RF02, RF03, RF06, RF08, RF11, RF13 — Issues API
import { api } from './client';
import type { Issue, PagedResponse } from '$lib/types';

export interface IssueFilters {
	type?: string[];
	status?: string[];
	priority?: string[];
	assignedToId?: string;
	search?: string;
	archived?: boolean;
	page?: number;
	pageSize?: number;
	sortBy?: string;
	order?: 'asc' | 'desc';
}

function buildQuery(filters: IssueFilters): string {
	const params = new URLSearchParams();
	if (filters.type?.length) params.set('type', filters.type.join(','));
	if (filters.status?.length) params.set('status', filters.status.join(','));
	if (filters.priority?.length) params.set('priority', filters.priority.join(','));
	if (filters.assignedToId) params.set('assignedToId', filters.assignedToId);
	if (filters.search) params.set('search', filters.search);
	if (filters.archived) params.set('archived', 'true');
	if (filters.page !== undefined) params.set('page', String(filters.page));
	if (filters.pageSize !== undefined) params.set('pageSize', String(filters.pageSize));
	if (filters.sortBy) params.set('sortBy', filters.sortBy);
	if (filters.order) params.set('order', filters.order);
	const qs = params.toString();
	return qs ? `?${qs}` : '';
}

export const issuesApi = {
	// RF03 — Lista con filtri e paginazione
	getAll: (filters: IssueFilters = {}) =>
		api.get<PagedResponse<Issue>>(`/api/issues${buildQuery(filters)}`),

	getById: (id: string) => api.get<Issue>(`/api/issues/${id}`),

	// RF02 — Creazione issue
	create: (data: {
		title: string;
		type: string;
		description: string;
		priority?: string;
		assignedToId?: string;
		image?: string;
	}) => api.post<Issue>('/api/issues', data),

	// RF06, RF13 — Aggiornamento (stato, archiviazione, etc.)
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
		}
	) => api.patch<Issue>(`/api/issues/${id}`, data),

	// RF08 — Export CSV/PDF
	exportFile: async (format: 'csv' | 'pdf', filters: IssueFilters = {}) => {
		const query = buildQuery(filters);
		const sep = query ? '&' : '?';
		const path = `/api/issues/export${query}${query ? sep : '?'}format=${format}`;
		const res = await api.getRaw(path);
		if (!res.ok) throw new Error(`Export failed: HTTP ${res.status}`);
		return res.blob();
	}
};
