# Projeto-de-Programa-o_N02_Grupo7

## Descrição
- O módulo implementa um sistema de avaliação de cursos e instrutores na Plataforma de Gerenciamento de Cursos e Treinamentos.
Permite que alunos preencham formulários de avaliação, instrutores registrem notas e feedbacks, e administradores acompanhem relatórios consolidados para apoiar melhorias pedagógicas.

Funcionalidades principais (resumo):
- Alunos avaliam Cursos e Instrutores do seu próprio curso.
- Instrutores avaliam alunos matriculados em cursos que ministram.
- Relatórios para instrutores com média, notas por pergunta e feedback textual (aluno anonimizado).
- Exportações em PDF e CSV.

## Objetivo do Módulo Desenvolvido

O módulo de Avaliação e Relatórios tem como objetivo prover um ciclo contínuo de melhoria pedagógica dentro da Plataforma de Cursos e Treinamentos. Ele centraliza:

1. Coleta estruturada de percepções (formulários de avaliação de Curso, Instrutor e desempenho de Aluno).
2. Registro de notas e feedback qualitativo / sentimento.
3. Consolidação de métricas comparativas (entre cursos, instrutores e alunos) e relatórios detalhados.
4. Geração de documentos (PDF com gráficos e CSV de avaliações) para análise externa ou arquivamento.

Com isso, administradores conseguem identificar padrões de satisfação e desempenho, instrutores acompanham resultados para ajustes didáticos e alunos participam ativamente do processo de melhoria.

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

### Requisitos
* JDK 21 instalado (verifique com `java -version`).
* Opcional: Maven instalado; caso não tenha, use o wrapper (`mvnw.cmd`).

### Execução Rápida (Windows PowerShell)
```powershell
./mvnw.cmd spring-boot:run
```
Isso iniciará a aplicação desktop (Swing). Como o módulo é configurado para `mainClass` `br.com.unit.modulo_avaliacao_relatorio.ModuloAvaliacaoView`, a janela inicial aparecerá após o bootstrap do Spring.

### Construir e Executar via JAR
```powershell
./mvnw.cmd clean package
java -jar target/Projeto-de-Programa-o_N02_Grupo7-0.0.1-SNAPSHOT.jar
```
Ao iniciar pelo JAR o comportamento visual é o mesmo: será aberta a interface Swing principal.

### Fluxo Básico na Interface
1. Tela Inicial / Login: fazer login ou cadastrar novo usuário (Aluno, Instrutor ou Administrador).
2. Menu Principal: botões são exibidos de acordo com o perfil logado.
	- Aluno: avaliar Curso / Instrutor.
	- Instrutor: avaliar Aluno, visualizar relatórios de seus cursos.
	- Administrador: gerar e visualizar relatórios consolidados, gerenciar formulários, cadastrar usuários e cursos.
3. Formulários: preencher e salvar; dados ficam persistidos em SQLite (`avaliacoes.db`).
4. Relatórios: filtrar, gerar novos, visualizar, exportar PDF/CSV e excluir.

### Dados de Exemplo
Na primeira execução, o `DataLoader` insere cursos, instrutores, alunos e formulários padrão se ainda não existirem (evita duplicação por e-mail). Isso permite testar rapidamente sem carga manual inicial.

### Variáveis e Ajustes
* Banco: configurado em `application.properties`. Para usar outro path basta alterar a URL JDBC.
* Caso deseje iniciar com um banco limpo, apague o arquivo `avaliacoes.db` antes de executar.
* Logs / Actuator: endpoints de Actuator podem ser expandidos futuramente; atualmente o foco é o uso desktop.


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

## Responsabilidade de cada Integrante
- **Antonio Jorge Santana Filho**   
- **Pedro Henrique Torres Pereira** 
- **Lorena Mariah Oliveira Lima**
- **Joãoo Vitor Nunes Oliveira Aves**



## Licença
