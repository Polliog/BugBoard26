// BugBoard26 - TypeScript Interfaces

export type UserRole = 'admin' | 'user';
export type ProjectMemberRole = 'assigned' | 'external';
export type IssueType = 'Question' | 'Bug' | 'Documentation' | 'Feature';
export type IssuePriority = 'Bassa' | 'Media' | 'Alta' | 'Critica';
export type IssueStatus = 'Aperta' | 'In Progress' | 'Risolta' | 'Chiusa';

export interface User {
	id: string;
	email: string;
	name: string;
	password: string; // solo per mock
	role: UserRole;
}

export interface ProjectMember {
	userId: string;
	role: ProjectMemberRole;
	addedAt: Date;
}

export interface Project {
	id: string;
	name: string;
	description: string;
	members: ProjectMember[];
	createdBy: string;
	createdAt: Date;
}

export interface Issue {
	id: string;
	projectId: string;
	title: string;
	type: IssueType;
	description: string;
	priority: IssuePriority;
	status: IssueStatus;
	assignedTo?: string;
	createdBy: string;
	createdAt: Date;
	updatedAt?: Date;
	image?: string; // base64 o URL
	isArchived: boolean;
	archivedAt?: Date;
	archivedBy?: string;
}

export interface Comment {
	id: string;
	issueId: string;
	userId: string;
	content: string;
	createdAt: Date;
	updatedAt?: Date;
}

// Auth types
export interface AuthState {
	user: User | null;
	token: string | null;
	isLoading: boolean;
	error: string | null;
}

export interface LoginCredentials {
	email: string;
	password: string;
	rememberMe?: boolean;
}
