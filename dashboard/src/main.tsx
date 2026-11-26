import React from 'react';
import ReactDOM from 'react-dom/client';
import axios from 'axios';
import App from './App';
import './index.css';

// Support production backend deployment (Render/Vercel)
const apiUrl = (import.meta as any).env?.VITE_API_URL;
if (apiUrl) {
  axios.defaults.baseURL = apiUrl;
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
