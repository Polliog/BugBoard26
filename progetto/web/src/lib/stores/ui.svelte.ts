// UI Store — toast e stato modali (Svelte 5 Runes)

let _loading = $state(false);

export const uiStore = {
	get loading() {
		return _loading;
	},
	setLoading(value: boolean) {
		_loading = value;
	}
};
