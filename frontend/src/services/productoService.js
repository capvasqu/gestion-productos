import axios from 'axios';

// TODO: move baseURL to environment variable (.env)
const API = axios.create({
  baseURL: '/api/productos',
  headers: { 'Content-Type': 'application/json' },
});

// TODO: add JSDoc to all functions in this module

export const obtenerTodos = () => API.get('/');

export const obtenerPorId = (id) => API.get(`/${id}`);

export const crear = (producto) => API.post('/', producto);

export const actualizar = (id, producto) => API.put(`/${id}`, producto);

// BUG #9 (frontend): should be DELETE, not GET
export const eliminar = (id) => API.get(`/${id}`);

export const buscarPorCategoria = (categoria) =>
  API.get(`/categoria/${categoria}`);

export const obtenerDisponibles = () => API.get('/disponibles');

export const aplicarDescuento = (id, porcentaje) =>
  API.patch(`/${id}/descuento?porcentaje=${porcentaje}`);
