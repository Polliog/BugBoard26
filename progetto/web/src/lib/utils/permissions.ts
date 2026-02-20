// RF15 — Helper permessi centralizzato
import type { User, Issue } from '$lib/types';

export function can(user: User | null, action: string, issue?: Issue): boolean {
	if (!user) return false;
	switch (action) {
		case 'create:issue':
			return user.role !== 'EXTERNAL';
		case 'comment':
			return user.role !== 'EXTERNAL';
		case 'change:status':
			return user.role === 'ADMIN' || issue?.assignedTo?.id === user.id;
		case 'archive':
			return user.role === 'ADMIN';
		case 'manage:users':
			return user.role === 'ADMIN';
		default:
			return false;
	}
}
