# Frontend Reservaciones - Spring Security HTTP Basic

Esta versión del frontend está adaptada para trabajar con el backend protegido con Spring Security usando HTTP Basic Authentication.

## Roles implementados

- `ADMIN`: acceso exclusivo al dashboard administrativo. Puede gestionar habitaciones, disponibilidad y reservaciones.
- `USUARIO`: acceso a la vista de reservación. Puede consultar disponibilidad y registrar reservaciones.

## Separación de permisos en el frontend

- `/admin`: solo disponible para usuarios con rol `ADMIN`.
- `/reservar`: solo disponible para usuarios con rol `USUARIO`.
- El usuario administrador ya no visualiza ni accede a la opción de reservar.
- Si un rol intenta abrir una vista no permitida, se redirige a `/no-autorizado`.

## Diseño

Se actualizó el diseño hacia una interfaz tipo dashboard:

- Barra superior limpia con datos de sesión.
- Panel administrativo con encabezado ejecutivo, métricas y tarjetas.
- Tablas con estilo limpio para inventario y reservaciones.
- Login y registro con diseño minimalista.
- Navegación adaptada al rol autenticado.

## Usuarios de prueba

ADMIN:

```txt
correo: admin@reservahotel.com
password: Admin12345
```

USUARIO:

```txt
correo: usuario@reservahotel.com
password: Usuario12345
```

## Ejecución

```bash
npm install
npm start
```

## Compilación

La compilación fue probada correctamente con:

```bash
npm run build
```
