/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: ['class', '[data-theme="dark"]'],
  theme: {
    extend: {
      colors: {
        bg: "var(--bg)",
        surface: "var(--surface)",
        border: "var(--border)",
        
        accent: {
          DEFAULT: "var(--accent)",
          bg: "var(--accent-bg)",
          border: "var(--accent-border)",
        },
        
        online: "var(--online)",
        
        text: {
          DEFAULT: "var(--text)",
          h: "var(--text-h)",
        },
        
        code: {
          bg: "var(--code-bg)",
        },

        bubble: {
          in: {
            bg: "var(--bubble-in-bg)",
            text: "var(--bubble-in-text)",
          },
          out: {
            bg: "var(--bubble-out-bg)",
            text: "var(--bubble-out-text)",
          }
        },

        social: {
          bg: "var(--social-bg)",
        }
      },
      boxShadow: {
        DEFAULT: "var(--shadow)",
      },
      fontFamily: {
        sans: ["var(--sans)"],
        heading: ["var(--heading)"],
        mono: ["var(--mono)"],
      },
      animation: {
        'spin-slow': 'spin 1.5s linear infinite',
        'skeleton': 'skeleton 1.5s ease-in-out infinite',
      },
      keyframes: {
        skeleton: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '.5' },
        }
      }
    },
  },
  plugins: [],
}
