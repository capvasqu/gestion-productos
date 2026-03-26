import React, { useState, useEffect } from 'react';
import { crear, actualizar } from '../services/productoService';

// TODO: add JSDoc to component and its props
function ProductoFormulario({ productoEditar, onGuardado, onCancelar }) {

  const estadoInicial = {
    nombre: '',
    descripcion: '',
    precio: '',
    stock: '',
    categoria: '',
  };

  const [form, setForm] = useState(estadoInicial);
  const [errores, setErrores] = useState({});
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    if (productoEditar) {
      setForm({
        nombre: productoEditar.nombre || '',
        descripcion: productoEditar.descripcion || '',
        precio: productoEditar.precio || '',
        stock: productoEditar.stock || '',
        categoria: productoEditar.categoria || '',
      });
    } else {
      setForm(estadoInicial);
    }
    setErrores({});
  }, [productoEditar]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    // BUG #13: does not clear the field error when the user starts editing
  };

  const validar = () => {
    const nuevoErrores = {};
    if (!form.nombre.trim()) nuevoErrores.nombre = 'El nombre es obligatorio';
    if (!form.precio || Number(form.precio) <= 0) nuevoErrores.precio = 'Precio debe ser mayor a 0';
    if (form.stock === '' || Number(form.stock) < 0) nuevoErrores.stock = 'Stock no puede ser negativo';
    if (!form.categoria.trim()) nuevoErrores.categoria = 'La categoría es obligatoria';
    return nuevoErrores;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const erroresValidacion = validar();
    if (Object.keys(erroresValidacion).length > 0) {
      setErrores(erroresValidacion);
      return;
    }
    setGuardando(true);
    try {
      const payload = {
        ...form,
        precio: Number(form.precio),
        stock: Number(form.stock),
      };
      if (productoEditar) {
        await actualizar(productoEditar.id, payload);
      } else {
        await crear(payload);
      }
      setForm(estadoInicial);
      onGuardado();
    } catch (err) {
      // BUG #14: does not display validation errors returned by the backend
      setErrores({ general: 'Error al guardar el producto' });
    } finally {
      setGuardando(false);
    }
  };

  const estiloInput = (campo) => ({
    width: '100%',
    padding: '8px',
    marginBottom: '4px',
    border: errores[campo] ? '1px solid red' : '1px solid #ccc',
    borderRadius: '4px',
    boxSizing: 'border-box',
  });

  const estiloLabel = { display: 'block', marginBottom: '2px', fontWeight: 'bold', fontSize: '13px' };
  const estiloError = { color: 'red', fontSize: '12px', marginBottom: '8px' };
  const estiloGrupo = { marginBottom: '12px' };

  return (
    <form onSubmit={handleSubmit}>
      {errores.general && <p style={{ color: 'red' }}>{errores.general}</p>}

      <div style={estiloGrupo}>
        <label style={estiloLabel}>Nombre *</label>
        <input name="nombre" value={form.nombre} onChange={handleChange} style={estiloInput('nombre')} />
        {errores.nombre && <span style={estiloError}>{errores.nombre}</span>}
      </div>

      <div style={estiloGrupo}>
        <label style={estiloLabel}>Descripción</label>
        <textarea name="descripcion" value={form.descripcion} onChange={handleChange}
          style={{ ...estiloInput('descripcion'), height: '60px', resize: 'vertical' }} />
      </div>

      <div style={estiloGrupo}>
        <label style={estiloLabel}>Precio *</label>
        <input name="precio" type="number" step="0.01" value={form.precio}
          onChange={handleChange} style={estiloInput('precio')} />
        {errores.precio && <span style={estiloError}>{errores.precio}</span>}
      </div>

      <div style={estiloGrupo}>
        <label style={estiloLabel}>Stock *</label>
        <input name="stock" type="number" value={form.stock}
          onChange={handleChange} style={estiloInput('stock')} />
        {errores.stock && <span style={estiloError}>{errores.stock}</span>}
      </div>

      <div style={estiloGrupo}>
        <label style={estiloLabel}>Categoría *</label>
        <input name="categoria" value={form.categoria}
          onChange={handleChange} style={estiloInput('categoria')} />
        {errores.categoria && <span style={estiloError}>{errores.categoria}</span>}
      </div>

      <div style={{ display: 'flex', gap: '10px' }}>
        <button type="submit" disabled={guardando}
          style={{ padding: '8px 20px', background: '#28a745', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
          {guardando ? 'Guardando...' : productoEditar ? 'Actualizar' : 'Crear'}
        </button>
        {productoEditar && (
          <button type="button" onClick={onCancelar}
            style={{ padding: '8px 20px', background: '#6c757d', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
            Cancelar
          </button>
        )}
      </div>
    </form>
  );
}

export default ProductoFormulario;
