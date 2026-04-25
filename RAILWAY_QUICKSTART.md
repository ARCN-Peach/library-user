# Despliegue en Railway - Guía rápida

Para desplegar **library-user** en Railway correctamente, sigue estos pasos:

## 📋 Checklist de variables requeridas

Asegúrate de configurar estas variables en Railway Dashboard:

✅ **Base de datos:**

- `DB_URL` → `jdbc:postgresql://postgres-host:5432/library_user`
- `DB_USER` → Usuario de PostgreSQL
- `DB_PASSWORD` → Contraseña PostgreSQL

✅ **RabbitMQ:**

- `RABBITMQ_HOST` → Host de RabbitMQ en Railway
- `RABBITMQ_PORT` → 5672 (por defecto)
- `RABBITMQ_USER` → Usuario RabbitMQ
- `RABBITMQ_PASSWORD` → Contraseña RabbitMQ

✅ **Seguridad:**

- `JWT_SECRET` → Mínimo 30 caracteres (cambiar en producción)

✅ **Bootstrap (opcional):**

- `BOOTSTRAP_LIBRARIAN_ENABLED=true`
- `BOOTSTRAP_LIBRARIAN_NAME`
- `BOOTSTRAP_LIBRARIAN_EMAIL`
- `BOOTSTRAP_LIBRARIAN_PASSWORD`

## 🚀 Pasos rápidos

1. **En Railway Dashboard:**

   ```
   New Project → Deploy from GitHub → ARCN-Peach/library-user
   ```

2. **Agregar PostgreSQL y RabbitMQ:**

   ```
   + Add → Database → PostgreSQL
   + Add → RabbitMQ (o Docker image)
   ```

3. **Configurar variables:**
   - Ve a `library-user` → Variables
   - Copia las variables del archivo `.env.railway.example`
   - Reemplaza placeholder values con valores reales

4. **Deploy:**

   ```
   Railway auto-redeploy en cada push a main
   O: Click en "Deploy" manualmente
   ```

## 🔍 Verificar despliegue

```bash
# Ver logs
railway logs -f

# Probar salud
curl https://tu-app.up.railway.app/actuator/health

# Swagger
https://tu-app.up.railway.app/swagger-ui.html
```

## 📖 Documentación completa

Ver archivo `DEPLOYMENT_RAILWAY.md` en la raíz del repositorio para guía detallada y troubleshooting.
