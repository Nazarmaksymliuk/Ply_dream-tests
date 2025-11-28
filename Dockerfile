FROM mcr.microsoft.com/playwright/java:v1.46.0-focal

# Створимо робочу директорію
WORKDIR /app

# Скопіюємо pom.xml і завантажимо залежності (кешування)
COPY pom.xml .
RUN mvn -B dependency:resolve dependency:resolve-plugins

# Скопіюємо весь проєкт
COPY . .

# Запуск тестів
CMD ["mvn", "-B", "clean", "verify"]
