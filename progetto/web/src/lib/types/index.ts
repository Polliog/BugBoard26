export type GlobalRole = 'ADMIN' | 'USER' | 'EXTERNAL';
export type IssueType = 'QUESTION' | 'BUG' | 'DOCUMENTATION' | 'FEATURE';
export type IssuePriority = 'BASSA' | 'MEDIA' | 'ALTA' | 'CRITICA';
export type IssueStatus = 'TODO' | 'IN_PROGRESS' | 'RISOLTA';

export interface User {
	id: string;
	email: string;
	name: string;
	role: GlobalRole;
	createdAt: string; // ISO string
}

export interface Issue {
	id: string;
	title: string;
	type: IssueType;
	description: string;
	priority: IssuePriority;
	status: IssueStatus;
	assignedTo: User | null;
	createdBy: User;
	createdAt: string;
	updatedAt: string | null;
	image: string | null;
	archived: boolean;
	archivedAt: string | null;
	archivedBy: User | null;
	labels: string[];
	history: HistoryEntry[];
}

export interface Comment {
	id: string;
	issueId: string;
	author: User;
	content: string;
	createdAt: string;
	updatedAt: string | null;
}

export interface HistoryEntry {
	id: string;
	performedBy: User;
	action: string;
	timestamp: string;
}

export interface Notification {
	id: string;
	message: string;
	issueId: string | null;
	issueTitle: string | null;
	read: boolean;
	createdAt: string;
}

export interface PagedResponse<T> {
	data: T[];
	total: number;
	page: number;
	pageSize: number;
}

export interface AuthResponse {
	token: string;
	user: User;
}
