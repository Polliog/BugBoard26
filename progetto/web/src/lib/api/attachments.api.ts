import { api } from './client';
import type { Attachment } from '$lib/types';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export const attachmentsApi = {
	upload: (issueId: string, file: File) => {
		const formData = new FormData();
		formData.append('file', file);
		return api.postForm<Attachment>(`/api/issues/${issueId}/attachments`, formData);
	},

	download: async (issueId: string, storedFilename: string): Promise<Blob> => {
		const res = await api.getRaw(`/api/issues/${issueId}/attachments/${storedFilename}`);
		if (!res.ok) throw new Error(`HTTP ${res.status}`);
		return res.blob();
	},

	delete: (issueId: string, storedFilename: string) =>
		api.delete<void>(`/api/issues/${issueId}/attachments/${storedFilename}`)
};
