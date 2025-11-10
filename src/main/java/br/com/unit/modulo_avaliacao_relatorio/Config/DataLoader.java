package br.com.unit.modulo_avaliacao_relatorio.Config;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.*;
import br.com.unit.modulo_avaliacao_relatorio.Service.*;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.AvaliacaoRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Configuration
@Lazy(false)
public class DataLoader implements CommandLineRunner {

    private final UsuarioService usuarioService;
    private final CursoService cursoService;
    private final FormularioService formularioService;
    private final AvaliacaoService avaliacaoService;
    private final RelatorioService relatorioService;
    private final AvaliacaoRepositorio avaliacaoRepositorio;

    public DataLoader(
            UsuarioService usuarioService,
            CursoService cursoService,
            FormularioService formularioService,
            AvaliacaoService avaliacaoService,
            RelatorioService relatorioService,
            AvaliacaoRepositorio avaliacaoRepositorio) {
        this.usuarioService = usuarioService;
        this.cursoService = cursoService;
        this.formularioService = formularioService;
        this.avaliacaoService = avaliacaoService;
        this.relatorioService = relatorioService;
        this.avaliacaoRepositorio = avaliacaoRepositorio;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("[DataLoader] Iniciando carregamento de dados...");
        System.out.println("========================================");
        
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

        Curso cursoJava = resolveCurso("Curso Java", "Aprendizado de Java básico e avançado", 40, Collections.singletonList(instrutor1));
        Curso cursoPython = resolveCurso("Curso Python", "Aprendizado de Python básico e avançado", 35, Collections.singletonList(instrutor2));
        Curso cursoWeb = resolveCurso("Desenvolvimento Web", "HTML, CSS e JavaScript", 45, Collections.singletonList(instrutor3));

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

        Aluno aluno1b = new Aluno();
        aluno1b.setNome("Rafael Costa");
        aluno1b.setEmail("rafael.java@teste.com");
        aluno1b.setSenha("1234");
        aluno1b.setMatricula("2025101");
        aluno1b.setCursoAtual(cursoJava);

        Aluno aluno2b = new Aluno();
        aluno2b.setNome("Bianca Alves");
        aluno2b.setEmail("bianca.python@teste.com");
        aluno2b.setSenha("1234");
        aluno2b.setMatricula("2025102");
        aluno2b.setCursoAtual(cursoPython);

        Aluno aluno3b = new Aluno();
        aluno3b.setNome("Camila Nunes");
        aluno3b.setEmail("camila.web@teste.com");
        aluno3b.setSenha("1234");
        aluno3b.setMatricula("2025103");
        aluno3b.setCursoAtual(cursoWeb);

        aluno1b = resolveAluno(aluno1b);
        aluno2b = resolveAluno(aluno2b);
        aluno3b = resolveAluno(aluno3b);

        Instrutor instrutor1b = new Instrutor();
        instrutor1b.setNome("Carla Menezes");
        instrutor1b.setEmail("carla.java@teste.com");
        instrutor1b.setSenha("1234");
        instrutor1b.setEspecialidade("Java");

        Instrutor instrutor2b = new Instrutor();
        instrutor2b.setNome("Bruno Ferreira");
        instrutor2b.setEmail("bruno.python@teste.com");
        instrutor2b.setSenha("1234");
        instrutor2b.setEspecialidade("Python");

        Instrutor instrutor3b = new Instrutor();
        instrutor3b.setNome("Marina Azevedo");
        instrutor3b.setEmail("marina.web@teste.com");
        instrutor3b.setSenha("1234");
        instrutor3b.setEspecialidade("Front-end");

        instrutor1b = resolveInstrutor(instrutor1b);
        instrutor2b = resolveInstrutor(instrutor2b);
        instrutor3b = resolveInstrutor(instrutor3b);

        adicionarInstrutorAoCurso(cursoJava, instrutor1b);
        adicionarInstrutorAoCurso(cursoPython, instrutor2b);
        adicionarInstrutorAoCurso(cursoWeb, instrutor3b);

        List<Pergunta> perguntasInstrutor = new ArrayList<>();
        Pergunta p1a = new Pergunta();
        p1a.setTexto("Como você avalia o domínio do conteúdo pelo instrutor?");
        p1a.setTipo(Pergunta.TipoPergunta.CONTEUDO);
        Pergunta p2a = new Pergunta();
        p2a.setTexto("Como você avalia a frequência do instrutor no curso?");
        p2a.setTipo(Pergunta.TipoPergunta.FREQUENCIA);
        Pergunta p3a = new Pergunta();
        p3a.setTexto("Qual o nível de organização do instrutor na sua visão?");
        p3a.setTipo(Pergunta.TipoPergunta.ORGANIZACAO);
        Pergunta p4a = new Pergunta();
        p4a.setTexto("Qual a nota para a pontualidade do instrutor?");
        p4a.setTipo(Pergunta.TipoPergunta.PONTUALIDADE);
        Pergunta p5a = new Pergunta();
        p5a.setTexto("Qual o nivel de didatica do instrutor?");
        p5a.setTipo(Pergunta.TipoPergunta.DIDATICA);
        Pergunta p6a = new Pergunta();
        p6a.setTexto("Qual o seu nivel de satisfação com o instrutor?");
        p6a.setTipo(Pergunta.TipoPergunta.SATISFACAO);
        perguntasInstrutor.add(p1a);
        perguntasInstrutor.add(p2a);
        perguntasInstrutor.add(p3a);
        perguntasInstrutor.add(p4a);
        perguntasInstrutor.add(p5a);
        perguntasInstrutor.add(p6a);

        List<Pergunta> perguntasCurso = new ArrayList<>();
        Pergunta p1b = new Pergunta();
        p1b.setTexto("Como você avalia o conteúdo do curso?");
        p1b.setTipo(Pergunta.TipoPergunta.CONTEUDO);
        Pergunta p2b = new Pergunta();
        p2b.setTexto("Como você avalia a carga horária do curso?");
        p2b.setTipo(Pergunta.TipoPergunta.CARGA_HORARIA);
        Pergunta p3b = new Pergunta();
        p3b.setTexto("Você recomendaria esse curso para um amigo ou familiar?(5 - Com certaza , 1 - Nunca");
        p3b.setTipo(Pergunta.TipoPergunta.RECOMENDACAO);
        Pergunta p4b = new Pergunta();
        p4b.setTexto("Qual a sua satisfação com o curso? ");
        p4b.setTipo(Pergunta.TipoPergunta.SATISFACAO);
        perguntasCurso.add(p1b);
        perguntasCurso.add(p2b);
        perguntasCurso.add(p3b);
        perguntasCurso.add(p4b);

        var formulariosExistentes = formularioService.listarFormularios();
        boolean formInstrutorExiste = formulariosExistentes.stream()
                .anyMatch(f -> "Avaliação do Instrutor".equals(f.getTitulo()));
        boolean formCursoExiste = formulariosExistentes.stream()
                .anyMatch(f -> "Avaliação do Curso".equals(f.getTitulo()));

        if (!formInstrutorExiste) {
            formularioService.criarFormulario("Avaliação do Instrutor", perguntasInstrutor, Formulario.TipoFormulario.INSTRUTOR);
            System.out.println("[DataLoader] Formulário 'Avaliação do Instrutor' criado.");
        } else {
            System.out.println("[DataLoader] Formulário 'Avaliação do Instrutor' já existe.");
        }

        if (!formCursoExiste) {
            formularioService.criarFormulario("Avaliação do Curso", perguntasCurso, Formulario.TipoFormulario.CURSO);
            System.out.println("[DataLoader] Formulário 'Avaliação do Curso' criado.");
        } else {
            System.out.println("[DataLoader] Formulário 'Avaliação do Curso' já existe.");
        }

        
        if (avaliacaoRepositorio.totalAvaliacoes() == 0) {
            System.out.println("[DataLoader] Criando avaliações de exemplo...");
            var formularios = formularioService.listarFormularios();
            Formulario formInstrutor = formularios.stream()
                    .filter(f -> "Avaliação do Instrutor".equals(f.getTitulo()))
                    .findFirst().orElse(null);
            Formulario formCurso = formularios.stream()
                    .filter(f -> "Avaliação do Curso".equals(f.getTitulo()))
                    .findFirst().orElse(null);

            Function<Formulario, List<Nota>> gerarNotas = (form) -> {
                List<Nota> notas = new ArrayList<>();
                if (form != null && form.getPerguntas() != null) {
                    int val = 5;
                    for (Pergunta pg : form.getPerguntas()) {
                        Nota n = new Nota();
                        if (pg.getTipo() == Pergunta.TipoPergunta.FREQUENCIA) {
                            n.setNota(4);
                        } else {
                            n.setNota(val);
                        }
                        val = val == 5 ? 4 : 5;
                        n.setPergunta(pg);
                        notas.add(n);
                    }
                }
                return notas;
            };

            List<Avaliacao> avaliacaosParaSalvar = new ArrayList<>();
            List<Aluno> alunos = List.of(aluno1, aluno2, aluno3, aluno1b, aluno2b, aluno3b);
            for (Aluno al : alunos) {
                if (formCurso != null) {
                    Avaliacao avCurso = new Avaliacao();
                    avCurso.setAluno(al);
                    avCurso.setUsuario(al);
                    avCurso.setCurso(al.getCursoAtual());
                    Instrutor instrAssoc = (al.getCursoAtual() != null && al.getCursoAtual().getInstrutores() != null && !al.getCursoAtual().getInstrutores().isEmpty())
                            ? al.getCursoAtual().getInstrutores().getFirst() : null;
                    avCurso.setInstrutor(instrAssoc);
                    avCurso.setFormulario(formCurso);
                    avCurso.setNotas(gerarNotas.apply(formCurso));
                    Feedback fb = new Feedback();
                    fb.setComentario("Feedback sobre curso: excelente conteúdo e organização.");
                    fb.setUsuario(al);
                    avCurso.setFeedback(fb);
                    avaliacaosParaSalvar.add(avCurso);
                }
                if (formInstrutor != null) {
                    Avaliacao avInstrutor = new Avaliacao();
                    avInstrutor.setAluno(al);
                    avInstrutor.setUsuario(al);
                    Instrutor instrAssoc = (al.getCursoAtual() != null && al.getCursoAtual().getInstrutores() != null && !al.getCursoAtual().getInstrutores().isEmpty())
                            ? al.getCursoAtual().getInstrutores().getFirst() : null;
                    avInstrutor.setInstrutor(instrAssoc);
                    avInstrutor.setCurso(al.getCursoAtual());
                    avInstrutor.setFormulario(formInstrutor);
                    avInstrutor.setNotas(gerarNotas.apply(formInstrutor));
                    Feedback fb2 = new Feedback();
                    fb2.setComentario("Feedback sobre instrutor: ótima didática e clareza.");
                    fb2.setUsuario(al);
                    avInstrutor.setFeedback(fb2);
                    avaliacaosParaSalvar.add(avInstrutor);
                }
            }

            for (Avaliacao av : avaliacaosParaSalvar) {
                try {
                    avaliacaoService.salvarAvaliacao(av);
                } catch (Exception e) {
                    System.out.println("[DataLoader] Falha ao salvar avaliação: " + e.getMessage());
                }
            }

            System.out.println("[DataLoader] Avaliações criadas: " + avaliacaoRepositorio.totalAvaliacoes());

        
            try {
                relatorioService.gerarRelatorioCurso(cursoJava.getId());
                relatorioService.gerarRelatorioDetalhadoInstrutor(instrutor1.getId());
                relatorioService.gerarRelatorioAluno(aluno1.getId());
                System.out.println("[DataLoader] Relatórios iniciais gerados.");
            } catch (Exception e) {
                System.out.println("[DataLoader] Falha ao gerar relatórios de exemplo: " + e.getMessage());
            }
        } else {
            System.out.println("[DataLoader] Avaliações já existentes. Pulando criação de avaliações e relatórios.");
        }

        
        
        

        System.out.println("========================================");
        System.out.println("[DataLoader] Finalizado: dados de teste carregados com sucesso!");
        System.out.println("========================================");
    }

