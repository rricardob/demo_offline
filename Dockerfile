# Usa una imagen base de Gradle con JDK 17
FROM gradle:8.4.0-jdk17 AS build

# Establece el directorio de trabajo
WORKDIR /app

# Copia todos los archivos del proyecto al contenedor
COPY . .

# Construye el proyecto
RUN gradle clean build --no-daemon

# Usa una imagen base de OpenJDK para la ejecución
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo .jar desde la etapa de compilación
COPY --from=build /app/build/libs/*.jar app.jar

# Expone el puerto (ajustar según la configuración de tu aplicación)
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]
