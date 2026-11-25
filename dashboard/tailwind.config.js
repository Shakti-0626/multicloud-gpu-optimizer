/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        aws: {
          DEFAULT: '#FF9900',
          dark: '#E88B00',
          light: '#FFF4E5'
        },
        gcp: {
          DEFAULT: '#4285F4',
          dark: '#3367D6',
          light: '#E8F0FE'
        },
        azure: {
          DEFAULT: '#0089D6',
          dark: '#0072B8',
          light: '#E5F4FC'
        }
      }
    },
  },
  plugins: [],
}