    @Transactional
    protected Instrutor resolveInstrutor(Instrutor novo) {
        var existente = usuarioService.buscarPorEmail(novo.getEmail()).orElse(null);
        if (existente == null) return (Instrutor) usuarioService.salvarUsuario(novo);
        if (existente instanceof Instrutor i) return i;
        System.out.println("[DataLoader] Email " + novo.getEmail() + " já utilizado por outro tipo de usuário. Pulando criação do Instrutor.");
    return novo;
    }

    @Transactional
    protected Aluno resolveAluno(Aluno novo) {
        var existente = usuarioService.buscarPorEmail(novo.getEmail()).orElse(null);
        if (existente == null) return (Aluno) usuarioService.salvarUsuario(novo);
        if (existente instanceof Aluno a) return a;
        System.out.println("[DataLoader] Email " + novo.getEmail() + " já utilizado por outro tipo de usuário. Pulando criação do Aluno.");
        return novo;
    }

    @Transactional
    protected Curso resolveCurso(String nome, String descricao, Integer cargaHoraria, List<Instrutor> instrutores) {
        List<Curso> cursosExistentes = cursoService.listarCursos();
        for (Curso c : cursosExistentes) {
            if (c.getNome() != null && c.getNome().equalsIgnoreCase(nome)) {
                System.out.println("[DataLoader] Curso '" + nome + "' já existe. ID: " + c.getId());
                
                if (instrutores != null && !instrutores.isEmpty()) {
                    List<Instrutor> instrutoresAtuais = c.getInstrutores() != null ? new ArrayList<>(c.getInstrutores()) : new ArrayList<>();
                    boolean atualizado = false;
                    
                    for (Instrutor novoInstr : instrutores) {
                        if (!instrutoresAtuais.contains(novoInstr)) {
                            instrutoresAtuais.add(novoInstr);
                            atualizado = true;
                        }
                    }
                    
                    if (atualizado) {
                        c.setInstrutores(instrutoresAtuais);
                        c = cursoService.criarCurso(c);
                        System.out.println("[DataLoader] Instrutores do curso '" + nome + "' atualizados.");
                    }
                }
                
                return c;
            }
        }
        
        Curso novoCurso = new Curso();
        novoCurso.setNome(nome);
        novoCurso.setDescricao(descricao);
        novoCurso.setCargaHoraria(cargaHoraria);
        novoCurso.setInstrutores(instrutores != null ? instrutores : new ArrayList<>());
        novoCurso = cursoService.criarCurso(novoCurso);
        System.out.println("[DataLoader] Curso '" + nome + "' criado. ID: " + novoCurso.getId());
        return novoCurso;
    }

    @Transactional
    protected void adicionarInstrutorAoCurso(Curso curso, Instrutor instrutor) {
        if (curso == null || instrutor == null) {
            return;
        }
        
        List<Instrutor> instrutores = curso.getInstrutores() != null ? new ArrayList<>(curso.getInstrutores()) : new ArrayList<>();
        
        boolean jaExiste = instrutores.stream()
                .anyMatch(i -> i.getId() != null && i.getId().equals(instrutor.getId()));
        
        if (!jaExiste) {
            instrutores.add(instrutor);
            curso.setInstrutores(instrutores);
            cursoService.criarCurso(curso);
            System.out.println("[DataLoader] Instrutor '" + instrutor.getNome() + "' adicionado ao curso '" + curso.getNome() + "'");
        } else {
            System.out.println("[DataLoader] Instrutor '" + instrutor.getNome() + "' já está no curso '" + curso.getNome() + "'");
        }
    }
}
