# Étape 1 : Construction de l'artefact JAR avec Maven et Java 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copie des fichiers Maven
COPY pom.xml ./
COPY .mvn .mvn/
COPY mvnw ./

# Téléchargement des dépendances hors-ligne pour accélérer les builds ultérieurs
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

# Copie des sources et compilation
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Étape 2 : Image d'exécution légère JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Création du dossier d'uploads local
RUN mkdir -p /app/uploads/donations

# Utilisateur non-root pour la sécurité
RUN addgroup -S yowgifter && adduser -S yowgifter -G yowgifter
USER yowgifter

# Copie du JAR compilé
COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
