// Comments Service
import type { Comment } from '$lib/types';
import { getStoredData, setStoredData } from './mockData';

export class CommentsService {
	/**
	 * Ottieni tutti i commenti di una issue
	 */
	static getCommentsByIssue(issueId: string): Comment[] {
		const allComments = getStoredData<Comment[]>('bugboard_comments', []);
		return allComments
			.filter((c) => c.issueId === issueId)
			.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
	}

	/**
	 * Crea un nuovo commento
	 */
	static createComment(issueId: string, userId: string, content: string): Comment {
		const comments = getStoredData<Comment[]>('bugboard_comments', []);

		const newComment: Comment = {
			id: `comm-${Date.now()}`,
			issueId,
			userId,
			content,
			createdAt: new Date()
		};

		comments.push(newComment);
		setStoredData('bugboard_comments', comments);

		return newComment;
	}

	/**
	 * Aggiorna un commento
	 */
	static updateComment(commentId: string, content: string): Comment | null {
		const comments = getStoredData<Comment[]>('bugboard_comments', []);
		const index = comments.findIndex((c) => c.id === commentId);

		if (index === -1) return null;

		comments[index] = {
			...comments[index],
			content,
			updatedAt: new Date()
		};
		setStoredData('bugboard_comments', comments);

		return comments[index];
	}

	/**
	 * Elimina un commento
	 */
	static deleteComment(commentId: string): boolean {
		const comments = getStoredData<Comment[]>('bugboard_comments', []);
		const filtered = comments.filter((c) => c.id !== commentId);

		if (filtered.length === comments.length) return false;

		setStoredData('bugboard_comments', filtered);
		return true;
	}
}
