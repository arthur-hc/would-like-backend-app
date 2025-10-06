# Deployment Guide - Would Like API

## Overview
This guide covers deployment strategies from local development to production environments, focusing on containerization, cloud deployment, and CI/CD automation.

## Local Development Deployment

### Docker Compose Setup
Create `docker-compose.yml` in project root:

```yaml
version: '3.8'
services:
  wouldlike-api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DB_HOST=mysql
      - RABBITMQ_HOST=rabbitmq
    depends_on:
      - mysql
      - rabbitmq
    networks:
      - wouldlike-network

  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: wouldlike_db
      MYSQL_USER: wouldlike_user
      MYSQL_PASSWORD: dev_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./scripts/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - wouldlike-network

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: wouldlike
      RABBITMQ_DEFAULT_PASS: dev_password
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - wouldlike-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - wouldlike-network

volumes:
  mysql_data:
  rabbitmq_data:
  redis_data:

networks:
  wouldlike-network:
    driver: bridge
```

### Commands
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f wouldlike-api

# Stop all services
docker-compose down

# Rebuild and start
docker-compose up --build
```

## Production Deployment Options

### Option 1: Azure Container Instances (Recommended for Portfolio)
**Cost**: ~$10-30/month with free tier benefits

#### Azure Setup Steps
```bash
# Login to Azure
az login

# Create resource group
az group create --name wouldlike-rg --location eastus

# Create Azure Container Registry
az acr create --resource-group wouldlike-rg --name wouldlikeregistry --sku Basic

# Create Azure Database for MySQL
az mysql flexible-server create \
  --resource-group wouldlike-rg \
  --name wouldlike-mysql-server \
  --admin-user wishadmin \
  --admin-password YourSecurePassword123! \
  --sku-name Standard_B1ms \
  --version 8.0.21

# Deploy container
az container create \
  --resource-group wouldlike-rg \
  --name wouldlike-api \
  --image wouldlikeregistry.azurecr.io/wouldlike-api:latest \
  --cpu 1 --memory 2 \
  --ports 8080 \
  --environment-variables \
    SPRING_PROFILES_ACTIVE=prod \
    DB_HOST=wouldlike-mysql-server.mysql.database.azure.com
```

### Option 2: AWS Fargate (Alternative)
**Cost**: ~$15-40/month

#### AWS Setup
```bash
# Create ECS cluster
aws ecs create-cluster --cluster-name wouldlike-cluster

# Create RDS MySQL instance
aws rds create-db-instance \
  --db-instance-identifier wouldlike-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password YourSecurePassword123! \
  --allocated-storage 20

# Deploy using Fargate (see detailed config below)
```

### Option 3: Google Cloud Run (Cost-Effective)
**Cost**: ~$5-20/month (pay-per-request)

```bash
# Build and deploy
gcloud builds submit --tag gcr.io/PROJECT-ID/wouldlike-api
gcloud run deploy --image gcr.io/PROJECT-ID/wouldlike-api --platform managed
```

## Kubernetes Deployment

### Local Kubernetes (Minikube)
```bash
# Start Minikube
minikube start

# Apply configurations
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/rabbitmq-deployment.yaml
kubectl apply -f k8s/wouldlike-api-deployment.yaml
kubectl apply -f k8s/ingress.yaml
```

### Kubernetes Manifests Structure
```
k8s/
├── namespace.yaml
├── configmap.yaml
├── secret.yaml
├── mysql-deployment.yaml
├── rabbitmq-deployment.yaml
├── wouldlike-api-deployment.yaml
├── service.yaml
└── ingress.yaml
```

### Sample Deployment Manifest
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wouldlike-api
  namespace: wouldlike
spec:
  replicas: 3
  selector:
    matchLabels:
      app: wouldlike-api
  template:
    metadata:
      labels:
        app: wouldlike-api
    spec:
      containers:
      - name: wouldlike-api
        image: wouldlike-api:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: wouldlike-config
              key: db_host
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

## CI/CD Pipeline

### GitHub Actions Workflow
Create `.github/workflows/ci-cd.yml`:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests
      run: mvn clean test
    
    - name: Generate test report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3

  security-scan:
    runs-on: ubuntu-latest
    needs: test
    steps:
    - uses: actions/checkout@v3
    
    - name: Run OWASP Dependency Check
      uses: dependency-check/Dependency-Check_Action@main
      with:
        project: 'wouldlike-api'
        path: '.'
        format: 'JSON'

  build-and-push:
    runs-on: ubuntu-latest
    needs: [test, security-scan]
    if: github.ref == 'refs/heads/main'
    steps:
    - uses: actions/checkout@v3
    
    - name: Build Docker image
      run: docker build -t wouldlike-api:${{ github.sha }} .
    
    - name: Push to registry
      run: |
        echo ${{ secrets.REGISTRY_PASSWORD }} | docker login -u ${{ secrets.REGISTRY_USERNAME }} --password-stdin
        docker push wouldlike-api:${{ github.sha }}

  deploy-staging:
    runs-on: ubuntu-latest
    needs: build-and-push
    if: github.ref == 'refs/heads/main'
    steps:
    - name: Deploy to staging
      run: |
        # Azure deployment commands
        az container create --resource-group wishlist-staging-rg --name wishlist-api-staging
```

