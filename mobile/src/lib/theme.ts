import { useColorScheme } from 'react-native';

const palette = {
  light: {
    background: '#FFFFFF',
    surface: '#F4F2FB',
    border: '#E2DEF2',
    text: '#17142B',
    textMuted: '#615C7A',
    primary: '#6C4DF6',
    primaryText: '#FFFFFF',
    danger: '#D93A4C',
    success: '#1F9D63',
  },
  dark: {
    background: '#0F0D1A',
    surface: '#1C1930',
    border: '#2D2948',
    text: '#F3F1FF',
    textMuted: '#A9A4C4',
    primary: '#8F77FF',
    primaryText: '#FFFFFF',
    danger: '#FF6B7B',
    success: '#4CD39A',
  },
};

export type Colors = typeof palette.light;

export function useColors(): Colors {
  return palette[useColorScheme() === 'dark' ? 'dark' : 'light'];
}

export const space = { xs: 4, sm: 8, md: 16, lg: 24, xl: 32 } as const;
export const radius = { sm: 8, md: 14, lg: 22, pill: 999 } as const;
