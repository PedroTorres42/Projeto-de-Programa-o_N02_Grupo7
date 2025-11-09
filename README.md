# Projeto-de-Programa-o_N02_Grupo7

## Descrição
- O módulo implementa um sistema de avaliação de cursos e instrutores na Plataforma de Gerenciamento de Cursos e Treinamentos.
Permite que alunos preencham formulários de avaliação, instrutores registrem notas e feedbacks, e administradores acompanhem relatórios consolidados para apoiar melhorias pedagógicas.

Funcionalidades principais (resumo):
- Alunos avaliam Cursos e Instrutores do seu próprio curso.
- Instrutores avaliam alunos matriculados em cursos que ministram.
- Relatórios para instrutores com média, notas por pergunta e feedback textual (aluno anonimizado).
- Exportações em PDF e CSV.
## Tecnologias Utilizadas

- Java 21
- Spring Boot 3 (Data JPA, Validation, Actuator)
- Hibernate + Spring Data JPA
- SQLite (arquivo local `avaliacoes.db`)
- Swing (interface desktop)
- iText (PDF), OpenCSV (CSV), JFreeChart (gráficos)
- Maven (wrapper incluso `mvnw`/`mvnw.cmd`)

## Instalação / Configuração

Pré-requisitos:
- Java 21 instalado (JDK 21)
- Opcional: Maven instalado (ou usar o wrapper incluso)

Configuração padrão:
- O arquivo `src/main/resources/application.properties` já aponta para um banco SQLite local: `jdbc:sqlite:file:avaliacoes.db?cache=shared&mode=rwc`.
- Ao iniciar, o esquema é criado/atualizado automaticamente (`spring.jpa.hibernate.ddl-auto=update`).
- Dados de exemplo são inseridos pelo `DataLoader` caso ainda não existam (por e-mail).

## Como Rodar o Projeto



## Estrutura do Projeto

```
Projeto/
├─ pom.xml
├─ src/
│  ├─ main/
│  │  ├─ java/br/com/unit/modulo_avaliacao_relatorio/
│  │  │  ├─ Config/        (DataLoader – seed)
│  │  │  ├─ Modelos/       (Entidades JPA: Aluno, Instrutor, Curso, Avaliacao, Nota, ...)
│  │  │  ├─ Repositorios/  (Spring Data)
│  │  │  ├─ Service/       (regras de negócio, relatórios, formulários)
│  │  │  ├─ View/          (telas Swing)
│  │  │  └─ ModuloAvaliacaoView.java (main)
│  │  └─ resources/
│  │     └─ application.properties
│  └─ test/
└─ target/
```

## Banco de Dados / Dados

- Banco: SQLite (arquivo local `avaliacoes.db`, criado na raiz quando a aplicação roda).
- DDL: `update` (Hibernate cria/atualiza as tabelas conforme entidades).
- Seed de dados (inserido na primeira execução sem duplicar e-mails):
	- 3 cursos (Java, Python, Desenvolvimento Web).
	- 2 instrutores por curso.
	- 2 alunos por curso.
	- Formulários "Avaliação do Curso" e "Avaliação do Instrutor" com perguntas básicas.

## Contribuição
- **Antonio Jorge Santana Filho**   
- **Pedro Henrique Torres Pereira** 
- **Lorena Mariah Oliveira Lima**



## Licença
