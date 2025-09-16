# Amigo Secreto API

## Descrição

A API Amigo Secreto permite a criação e gerenciamento de grupos para sorteios de amigo secreto. Inclui funcionalidades
como cadastro de grupos, participantes, sorteio e consulta dos pares sorteados.

## Tecnologias Utilizadas

- **Spring Boot** (Framework principal)
- **JPA/Hibernate** (Mapeamento objeto-relacional)
- **PostgreSQL** (Banco de dados relacional)
- **Lombok** (Redução de boilerplate)
- **Docker** (Para contêinerização)
- **JUnit e Mockito** (Para testes)
- **Springdoc-openapi** (Para documentação automática)

## Pré requisitos

- Java 17
- Maven
- PostgreSQL

## Instalação

1. Clone o repositório:

```sh
  git clone https://github.com/seu-usuario/amigo-secreto-api.git
  cd amigo-secreto-api
```

2. Configure o banco de dados no `application.yml`:

```yml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: ${JWT_SECRET}

cors:
  allowedOrigin: ${ALLOWED_ORIGIN}
```

3. Rode a aplicação:

```sh
  ./mvnw spring-boot:run
```

## Docker

Se preferir você pode rodar a API com Docker através do comando:

```sh
  docker-compose up -d
```

## Documentação

A documentação é gerada automaticamente e pode ser acessada em http://localhost:8080/swagger-ui.html

## Testes

Para rodar os testes, utilize:

```sh
  ./mvnw test
```

