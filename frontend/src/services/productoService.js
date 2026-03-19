import axios from 'axios';

// TODO: mover baseURL a variable de entorno (.env)
const API = axios.create({
  baseURL: '/api/productos',
  headers: { 'Content-Type': 'application/json' },
});

// TODO: agregar JSDoc a todas las funciones de este módulo

export const obtenerTodos = () => API.get('/');

export const obtenerPorId = (id) => API.get(`/${id}`);

export const crear = (producto) => API.post('/', producto);

export const actualizar = (id, producto) => API.put(`/${id}`, producto);

// BUG #9 (frontend): debería ser DELETE, no GET
export const eliminar = (id) => API.get(`/${id}`);

export const buscarPorCategoria = (categoria) =>
  API.get(`/categoria/${categoria}`);

export const obtenerDisponibles = () => API.get('/disponibles');

export const aplicarDescuento = (id, porcentaje) =>
  API.patch(`/${id}/descuento?porcentaje=${porcentaje}`);
