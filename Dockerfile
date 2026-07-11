# ============================================================
# Etapa 1 - BUILD
# Compila y empaqueta el JAR con el wrapper de Gradle.
# Se saltan los tests: son @SpringBootTest y necesitan una
# instancia de Oracle viva + los secretos, cosa que no existe
# durante el build de la imagen.
# ============================================================
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiamos primero el wrapper y los descriptores de build para
# aprovechar la cache de capas de Docker (las dependencias solo
# se re-descargan si cambian estos archivos).
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies || true

# Ahora el codigo fuente y el empaquetado.
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ============================================================
# Etapa 2 - RUNTIME
# Imagen minima con solo el JRE y el JAR. Corre como usuario
# no-root.
# ============================================================
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Usuario sin privilegios.
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /app/build/libs/*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

# Los secretos y la config de BD se inyectan por variables de
# entorno en tiempo de ejecucion (ver docker-compose.yml).
ENTRYPOINT ["java", "-jar", "app.jar"]
