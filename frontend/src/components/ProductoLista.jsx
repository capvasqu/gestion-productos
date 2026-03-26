import React from 'react';

// TODO: add JSDoc to component and its props
function ProductoLista({ productos, onEditar, onEliminar }) {

  // BUG #11: does not handle empty list with a friendly message
  if (!productos) return <p>Cargando...</p>;

  const estiloTabla = {
    width: '100%',
    borderCollapse: 'collapse',
    fontSize: '14px',
  };

  const estiloTh = {
    background: '#333',
    color: '#fff',
    padding: '10px',
    textAlign: 'left',
  };

  const estiloTd = {
    padding: '8px 10px',
    borderBottom: '1px solid #ddd',
  };

  const estiloBoton = {
    marginRight: '6px',
    padding: '4px 10px',
    cursor: 'pointer',
    borderRadius: '4px',
    border: 'none',
  };

  return (
    <table style={estiloTabla}>
      <thead>
        <tr>
          <th style={estiloTh}>ID</th>
          <th style={estiloTh}>Nombre</th>
          <th style={estiloTh}>Categoría</th>
          <th style={estiloTh}>Precio</th>
          <th style={estiloTh}>Stock</th>
          <th style={estiloTh}>Activo</th>
          <th style={estiloTh}>Acciones</th>
        </tr>
      </thead>
      <tbody>
        {productos.map((p) => (
          <tr key={p.id} style={{ background: p.activo ? '#fff' : '#f9f9f9' }}>
            <td style={estiloTd}>{p.id}</td>
            <td style={estiloTd}>{p.nombre}</td>
            <td style={estiloTd}>{p.categoria}</td>
            {/* BUG #12: price is not formatted as currency */}
            <td style={estiloTd}>{p.precio}</td>
            <td style={estiloTd}>{p.stock}</td>
            <td style={estiloTd}>{p.activo ? 'Sí' : 'No'}</td>
            <td style={estiloTd}>
              <button
                style={{ ...estiloBoton, background: '#4a90e2', color: '#fff' }}
                onClick={() => onEditar(p)}
              >
                Editar
              </button>
              <button
                style={{ ...estiloBoton, background: '#e24a4a', color: '#fff' }}
                onClick={() => onEliminar(p.id)}
              >
                Eliminar
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default ProductoLista;
