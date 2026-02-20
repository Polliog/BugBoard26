// Utility formattazione date

const dateFormatter = new Intl.DateTimeFormat('it-IT', {
	day: '2-digit',
	month: 'long',
	year: 'numeric'
});

const dateTimeFormatter = new Intl.DateTimeFormat('it-IT', {
	day: '2-digit',
	month: 'long',
	year: 'numeric',
	hour: '2-digit',
	minute: '2-digit'
});

export function formatDate(iso: string | null | undefined): string {
	if (!iso) return '';
	return dateFormatter.format(new Date(iso));
}

export function formatDateTime(iso: string | null | undefined): string {
	if (!iso) return '';
	return dateTimeFormatter.format(new Date(iso));
}
