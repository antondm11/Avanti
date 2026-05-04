# Avanti

## Cómo ejecutar Avanti correctamente

Avanti es una aplicación JavaFX conectada a una base de datos MySQL. Para que funcione correctamente, hay que preparar tres cosas: el proyecto Java, la base de datos y los archivos multimedia.

---

### 1. Clonar el repositorio

Primero hay que clonar el repositorio desde GitHub:

```bash
git clone URL_DEL_REPOSITORIO
cd Avanti
```

### 2. Descargar los archivos multimedia con Git LFS

Este proyecto utiliza vídeos `.mp4` para reproducir trailers dentro de la aplicación. Como algunos archivos son pesados, se gestionan mediante Git LFS.

Si no está instalado Git LFS, hay que instalarlo antes:

```bash
git lfs install
```

Después, dentro de la carpeta del proyecto:

```bash
git lfs pull
```

Esto descargará correctamente los vídeos de la carpeta:

`avanti-app/src/main/resources/trailers/`

Si no se hace este paso, los trailers pueden no reproducirse correctamente.

### 3. Crear la base de datos

La base de datos se encuentra en:

`database/avanti.sql`

Para cargarla:

1. Abrir MySQL Workbench o phpMyAdmin.
2. Asegurarse de que MySQL está iniciado.
3. Ejecutar el script completo `avanti.sql`.

El script crea automáticamente:

- La base de datos `avanti`
- Las tablas necesarias
- Los géneros, películas, usuarios, alquileres y datos de prueba
- Los triggers y la vista `v_alquileres`

### 4. Configuración de conexión

La conexión a la base de datos está definida en:

`avanti-app/src/main/java/teamavanti/bbdd/DatabaseManager.java`

Por defecto usa:

```java
private static final String URL = "jdbc:mysql://localhost:3306/avanti";
private static final String USUARIO = "root";
private static final String PASSWORD = "";
```

Es decir:

- **Base de datos:** avanti
- **Usuario:** root
- **Contraseña:** vacía

Si en el equipo se utiliza otra contraseña de MySQL, hay que modificar el valor de `PASSWORD`.

### 5. Ejecutar la aplicación

El punto de entrada principal es:

`avanti-app/src/main/java/teamavanti/Main.java`

Desde el IDE, se debe ejecutar la clase:

`teamavanti.Main`

También es importante que el proyecto se abra desde la carpeta `avanti-app`, ya que ahí se encuentra el `pom.xml`.

### 6. Usuarios de prueba

Para iniciar sesión como **cliente**:
- **Email:** cliente@avanti.com
- **Contraseña:** cliente123

Para iniciar sesión como **administradora**:
- **Email:** admin@avanti.com
- **Contraseña:** admin123

### 7. Notas importantes

- MySQL debe estar iniciado antes de ejecutar la aplicación.
- La base de datos `avanti` debe existir y estar cargada con el script `database/avanti.sql`.
- Los trailers y portadas deben estar descargados correctamente mediante Git LFS.
- Si los vídeos no se reproducen, ejecutar:
  ```bash
  git lfs pull
  ```
- Si la aplicación no conecta con la base de datos, revisar `DatabaseManager.java` y comprobar usuario, contraseña y puerto de MySQL.
