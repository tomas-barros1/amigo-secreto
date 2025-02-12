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

2. Configure o banco de dados no `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/amigosecreto
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
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

