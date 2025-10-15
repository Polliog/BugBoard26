// Issues Service
import type { Issue, IssueType, IssuePriority, IssueStatus } from '$lib/types';
import { getStoredData, setStoredData } from './mockData';

export class IssuesService {
	/**
	 * Ottieni tutte le issue di un progetto
	 */
	static getIssuesByProject(projectId: string, includeArchived: boolean = false): Issue[] {
		const allIssues = getStoredData<Issue[]>('bugboard_issues', []);
		return allIssues.filter(
			(issue) => issue.projectId === projectId && (includeArchived || !issue.isArchived)
		);
	}

	/**
	 * Ottieni una issue per ID
	 */
	static getIssueById(issueId: string): Issue | null {
		const issues = getStoredData<Issue[]>('bugboard_issues', []);
		return issues.find((i) => i.id === issueId) || null;
	}

	/**
	 * Crea una nuova issue
	 */
	static createIssue(
		projectId: string,
		title: string,
		type: IssueType,
		description: string,
		priority: IssuePriority,
		status: IssueStatus,
		assignedTo: string | undefined,
		createdBy: string,
		image?: string
	): Issue {
		const issues = getStoredData<Issue[]>('bugboard_issues', []);

		const newIssue: Issue = {
			id: `issue-${Date.now()}`,
			projectId,
			title,
			type,
			description,
			priority,
			status,
			assignedTo,
			createdBy,
			createdAt: new Date(),
			image,
			isArchived: false
		};

		issues.push(newIssue);
		setStoredData('bugboard_issues', issues);

		return newIssue;
	}

	/**
	 * Aggiorna una issue
	 */
	static updateIssue(issueId: string, updates: Partial<Issue>): Issue | null {
		const issues = getStoredData<Issue[]>('bugboard_issues', []);
		const index = issues.findIndex((i) => i.id === issueId);

		if (index === -1) return null;

		issues[index] = {
			...issues[index],
			...updates,
			updatedAt: new Date()
		};
		setStoredData('bugboard_issues', issues);

		return issues[index];
	}

	/**
	 * Archivia una issue
	 */
	static archiveIssue(issueId: string, archivedBy: string): Issue | null {
		return this.updateIssue(issueId, {
			isArchived: true,
			archivedAt: new Date(),
			archivedBy
		});
	}

	/**
	 * Ripristina una issue archiviata
	 */
	static unarchiveIssue(issueId: string): Issue | null {
		return this.updateIssue(issueId, {
			isArchived: false,
			archivedAt: undefined,
			archivedBy: undefined
		});
	}

	/**
	 * Elimina una issue (solo admin)
	 */
	static deleteIssue(issueId: string): boolean {
		const issues = getStoredData<Issue[]>('bugboard_issues', []);
		const filtered = issues.filter((i) => i.id !== issueId);

		if (filtered.length === issues.length) return false;

		setStoredData('bugboard_issues', filtered);
		return true;
	}
}
