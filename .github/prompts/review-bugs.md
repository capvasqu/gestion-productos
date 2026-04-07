# Agente: review-bugs
# Propósito: Revisión integral de código — bugs, calidad, seguridad y performance

Analiza exhaustivamente todos los archivos de código fuente de este repositorio
y genera un archivo llamado REPORTE-REVISION-INTEGRAL.md en la raíz del proyecto.

## Instrucciones de análisis

### 1. Bugs y errores lógicos
Busca en todo el código:
- Errores lógicos y bugs funcionales
- Validaciones faltantes de parámetros de entrada (null, negativos, vacíos)
- Manejo incorrecto de excepciones (catch vacíos, excepciones genéricas)
- Condiciones de carrera o problemas de concurrencia
- Borrado físico donde debería haber borrado lógico
- Pérdida de precisión en operaciones numéricas (double/float para dinero)
- NullPointerException potenciales no manejados

### 2. Calidad de código
Evalúa las siguientes dimensiones:
- Principios SOLID (Single Responsibility, Open/Closed, etc.)
- Código duplicado o que viola DRY (Don't Repeat Yourself)
- Métodos demasiado largos o con demasiadas responsabilidades
- Inyección de dependencias incorrecta (@Autowired en campo vs constructor)
- Ausencia de documentación (Javadoc/JSDoc) en métodos públicos
- Nombres de variables o métodos poco descriptivos
- Complejidad ciclomática alta

### 3. Seguridad (perspectiva ciberseguridad)
Identifica vulnerabilidades de seguridad:
- SQL Injection: queries dinámicas sin parametrizar
- Exposición de entidades JPA directamente en endpoints REST
- Datos sensibles expuestos en logs o respuestas de error
- Falta de validación de entrada en endpoints públicos
- Configuraciones inseguras (contraseñas hardcodeadas, SSL desactivado)
- Cross-Site Scripting (XSS) en el frontend
- Exposición de stack traces en respuestas de error al cliente
- Secretos o API keys expuestos en el código

### 4. Performance y rendimiento
Detecta problemas de rendimiento:
- Consultas N+1 en JPA/Hibernate (lazy loading mal usado)
- Falta de paginación en endpoints que retornan listas completas
- Operaciones costosas dentro de loops
- Falta de índices sugeridos para queries frecuentes
- Carga innecesaria de datos completos cuando solo se necesitan campos específicos
- Re-renders innecesarios en componentes React (falta de useMemo/useCallback)
- Llamadas HTTP redundantes desde el frontend

## Formato del reporte

Genera el reporte con la siguiente estructura exacta:

# Reporte de Revisión Integral de Código
**Proyecto:** [nombre del proyecto]
**Fecha:** [fecha actual]
**Archivos analizados:** [número]
**Total de hallazgos:** [número]

## Resumen ejecutivo
[Párrafo de 3-4 líneas con los hallazgos más críticos]

## Estadísticas
| Categoría | Crítico | Alto | Medio | Bajo | Total |
|-----------|---------|------|-------|------|-------|
| Bugs | | | | | |
| Calidad | | | | | |
| Seguridad | | | | | |
| Performance | | | | | |
| **Total** | | | | | |

## Hallazgos detallados

### Bugs y errores lógicos
#### [CRÍTICO/ALTO/MEDIO/BAJO] BUG-001: [título]
- **Archivo:** ruta/al/archivo.java (línea N)
- **Descripción:** qué está mal y por qué es un problema
- **Impacto:** qué puede ocurrir si no se corrige
- **Corrección sugerida:** código o descripción de cómo corregirlo

[repetir para cada hallazgo]

### Calidad de código
[misma estructura]

### Seguridad
[misma estructura]

### Performance
[misma estructura]

## Plan de acción recomendado
Lista priorizada de los 5 hallazgos más críticos a corregir primero,
con estimación de esfuerzo (horas) para cada uno.

## Nota sobre herramientas complementarias
Este análisis fue realizado por Claude Code. Para análisis estático
continuo se recomienda complementar con:
- SonarQube/SonarCloud: calidad y cobertura de código
- OWASP Dependency-Check: vulnerabilidades en dependencias
- Snyk: seguridad en dependencias y contenedores