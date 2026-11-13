Guía de Instalación y Ejecución

Sigue estos pasos en orden para levantar el sistema en tu entorno local:

### Paso 1: Configurar la Base de Datos
1. Abre **SQL Server Management Studio (SSMS)** e inicia sesión en tu instancia local.
2. Ubica el archivo script SQL dentro de la carpeta `database/` de este repositorio 
3. Abre el archivo en SSMS y ejecútalo completo (**F5**).
   * *Esto creará automáticamente la base de datos `bd_la_esquina`, las 7 tablas relacionales, restricciones de integridad (`CHECK`), índices de rendimiento y datos iniciales de prueba*

### Paso 2: Ajustar Credenciales de Conexión en Java
1. Abre el proyecto `SistemaInventario` en **Apache NetBeans**.
2. Navega hacia el paquete `dao` y abre el archivo **`ConexionDB.java`**.
3. Ubica las siguientes líneas dentro del bloque estático y reemplaza los datos según la configuración de tu SQL Server local[cite: 122]:
   ```java
   config.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=bd_la_esquina;encrypt=true;trustServerCertificate=true;");
   config.setUsername("sa");              // Tu usuario de SQL Server
   config.setPassword("tu_contraseña");   // Tu contraseña real de SQL Server
