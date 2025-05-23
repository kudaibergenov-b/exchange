# Этап 1: Сборка с Maven
FROM eclipse-temurin:17 AS builder

WORKDIR /app

# Копируем файлы проекта
COPY . .

# Скачиваем зависимости и собираем проект
RUN ./mvnw clean package -DskipTests

# Этап 2: Финальный образ с JRE
FROM eclipse-temurin:17-jre

WORKDIR /app

# Копируем jar из предыдущего этапа
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]