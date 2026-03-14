import { browser } from '$app/environment';

type Theme = 'light' | 'dark';

let _loading = $state(false);
let _theme = $state<Theme>(
	browser ? (localStorage.getItem('theme') as Theme) || 
	(window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light') : 'light'
);

export const uiStore = {
	get loading() {
		return _loading;
	},
	setLoading(value: boolean) {
		_loading = value;
	},
	get theme() {
		return _theme;
	},
	toggleTheme() {
		_theme = _theme === 'light' ? 'dark' : 'light';
		if (browser) {
			localStorage.setItem('theme', _theme);
		}
	}
};
