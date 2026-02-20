import { api } from './client';
import type { Comment } from '$lib/types';

export const commentsApi = {
	getByIssue: (issueId: string) =>
		api.get<Comment[]>(`/api/issues/${issueId}/comments`),

	create: (issueId: string, content: string, image?: string | null) =>
		api.post<Comment>(`/api/issues/${issueId}/comments`, { content, image: image ?? null }),

	update: (issueId: string, commentId: string, content: string, image?: string | null) =>
		api.patch<Comment>(`/api/issues/${issueId}/comments/${commentId}`, { content, image }),

	delete: (issueId: string, commentId: string) =>
		api.delete<void>(`/api/issues/${issueId}/comments/${commentId}`)
};
