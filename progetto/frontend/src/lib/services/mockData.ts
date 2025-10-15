// Mock Data Service
import type { User, Project, Issue, Comment } from '$lib/types';

// Mock Users
export const mockUsers: User[] = [
	{
		id: '1',
		email: 'admin@bugboard.com',
		name: 'Super Admin',
		password: 'admin123',
		role: 'admin'
	},
	{
		id: '2',
		email: 'user@bugboard.com',
		name: 'Mario Rossi',
		password: 'user123',
		role: 'user'
	},
	{
		id: '3',
		email: 'viewer@bugboard.com',
		name: 'Luca Bianchi',
		password: 'viewer123',
		role: 'user'
	},
	{
		id: '4',
		email: 'dev@bugboard.com',
		name: 'Anna Verdi',
		password: 'dev123',
		role: 'user'
	}
];

// Mock Projects
export const mockProjects: Project[] = [
	{
		id: 'proj-1',
		name: 'BugBoard26',
		description: 'Sistema di tracciamento bug e issue per progetti software',
		members: [
			{ userId: '1', role: 'assigned', addedAt: new Date('2024-01-01') },
			{ userId: '2', role: 'assigned', addedAt: new Date('2024-01-02') },
			{ userId: '3', role: 'external', addedAt: new Date('2024-01-03') }
		],
		createdBy: '1',
		createdAt: new Date('2024-01-01')
	},
	{
		id: 'proj-2',
		name: 'E-Commerce Platform',
		description: 'Piattaforma e-commerce con gestione ordini e pagamenti',
		members: [
			{ userId: '2', role: 'assigned', addedAt: new Date('2024-02-01') },
			{ userId: '4', role: 'assigned', addedAt: new Date('2024-02-02') }
		],
		createdBy: '2',
		createdAt: new Date('2024-02-01')
	},
	{
		id: 'proj-3',
		name: 'Mobile App React Native',
		description: 'App mobile cross-platform per iOS e Android',
		members: [
			{ userId: '1', role: 'assigned', addedAt: new Date('2024-03-01') },
			{ userId: '4', role: 'external', addedAt: new Date('2024-03-05') }
		],
		createdBy: '1',
		createdAt: new Date('2024-03-01')
	}
];

// Mock Issues
export const mockIssues: Issue[] = [
	{
		id: 'issue-1',
		projectId: 'proj-1',
		title: 'Errore login con credenziali vuote',
		type: 'Bug',
		description:
			'Quando si tenta il login senza inserire email e password, il sistema non mostra errori di validazione.',
		priority: 'Alta',
		status: 'Aperta',
		assignedTo: '2',
		createdBy: '3',
		createdAt: new Date('2024-10-10'),
		isArchived: false
	},
	{
		id: 'issue-2',
		projectId: 'proj-1',
		title: 'Aggiungere filtro per priorità nelle issue',
		type: 'Feature',
		description:
			'Implementare un filtro dropdown per filtrare le issue in base alla priorità (Bassa, Media, Alta, Critica).',
		priority: 'Media',
		status: 'In Progress',
		assignedTo: '2',
		createdBy: '1',
		createdAt: new Date('2024-10-12'),
		isArchived: false
	},
	{
		id: 'issue-3',
		projectId: 'proj-1',
		title: 'Come si crea un nuovo progetto?',
		type: 'Question',
		description:
			'Non riesco a trovare il pulsante per creare un nuovo progetto. Dove si trova?',
		priority: 'Bassa',
		status: 'Risolta',
		assignedTo: '1',
		createdBy: '3',
		createdAt: new Date('2024-10-08'),
		updatedAt: new Date('2024-10-09'),
		isArchived: false
	},
	{
		id: 'issue-4',
		projectId: 'proj-1',
		title: 'Documentare API endpoints',
		type: 'Documentation',
		description: 'Creare documentazione Swagger per tutti gli endpoint REST API del progetto.',
		priority: 'Media',
		status: 'Aperta',
		assignedTo: '2',
		createdBy: '1',
		createdAt: new Date('2024-10-14'),
		isArchived: false
	},
	{
		id: 'issue-5',
		projectId: 'proj-1',
		title: 'Sistema crash durante upload immagine grande',
		type: 'Bug',
		description:
			'Quando si carica un\'immagine > 10MB, l\'applicazione crasha. Verificare limite dimensione.',
		priority: 'Critica',
		status: 'In Progress',
		assignedTo: '2',
		createdBy: '1',
		createdAt: new Date('2024-10-15'),
		isArchived: false
	},
	{
		id: 'issue-6',
		projectId: 'proj-2',
		title: 'Carrello non salva prodotti dopo refresh',
		type: 'Bug',
		description: 'I prodotti aggiunti al carrello spariscono dopo il refresh della pagina.',
		priority: 'Alta',
		status: 'Aperta',
		assignedTo: '4',
		createdBy: '2',
		createdAt: new Date('2024-10-11'),
		isArchived: false
	},
	{
		id: 'issue-7',
		projectId: 'proj-2',
		title: 'Integrare pagamento con Stripe',
		type: 'Feature',
		description: 'Aggiungere integrazione con Stripe per i pagamenti con carta di credito.',
		priority: 'Alta',
		status: 'Aperta',
		assignedTo: '2',
		createdBy: '2',
		createdAt: new Date('2024-10-13'),
		isArchived: false
	},
	{
		id: 'issue-8',
		projectId: 'proj-3',
		title: 'App si blocca su Android 12',
		type: 'Bug',
		description: 'L\'applicazione si blocca all\'avvio su dispositivi Android 12+.',
		priority: 'Critica',
		status: 'Aperta',
		assignedTo: '4',
		createdBy: '1',
		createdAt: new Date('2024-10-14'),
		isArchived: false
	},
	{
		id: 'issue-9',
		projectId: 'proj-3',
		title: 'Come testare su iOS simulator?',
		type: 'Question',
		description: 'Qual è il comando per avviare l\'app su iOS simulator?',
		priority: 'Bassa',
		status: 'Risolta',
		createdBy: '4',
		createdAt: new Date('2024-10-10'),
		updatedAt: new Date('2024-10-10'),
		isArchived: false
	}
];

