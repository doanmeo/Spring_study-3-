
# STAGE 1: Build ứng dụng (Tạo file .jar)

# Sử dụng Maven và JDK 25 để build code

FROM maven:3.9-eclipse-temurin-26-alpine AS builder
WORKDIR /app

# Copy file cấu hình maven và source code vào container
COPY pom.xml .
COPY src ./src

# Build project, bỏ qua test để tăng tốc độ build (vì test cần database)

RUN mvn clean package -DskipTests

# STAGE 2: Chạy ứng dụng (Môi trường Runtime siêu nhẹ)
# Chỉ sử dụng JRE (Java Runtime Environment) để chạy, giảm kích thước Image

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app

# Copy file .jar đã được build từ STAGE 1 sang STAGE 2

COPY --from=builder /app/target/*.jar app.jar

# Expose port 8080 ra bên ngoài

EXPOSE 8080

# Lệnh khởi chạy Spring Boot

ENTRYPOINT ["java", "-jar", "app.jar"]