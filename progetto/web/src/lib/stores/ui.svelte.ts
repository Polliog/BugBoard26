let _loading = $state(false);

export const uiStore = {
	get loading() {
		return _loading;
	},
	setLoading(value: boolean) {
		_loading = value;
	}
};
