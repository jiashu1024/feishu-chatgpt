# 基于 Java 8 和 Maven 的镜像
FROM maven:3.6.3-jdk-8

# 设置工作目录
WORKDIR /app

# 克隆项目
RUN git clone https://github.com/1130600015/feishu-chatgpt.git .

# Maven 打包，跳过测试
RUN mvn package -DskipTests

# 将 target 里的 jar 包拷贝到项目根目录
RUN cp target/*.jar .

# 用 Java 执行 jar 包
CMD ["java", "-jar", "FeiShuBot-1.0.0.jar"]
