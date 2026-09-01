# Gestor Documental REST API (Spring Boot)

Sistema de gestión documental desarrollado con **Spring Boot 3** y **PostgreSQL**, diseñado para administrar la carga, almacenamiento, metadatos y descarga de archivos asociados a usuarios, empresas y sistemas autorizados.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 17+
* **Framework:** Spring Boot 3.3.x
* **Persistencia:** Spring Data JPA / Hibernate
* **Base de Datos:** PostgreSQL
* **Gestor de Dependencias:** Apache Maven
* **Almacenamiento Físico:** Sistema de archivos (Disk Storage)

---

## 📂 Estructura del Proyecto

```text
src/main/java/developer/fullstack/gestordocumento/
│
├── config/          # Configuraciones de beans y propiedades de almacenamiento
├── controller/      # Endpoints REST (Company, User, Document)
├── dto/             # Objetos de Transferencia de Datos (Request/Response)
├── entity/          # Entidades JPA (Document, User, Company, SystemAccess)
├── repository/      # Repositorios de Spring Data JPA
├── service/         # Interfaces e implementaciones de la lógica de negocio
│   └── impl/
└── GestordocumentoApplication.java

⚙️ Configuración PreviaBase de Datos: Asegúrate de tener una instancia de PostgreSQL ejecutándose y crea la base de datos:SQLCREATE DATABASE gestor_documental_db;
Propiedades de la Aplicación (src/main/resources/application.properties):Propertiesserver.port=8080

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/gestor_documental_db
spring.datasource.username=postgres
spring.datasource.password=tu_contraseña

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Límite de tamaño de subida
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# Ruta de almacenamiento local
storage.location=uploads
🚀 Ejecución del ProyectoCompila y ejecuta la aplicación utilizando el wrapper de Maven:Bash# Compilar el proyecto
./mvnw clean compile

# Ejecutar la aplicación
./mvnw spring-boot:run
La API estará disponible en http://localhost:8080.📌 Documentación de la API (Endpoints REST)🏢 1. Empresas (/api/v1/companies)MétodoEndpointDescripciónPOST/api/v1/companiesRegistra una nueva empresa.GET/api/v1/companiesObteiene la lista de todas las empresas.GET/api/v1/companies/{id}Obtiene los datos de una empresa por UUID.PUT/api/v1/companies/{id}Actualiza los datos de una empresa.DELETE/api/v1/companies/{id}Elimina una empresa por su UUID.Ejemplo Payload POST:JSON{
  "name": "Empresa Ejemplo S.A.",
  "taxId": "76123456-7"
}
👤 2. Usuarios (/api/v1/users)MétodoEndpointDescripciónPOST/api/v1/usersRegistra un nuevo usuario con empresa/sistemas opcionales.GET/api/v1/usersObtiene la lista de todos los usuarios.GET/api/v1/users/{id}Obtiene la información detallada de un usuario.PUT/api/v1/users/{id}Actualiza la información de un usuario.DELETE/api/v1/users/{id}Elimina un usuario por su UUID.Ejemplo Payload POST:JSON{
  "fullName": "Francisco Aponte",
  "email": "francisco@ejemplo.com",
  "companyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "systemIds": []
}
📄 3. Documentos (/api/v1/documents)MétodoEndpointDescripciónPOST/api/v1/documents/uploadSube un archivo en multipart/form-data.GET/api/v1/documentsLista todos los documentos cargados.GET/api/v1/documents/{id}/metadataObtiene los metadatos de un documento por su ID.GET/api/v1/documents/{id}/downloadDescarga el archivo físico guardado en el servidor.Parametros POST /upload (form-data):file: Archivo binario (Requerido).email: String (Opcional - Asocia automáticamente el archivo al usuario si existe en BD).🧪 Automatización en PostmanPara guardar automáticamente el UUID del documento subido y usarlo en las solicitudes de descarga/metadatos, agrega el siguiente código en la pestaña Scripts -> Post-response (o Tests) de la petición POST /upload:JavaScriptif (pm.response.code === 201 || pm.response.code === 200) {
    const responseJson = pm.response.json();
    if (responseJson.id) {
        pm.globals.set("document_id", responseJson.id);
    }
}
Luego invoca la descarga o metadatos utilizando {{document_id}}:GET http://localhost:8080/api/v1/documents/{{document_id}}/download