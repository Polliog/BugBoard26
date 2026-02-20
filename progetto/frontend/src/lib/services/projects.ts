// Projects Service
import type { Project, User, ProjectMember } from '$lib/types';
import { getStoredData, setStoredData } from './mockData';

export class ProjectsService {
	/**
	 * Ottieni tutti i progetti visibili per un utente
	 */
	static getProjectsForUser(user: User): Project[] {
		const allProjects = getStoredData<Project[]>('bugboard_projects', []);

		// Admin vede tutto
		if (user.role === 'admin') {
			return allProjects;
		}

		// User vede solo progetti in cui è membro
		return allProjects.filter((project) =>
			project.members.some((member) => member.userId === user.id)
		);
	}

	/**
	 * Ottieni un progetto per ID
	 */
	static getProjectById(projectId: string): Project | null {
		const projects = getStoredData<Project[]>('bugboard_projects', []);
		return projects.find((p) => p.id === projectId) || null;
	}

	/**
	 * Crea un nuovo progetto
	 */
	static createProject(
		name: string,
		description: string,
		members: ProjectMember[],
		createdBy: string
	): Project {
		const projects = getStoredData<Project[]>('bugboard_projects', []);

		const newProject: Project = {
			id: `proj-${Date.now()}`,
			name,
			description,
			members,
			createdBy,
			createdAt: new Date()
		};

		projects.push(newProject);
		setStoredData('bugboard_projects', projects);

		return newProject;
	}

	/**
	 * Aggiorna un progetto
	 */
	static updateProject(projectId: string, updates: Partial<Project>): Project | null {
		const projects = getStoredData<Project[]>('bugboard_projects', []);
		const index = projects.findIndex((p) => p.id === projectId);

		if (index === -1) return null;

		projects[index] = { ...projects[index], ...updates };
		setStoredData('bugboard_projects', projects);

		return projects[index];
	}

	/**
	 * Verifica se utente ha accesso al progetto
	 */
	static userHasAccess(projectId: string, user: User): boolean {
		if (user.role === 'admin') return true;

		const project = this.getProjectById(projectId);
		if (!project) return false;

		return project.members.some((m) => m.userId === user.id);
	}

	/**
	 * Verifica se utente può modificare il progetto (assigned o admin)
	 */
	static userCanEdit(projectId: string, user: User): boolean {
		if (user.role === 'admin') return true;

		const project = this.getProjectById(projectId);
		if (!project) return false;

		return project.members.some((m) => m.userId === user.id && m.role === 'assigned');
	}

	/**
	 * Conta issue per progetto
	 */
	static getIssueCount(projectId: string): number {
		const issues = getStoredData<any[]>('bugboard_issues', []);
		return issues.filter((i) => i.projectId === projectId && !i.isArchived).length;
	}
}
