package br.com.unit.modulo_avaliacao_relatorio.Config;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.*;
import br.com.unit.modulo_avaliacao_relatorio.Service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@Lazy(false)
public class DataLoader implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final FormularioService formularioService;

    public DataLoader(
            UsuarioService usuarioService, 
            CursoService cursoService, 
            FormularioService formularioService) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.formularioService = formularioService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Criar Administrador
        Administrador admin = new Administrador();
        admin.setNome("Admin Sistema");
        admin.setEmail("admin@sistema.com");
        admin.setSenha("admin123");
        var existenteAdmin = usuarioService.buscarPorEmail(admin.getEmail()).orElse(null);
        if (existenteAdmin == null) {
            admin = (Administrador) usuarioService.salvarUsuario(admin);
        } else if (existenteAdmin instanceof Administrador a) {
            admin = a;
        } else {
            System.out.println("[DataLoader] Email admin@sistema.com já existe para outro tipo de usuário. Mantendo existente e seguindo.");
        }

        // 2. Criar Instrutores
        Instrutor instrutor1 = new Instrutor();
        instrutor1.setNome("Carlos Silva");
        instrutor1.setEmail("carlos@teste.com");
        instrutor1.setSenha("1234");
        instrutor1.setEspecialidade("Java");

        Instrutor instrutor2 = new Instrutor();
        instrutor2.setNome("Ana Souza");
        instrutor2.setEmail("ana@teste.com");
        instrutor2.setSenha("1234");
        instrutor2.setEspecialidade("Python");

        Instrutor instrutor3 = new Instrutor();
        instrutor3.setNome("João Oliveira");
        instrutor3.setEmail("joao@teste.com");
        instrutor3.setSenha("1234");
        instrutor3.setEspecialidade("JavaScript");

        instrutor1 = resolveInstrutor(instrutor1);
        instrutor2 = resolveInstrutor(instrutor2);
        instrutor3 = resolveInstrutor(instrutor3);

        // 3. Criar Cursos
        Curso cursoJava = new Curso();
        cursoJava.setNome("Curso Java");
        cursoJava.setDescricao("Aprendizado de Java básico e avançado");
        cursoJava.setCargaHoraria(40);
        cursoJava.setInstrutores(Arrays.asList(instrutor1));

        Curso cursoPython = new Curso();
        cursoPython.setNome("Curso Python");
        cursoPython.setDescricao("Aprendizado de Python básico e avançado");
        cursoPython.setCargaHoraria(35);
        cursoPython.setInstrutores(Arrays.asList(instrutor2));

        Curso cursoWeb = new Curso();
        cursoWeb.setNome("Desenvolvimento Web");
        cursoWeb.setDescricao("HTML, CSS e JavaScript");
        cursoWeb.setCargaHoraria(45);
        cursoWeb.setInstrutores(Arrays.asList(instrutor3));

        cursoService.criarCurso(cursoJava);
        cursoService.criarCurso(cursoPython);
        cursoService.criarCurso(cursoWeb);

        // 4. Criar Alunos
        Aluno aluno1 = new Aluno();
        aluno1.setNome("Pedro Torres");
        aluno1.setEmail("pedro@teste.com");
        aluno1.setSenha("1234");
        aluno1.setMatricula("2025001");
        aluno1.setCursoAtual(cursoJava);

        Aluno aluno2 = new Aluno();
        aluno2.setNome("Maria Lima");
        aluno2.setEmail("maria@teste.com");
        aluno2.setSenha("1234");
        aluno2.setMatricula("2025002");
        aluno2.setCursoAtual(cursoPython);

        Aluno aluno3 = new Aluno();
        aluno3.setNome("Lucas Santos");
        aluno3.setEmail("lucas@teste.com");
        aluno3.setSenha("1234");
        aluno3.setMatricula("2025003");
        aluno3.setCursoAtual(cursoWeb);

        aluno1 = resolveAluno(aluno1);
        aluno2 = resolveAluno(aluno2);
        aluno3 = resolveAluno(aluno3);

        // 5. Criar Formulários e Perguntas
        // Para cada formulário, crie novas instâncias de Pergunta
        List<Pergunta> perguntasInstrutor = new ArrayList<>();
        Pergunta p1a = new Pergunta();
        p1a.setTexto("Como você avalia o domínio do conteúdo pelo instrutor?");
        p1a.setTipo(Pergunta.TipoPergunta.OUTRO);
        Pergunta p2a = new Pergunta();
        p2a.setTexto("Qual foi sua frequência no curso?");
        p2a.setTipo(Pergunta.TipoPergunta.FREQUENCIA);
        perguntasInstrutor.add(p1a);
        perguntasInstrutor.add(p2a);

        formularioService.criarFormulario("Avaliação do Instrutor", perguntasInstrutor);

        List<Pergunta> perguntasCurso = new ArrayList<>();
        Pergunta p1b = new Pergunta();
        p1b.setTexto("Como você avalia o domínio do conteúdo pelo instrutor?");
        p1b.setTipo(Pergunta.TipoPergunta.OUTRO);
        Pergunta p2b = new Pergunta();
        p2b.setTexto("Qual foi sua frequência no curso?");
        p2b.setTipo(Pergunta.TipoPergunta.FREQUENCIA);
        perguntasCurso.add(p1b);
        perguntasCurso.add(p2b);

        formularioService.criarFormulario("Avaliação do Curso", perguntasCurso);

        // 6. Criar algumas Avaliações
        // Para simulação, não associe o Formulario diretamente, pois o método criarFormulario retorna o Formulario persistido
        // Se necessário, busque o Formulario pelo título ou ID

        System.out.println("DataLoader finalizado: dados de teste inseridos com sucesso!");
    }

    private Instrutor resolveInstrutor(Instrutor novo) {
        var existente = usuarioService.buscarPorEmail(novo.getEmail()).orElse(null);
        if (existente == null) return (Instrutor) usuarioService.salvarUsuario(novo);
        if (existente instanceof Instrutor i) return i;
        System.out.println("[DataLoader] Email " + novo.getEmail() + " já utilizado por outro tipo de usuário. Pulando criação do Instrutor.");
        return novo; // retorna objeto local apenas para seguir fluxo, não persistido
    }

    private Aluno resolveAluno(Aluno novo) {
        var existente = usuarioService.buscarPorEmail(novo.getEmail()).orElse(null);
        if (existente == null) return (Aluno) usuarioService.salvarUsuario(novo);
        if (existente instanceof Aluno a) return a;
        System.out.println("[DataLoader] Email " + novo.getEmail() + " já utilizado por outro tipo de usuário. Pulando criação do Aluno.");
        return novo; // retorna objeto local apenas para seguir fluxo, não persistido
    }
}
