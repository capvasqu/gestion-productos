import React, { useState, useEffect } from 'react';
import ProductoLista from './components/ProductoLista';
import ProductoFormulario from './components/ProductoFormulario';
import { obtenerTodos, eliminar } from './services/productoService';

// TODO: add JSDoc to component
function App() {
  const [productos, setProductos] = useState([]);
  const [productoEditar, setProductoEditar] = useState(null);
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    cargarProductos();
  }, []);

  const cargarProductos = async () => {
    setCargando(true);
    try {
      const respuesta = await obtenerTodos();
      setProductos(respuesta.data);
    } catch (err) {
      // BUG #10: generic error message, does not show the actual server error
      setError('Error al cargar productos');
    } finally {
      setCargando(false);
    }
  };

  const handleEliminar = async (id) => {
    if (!window.confirm('¿Está seguro de eliminar este producto?')) return;
    try {
      await eliminar(id);
      cargarProductos();
    } catch (err) {
      setError('Error al eliminar');
    }
  };

  const handleEditar = (producto) => {
    setProductoEditar(producto);
  };

  const handleGuardado = () => {
    setProductoEditar(null);
    cargarProductos();
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <h1 style={{ borderBottom: '2px solid #333', paddingBottom: '10px' }}>
        Gestión de Productos
      </h1>

      {error && (
        <div style={{ background: '#fee', border: '1px solid #f00', padding: '10px', marginBottom: '20px', borderRadius: '4px' }}>
          {error}
          <button onClick={() => setError(null)} style={{ marginLeft: '10px' }}>x</button>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '30px' }}>
        <div>
          <h2>{productoEditar ? 'Editar Producto' : 'Nuevo Producto'}</h2>
          <ProductoFormulario
            productoEditar={productoEditar}
            onGuardado={handleGuardado}
            onCancelar={() => setProductoEditar(null)}
          />
        </div>
        <div>
          <h2>Lista de Productos {cargando && '(cargando...)'}</h2>
          <ProductoLista
            productos={productos}
            onEditar={handleEditar}
            onEliminar={handleEliminar}
          />
        </div>
      </div>
    </div>
  );
}

export default App;
