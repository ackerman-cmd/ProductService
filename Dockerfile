# Сборка
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app


RUN apt-get update && apt-get install -y maven


COPY pom.xml ./
RUN mvn dependency:go-offline


COPY src ./src
RUN mvn package -DskipTests

#  Создание финального образа
FROM openjdk:17-jdk-slim

WORKDIR /app


RUN apt-get update && apt-get install -y wget && \
    rm -rf /var/lib/apt/lists/*


RUN wget https://archive.apache.org/dist/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz && \
    tar -xzf hadoop-3.3.6.tar.gz && \
    mv hadoop-3.3.6 /opt/hadoop && \
    rm hadoop-3.3.6.tar.gz

#  переменные окружения для Hadoop
ENV HADOOP_HOME=/opt/hadoop
ENV PATH=$PATH:$HADOOP_HOME/bin


COPY --from=builder /app/target/*.jar app.jar


EXPOSE 8080

CMD ["java", "-jar", "app.jar"]