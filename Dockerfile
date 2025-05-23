# Этап 1: сборка проекта с помощью Maven и JDK 11
FROM maven:3.8.7-openjdk-17-slim AS build

WORKDIR /app

# Копируем pom.xml и скачиваем зависимости (чтобы кешировать их, если pom.xml не меняется)
COPY pom.xml .

RUN mvn dependency:go-offline

# Копируем исходники и собираем проект (пропускаем тесты для ускорения)
COPY src ./src

RUN mvn clean package -DskipTests

# Этап 2: запуск скомпилированного приложения с помощью JRE 11
FROM openjdk:17-jre-slim

WORKDIR /app

# Копируем скомпилированный jar из этапа сборки
COPY --from=build /app/target/*.jar app.jar

# Команда запуска приложения
CMD ["java", "-jar", "app.jar"]