// Mock Comments
export const mockComments: Comment[] = [
	{
		id: 'comm-1',
		issueId: 'issue-1',
		userId: '2',
		content: 'Ho verificato il problema, manca la validazione lato client. Procedo con il fix.',
		createdAt: new Date('2024-10-10T10:30:00')
	},
	{
		id: 'comm-2',
		issueId: 'issue-1',
		userId: '1',
		content: 'Grazie Mario! Assegna priorità alta.',
		createdAt: new Date('2024-10-10T11:00:00')
	},
	{
		id: 'comm-3',
		issueId: 'issue-3',
		userId: '1',
		content:
			'Il pulsante "Crea Progetto" appare solo per gli utenti admin in alto a destra nella pagina progetti.',
		createdAt: new Date('2024-10-08T14:00:00')
	},
	{
		id: 'comm-4',
		issueId: 'issue-3',
		userId: '3',
		content: 'Perfetto, grazie! Ho trovato il pulsante.',
		createdAt: new Date('2024-10-09T09:00:00')
	},
	{
		id: 'comm-5',
		issueId: 'issue-5',
		userId: '2',
		content:
			'Sto implementando un check lato client per bloccare upload > 5MB con messaggio di errore.',
		createdAt: new Date('2024-10-15T16:00:00')
	}
];

// Helper per ottenere dati dal localStorage o inizializzare
export function getStoredData<T>(key: string, defaultValue: T): T {
	if (typeof window === 'undefined') return defaultValue;

	const stored = localStorage.getItem(key);
	if (!stored) {
		localStorage.setItem(key, JSON.stringify(defaultValue));
		return defaultValue;
	}

	try {
		return JSON.parse(stored, (key, value) => {
			// Deserializza le date
			if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}T/.test(value)) {
				return new Date(value);
			}
			return value;
		});
	} catch {
		return defaultValue;
	}
}

export function setStoredData<T>(key: string, data: T): void {
	if (typeof window === 'undefined') return;
	localStorage.setItem(key, JSON.stringify(data));
}

// Inizializza dati se non esistono
export function initializeMockData() {
	getStoredData('bugboard_users', mockUsers);
	getStoredData('bugboard_projects', mockProjects);
	getStoredData('bugboard_issues', mockIssues);
	getStoredData('bugboard_comments', mockComments);
}