## Environment Configuration

### Development Environment
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/wouldlike_db
    username: wouldlike_user
    password: dev_password
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

logging:
  level:
    com.wishlist: DEBUG
```

### Production Environment
```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    root: INFO
    com.wishlist: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

## Security Considerations

### Production Security Checklist
- [ ] Use HTTPS only (TLS 1.2+)
- [ ] Implement rate limiting
- [ ] Enable CORS properly
- [ ] Use environment variables for secrets
- [ ] Enable security headers
- [ ] Regular security updates
- [ ] Database connection encryption
- [ ] JWT token expiration strategy

### Environment Variables
```bash
# Production environment variables
DB_URL=jdbc:mysql://prod-server:3306/wouldlike_db
DB_USERNAME=prod_user
DB_PASSWORD=super_secure_password
JWT_SECRET=your-256-bit-secret-key
RABBITMQ_HOST=prod-rabbitmq-server
REDIS_URL=redis://prod-redis-server:6379
```

## Monitoring & Observability

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Database connectivity
curl http://localhost:8080/actuator/health/db

# Custom business metrics
curl http://localhost:8080/actuator/metrics/wishlist.created.count
```

### Logging Strategy
- **Structured JSON logging** for production
- **Log aggregation** with ELK stack or Azure Monitor
- **Error tracking** with Sentry or similar
- **Performance monitoring** with APM tools

### Alerting Setup
- **Response time > 1s**: Warning alert
- **Error rate > 5%**: Critical alert
- **Database connections exhausted**: Critical alert
- **Memory usage > 85%**: Warning alert

## Backup & Recovery

### Database Backup
```bash
# Automated daily backups
mysqldump -h $DB_HOST -u $DB_USER -p$DB_PASSWORD wouldlike_db > backup_$(date +%Y%m%d).sql

# Restore from backup
mysql -h $DB_HOST -u $DB_USER -p$DB_PASSWORD wouldlike_db < backup_20250104.sql
```

### Disaster Recovery Plan
1. **RTO (Recovery Time Objective)**: 4 hours
2. **RPO (Recovery Point Objective)**: 1 hour
3. **Backup frequency**: Daily automated + hourly transaction logs
4. **Multi-region deployment** for high availability

## Scaling Strategies

### Horizontal Scaling
- **Load balancer**: Nginx or cloud load balancer
- **Multiple instances**: 3+ API instances
- **Database read replicas**: For read-heavy workloads
- **Cache layer**: Redis for session and data caching

### Performance Optimization
```yaml
# JVM tuning for production
JAVA_OPTS: >
  -Xms1g -Xmx2g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+HeapDumpOnOutOfMemoryError
```

## Cost Optimization

### Cloud Provider Comparison (Monthly Estimates)
| Service | Azure | AWS | GCP |
|---------|-------|-----|-----|
| Container Service | $15-30 | $20-40 | $10-25 |
| Database | $20-50 | $25-60 | $15-40 |
| Storage | $5-15 | $5-15 | $5-15 |
| **Total** | **$40-95** | **$50-115** | **$30-80** |

### Cost Reduction Tips
- Use free tiers when possible
- Auto-scaling based on traffic
- Reserved instances for predictable workloads
- Regular cost monitoring and optimization
