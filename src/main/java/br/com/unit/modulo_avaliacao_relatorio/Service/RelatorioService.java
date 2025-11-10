package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.*;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final RelatorioRepositorio relatorioRepositorio;
    private final AvaliacaoRepositorio avaliacaoRepositorio;
    private final CursoRepositorio cursoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final NotaRespositorio notaRespositorio;

    private static final float[] WIDTHS_DETALHADO = new float[]{2.5f, 2.2f, 1.2f, 1.2f, 1.3f, 1.2f, 4f};

    @Transactional(readOnly = true)
    public Relatorio pegarRelatorioPorId(Long id) {
        return relatorioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Relatório não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Relatorio> pegarTodosRelatorios() {
        return relatorioRepositorio.findAllOrderByDataDesc();
    }

    @Transactional
    public void salvarRelatorio(Relatorio relatorio) {
        relatorioRepositorio.save(relatorio);
    }

    @Transactional
    public Relatorio editarRelatorio(Long id, Relatorio novosDados) {
        Relatorio existente = relatorioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Relatório não encontrado"));

        if (novosDados.getTipo() != null) {
            existente.setTipo(novosDados.getTipo());
        }
        if (novosDados.getDocumento() != null && !novosDados.getDocumento().isEmpty()) {
            existente.setDocumento(novosDados.getDocumento());
        }

        existente.setData(LocalDate.now());
        return relatorioRepositorio.save(existente);
    }

    @Transactional
    public void excluirRelatorio(Long id) {
        relatorioRepositorio.deleteById(id);
    }


    @Transactional(readOnly = true)
    public List<Relatorio> filtrarRelatoriosPorAluno() {
        return relatorioRepositorio.findByTipoOrderByDataDesc(Relatorio.TipoRelatorio.ALUNO);
    }


    @Transactional(readOnly = true)
    public List<Relatorio> filtrarRelatoriosPorInstrutor() {
        return relatorioRepositorio.findByTipoOrderByDataDesc(Relatorio.TipoRelatorio.INSTRUTOR);
    }


    @Transactional(readOnly = true)
    public List<Relatorio> filtrarRelatoriosPorCurso() {
        return relatorioRepositorio.findByTipoOrderByDataDesc(Relatorio.TipoRelatorio.CURSO);
    }


    @Transactional(readOnly = true)
    public List<Relatorio> filtrarRelatoriosPorTipo(Relatorio.TipoRelatorio tipo) {
        return relatorioRepositorio.findByTipoOrderByDataDesc(tipo);
    }


    @Transactional
    public Relatorio gerarRelatorioComparativoInstrutoresPorCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));
        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(curso.getId());
        
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            throw new RuntimeException("Não há avaliações para o curso selecionado.");
        }
        
        byte[] pdf = montarPdfComparativo(
                "Relatório Comparativo por Curso",
                "Curso: " + Optional.ofNullable(curso.getNome()).orElse("ID " + cursoId),
                agruparPorInstrutor(avaliacoes)
        );

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setDescricao("Comparativo_Curso");
        r.setNomeEntidade(Optional.ofNullable(curso.getNome()).orElse("Curso_" + cursoId));
        r.setDocumento(Base64.getEncoder().encodeToString(pdf));
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    @Transactional
    public Relatorio gerarRelatorioComparativoCursosPorInstrutor(String instrutorId) {
        Usuario i = usuarioRepositorio.findById(instrutorId)
                .orElseThrow(() -> new RuntimeException("Instrutor não encontrado"));
        if (!(i instanceof Instrutor)) {
            throw new IllegalArgumentException("ID provido não é de um instrutor");
        }
        List<Avaliacao> avaliacoes = obterAvaliacoesDeInstrutor(instrutorId);
        
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            throw new RuntimeException("Não há avaliações para o instrutor selecionado.");
        }

        String subtitulo = "Instrutor: " + avaliacoes.stream()
                .map(Avaliacao::getInstrutor)
                .filter(Objects::nonNull)
                .map(Instrutor::getNome)
                .findFirst()
                .orElse("ID " + instrutorId);

        byte[] pdf = montarPdfComparativo(
                "Relatório Comparativo por Instrutor",
                subtitulo,
                agruparPorCurso(avaliacoes)
        );

        String nomeInstrutor = Optional.ofNullable(i.getNome()).orElse("Instrutor_" + instrutorId);
        
        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.INSTRUTOR);
        r.setDescricao("Comparativo_Instrutor");
        r.setNomeEntidade(nomeInstrutor);
        r.setDocumento(Base64.getEncoder().encodeToString(pdf));
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    @Transactional
    public Relatorio gerarRelatorioAluno(String alunoId) {
        Usuario u = usuarioRepositorio.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        if (!(u instanceof Aluno)) {
            throw new IllegalArgumentException("ID provido não é de um aluno");
        }

        List<Avaliacao> avaliacoes = obterAvaliacoesDeAluno(alunoId);
        
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            throw new RuntimeException("Não há avaliações para o aluno selecionado.");
        }
        
    String nomeAluno = Optional.ofNullable(u.getNome()).filter(s -> !s.isBlank()).orElse("(sem nome)");
    String subtitulo = "Aluno: " + nomeAluno + " (ID: " + u.getId() + ")";
    byte[] pdf = montarPdfDetalhado(
        "Relatório de Desempenho do Aluno",
        subtitulo,
        "Não há avaliações registradas para este aluno.",
        "Curso",
        "Instrutor",
        a -> nomeOuIdCurso(a.getCurso()),
        a -> nomeOuIdInstrutor(a.getInstrutor()),
        avaliacoes
    );

        String nomeAlunoFormatado = Optional.ofNullable(u.getNome())
            .filter(s -> !s.isBlank())
            .map(s -> s.replaceAll("\\s+", "_"))
            .orElse("Aluno_" + alunoId);

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.ALUNO);
        r.setDescricao("Individual_Aluno");
        r.setNomeEntidade(nomeAlunoFormatado);
        r.setDocumento(Base64.getEncoder().encodeToString(pdf));
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    @Transactional
    public Relatorio gerarRelatorioComparativoAlunosPorCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));
        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(cursoId);
        
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            throw new RuntimeException("Não há avaliações para o curso selecionado.");
        }

    byte[] pdf = montarPdfComparativo(
                "Relatório de Alunos do Curso",
                "Curso: " + Optional.ofNullable(curso.getNome()).orElse("ID " + cursoId),
                agruparPorAluno(avaliacoes)
        );

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setDescricao("Alunos_Curso");
        r.setNomeEntidade(Optional.ofNullable(curso.getNome()).orElse("Curso_" + cursoId));
        r.setDocumento(Base64.getEncoder().encodeToString(pdf));
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }


    private Map<String, List<Avaliacao>> agruparPorInstrutor(List<Avaliacao> avaliacoes) {
        return agrupar(avaliacoes, this::chaveInstrutor);
    }

    private Map<String, List<Avaliacao>> agruparPorCurso(List<Avaliacao> avaliacoes) {
        return agrupar(avaliacoes, this::chaveCurso);
    }

    private Map<String, List<Avaliacao>> agruparPorAluno(List<Avaliacao> avaliacoes) {
        return agrupar(avaliacoes, this::chaveAluno);
    }

    private Map<String, List<Avaliacao>> agrupar(List<Avaliacao> avaliacoes, Function<Avaliacao, String> keyFn) {
        if (avaliacoes == null) return Collections.emptyMap();
        return avaliacoes.stream()
                .collect(Collectors.groupingBy(
                        keyFn,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private String chaveCurso(Avaliacao a) {
        Curso c = a == null ? null : a.getCurso();
        return c != null ? nomeOuIdCurso(c) : "Curso ?";
    }

    private String chaveInstrutor(Avaliacao a) {
        Instrutor i = a == null ? null : a.getInstrutor();
        return i != null ? nomeOuIdInstrutor(i) : "Instrutor ?";
    }

    private String chaveAluno(Avaliacao a) {
        Aluno al = a == null ? null : a.getAluno();
        return al != null ? nomeOuIdAluno(al) : "Aluno ?";
    }


    private List<Avaliacao> obterAvaliacoesDeInstrutor(String instrutorId) {
        return avaliacaoRepositorio.findByInstrutorId(instrutorId);
    }

    private List<Avaliacao> obterAvaliacoesDeCurso(Long cursoID) {
        return avaliacaoRepositorio.findByCursoId(cursoID);
    }

    private List<Avaliacao> obterAvaliacoesDeAluno(String alunoId) {
        return avaliacaoRepositorio.findByAlunoId(alunoId);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarAvaliacoesDoInstrutor(String instrutorId) {
        if (instrutorId == null || instrutorId.isBlank()) return Collections.emptyList();
        return avaliacaoRepositorio.findByInstrutorIdComAssociacoes(instrutorId);
    }


    private BigDecimal calcularMediaPonderada(double nota, double freqPercent) {
        double wNota = 0.7;
        double wFreq = 0.3;
        double freqNormalizada = Math.max(0.0, Math.min(100.0, freqPercent)) / 10.0;
        double somaPesos = wNota + wFreq;
        double valor = (wNota * nota + wFreq * freqNormalizada) / somaPesos;
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private Double extrairFrequenciaPercentual(Avaliacao a) {
        if (a == null) return 0.0;
        
        return notaRespositorio.findFrequenciaByAvaliacao(a)
                .map(Integer::doubleValue)
                .orElse(0.0);
    }

    private Double media(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return 0.0;
        double soma = 0d;
        for (Double v : valores) {
            soma += (v == null ? 0d : v);
        }
        return soma / valores.size();
    }

    private BigDecimal mediaPonderadaGrupo(List<Avaliacao> lista) {
        if (lista == null || lista.isEmpty()) return BigDecimal.ZERO;
        Double mediaNota = media(lista.stream()
                .map(Avaliacao::getMedia)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        Double mediaFreq = media(lista.stream()
                .map(this::extrairFrequenciaPercentual)
                .collect(Collectors.toList()));
        return calcularMediaPonderada(
                mediaNota,
                mediaFreq
        );
    }


    @Transactional
    public Relatorio gerarRelatorioCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));

        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(cursoId);
        
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            throw new RuntimeException("Não há avaliações para o curso selecionado.");
        }
        
        byte[] pdf = montarPdfCurso(curso, avaliacoes);

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setDescricao("Detalhado_Curso");
        r.setNomeEntidade(Optional.ofNullable(curso.getNome()).orElse("Curso_" + cursoId));
        r.setDocumento(Base64.getEncoder().encodeToString(pdf));
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    @Transactional
    public Relatorio gerarRelatorioDetalhadoInstrutor(String instrutorId) {
        Usuario u = usuarioRepositorio.findById(instrutorId)
                .orElseThrow(() -> new RuntimeException("Instrutor não encontrado"));
        if (!(u instanceof Instrutor)) {
            throw new IllegalArgumentException("ID provido não é de um instrutor");
        }

        List<Avaliacao> avaliacoes = obterAvaliacoesDeInstrutor(instrutorId);
        
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            throw new RuntimeException("Não há avaliações para o instrutor selecionado.");
        }
        
        byte[] pdf = montarPdfInstrutor(u, avaliacoes);

        String nomeInstrutorFormatado = Optional.ofNullable(u.getNome())
            .filter(s -> !s.isBlank())
            .map(s -> s.replaceAll("\\s+", "_"))
            .orElse("Instrutor_" + instrutorId);

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.INSTRUTOR);
        r.setDescricao("Detalhado_Instrutor");
        r.setNomeEntidade(nomeInstrutorFormatado);
        r.setDocumento(Base64.getEncoder().encodeToString(pdf));
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    private byte[] montarPdfCurso(Curso curso, List<Avaliacao> avaliacoes) {
        String nomeCurso = Optional.ofNullable(curso.getNome()).filter(s -> !s.isBlank()).orElse("(sem nome)");
        String subtitulo = "Curso: " + nomeCurso + " (ID: " + curso.getId() + ")";
        
        List<MediaPorTipoPergunta> mediasPorTipo = notaRespositorio.mediasPorTipoPerguntaCurso(curso.getId());
        
        return montarPdfDetalhado(
                "Relatório Detalhado do Curso",
                subtitulo,
                "Não há avaliações registradas para este curso.",
                "Aluno",
                "Instrutor",
                a -> nomeOuIdAluno(a.getAluno()),
                a -> nomeOuIdInstrutor(a.getInstrutor()),
                avaliacoes,
                mediasPorTipo
        );
    }

    private byte[] montarPdfInstrutor(Usuario instrutor, List<Avaliacao> avaliacoes) {
        String nomeInstrutor = Optional.ofNullable(instrutor.getNome()).filter(s -> !s.isBlank()).orElse("(sem nome)");
        String subtitulo = "Instrutor: " + nomeInstrutor + " (ID: " + instrutor.getId() + ")";
        
        List<MediaPorTipoPergunta> mediasPorTipo = notaRespositorio.mediasPorTipoPerguntaInstrutor(instrutor.getId());
        
        return montarPdfDetalhado(
                "Relatório Detalhado do Instrutor",
                subtitulo,
                "Não há avaliações registradas para este instrutor.",
                "Curso",
                "Aluno",
                a -> nomeOuIdCurso(a.getCurso()),
                a -> nomeOuIdAluno(a.getAluno()),
                avaliacoes,
                mediasPorTipo
        );
    }

    private byte[] montarPdfDetalhado(String titulo,
                                      String subtitulo,
                                      String mensagemVazio,
                                      String col1Label,
                                      String col2Label,
                                      Function<Avaliacao, String> col1Fn,
                                      Function<Avaliacao, String> col2Fn,
                                      List<Avaliacao> avaliacoes) {
        return montarPdfDetalhado(titulo, subtitulo, mensagemVazio, col1Label, col2Label, 
                                  col1Fn, col2Fn, avaliacoes, null);
    }

    private byte[] montarPdfDetalhado(String titulo,
                                      String subtitulo,
                                      String mensagemVazio,
                                      String col1Label,
                                      String col2Label,
                                      Function<Avaliacao, String> col1Fn,
                                      Function<Avaliacao, String> col2Fn,
                                      List<Avaliacao> avaliacoes,
                                      List<MediaPorTipoPergunta> mediasPorTipo) {
        try {
            Doc db = iniciarPdf();
            adicionarTituloCabecalho(db.doc(), db.fonts(), titulo, subtitulo);

            if (avaliacoes == null || avaliacoes.isEmpty()) {
                db.doc().add(new Paragraph(mensagemVazio, db.fonts().normal()));
                db.doc().close();
                return db.baos().toByteArray();
            }

            String[] headers = new String[]{col1Label, col2Label, "Média Nota", "Frequência %", "Média Pond", "Sentimento", "Feedback"};
            PdfPTable table = criarTabelaDetalhada(WIDTHS_DETALHADO, headers);

            Status status = new Status();
            for (Avaliacao a : avaliacoes) {
                String col1 = col1Fn.apply(a);
                String col2 = col2Fn.apply(a);
                Metricas m = calcularMetricasLinha(a);
                adicionarLinhaDetalhada(table, col1, col2, m);
                acumular(status, m);
            }

            db.doc().add(table);
            adicionarResumo(db.doc(), db.fonts(), status);
            
            if (mediasPorTipo != null && !mediasPorTipo.isEmpty()) {
                adicionarMediasPorTipoPergunta(db.doc(), db.fonts(), mediasPorTipo);
            }

            db.doc().close();
            return db.baos().toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    private byte[] montarPdfComparativo(String titulo, String subtitulo, Map<String, List<Avaliacao>> grupos) {
        try {
            Doc db = iniciarPdf();
            adicionarTituloCabecalho(db.doc(), db.fonts(), titulo, subtitulo);

            float[] widths = new float[]{3f, 1.2f, 1.2f, 1.2f, 1f, 1f, 1f, 1f};
            String[] headers = {"Grupo", "Média Nota", "Média Freq%", "Média Pond", "Positivos", "Neutros", "Negativos", "Total"};
            PdfPTable table = criarTabelaDetalhada(widths, headers);

            List<Map.Entry<String, List<Avaliacao>>> ordenado = new ArrayList<>(grupos.entrySet());
            ordenado.sort((e1, e2) -> mediaPonderadaGrupo(e2.getValue()).compareTo(mediaPonderadaGrupo(e1.getValue())));

            for (Map.Entry<String, List<Avaliacao>> e : ordenado) {
                String nomeGrupo = e.getKey();
                List<Avaliacao> lista = e.getValue();

                Double mediaNota = media(lista.stream()
                        .map(a -> Optional.ofNullable(a.getMedia()).orElse(0d))
                        .collect(Collectors.toList()));

                Double mediaFreq = media(lista.stream()
                        .map(this::extrairFrequenciaPercentual)
                        .collect(Collectors.toList()));

                BigDecimal mediaPond = calcularMediaPonderada(mediaNota, mediaFreq);

                int pos = 0, neu = 0, neg = 0;
                for (Avaliacao a : lista) {
                    Sentimento sTexto = analisarSentimentoTexto(Optional.ofNullable(a.getFeedback()).map(Feedback::getComentario).orElse(null));
                    Sentimento sNum = analisarSentimentoNumero(extrairFeedbackNumerico(a));
                    Sentimento sGeral = combinarSentimento(sTexto, sNum);
                    if (sGeral == Sentimento.POSITIVO) pos++;
                    else if (sGeral == Sentimento.NEUTRO) neu++;
                    else neg++;
                }

                adicionarCell(table, nomeGrupo);
                adicionarCell(table, String.valueOf(mediaNota));
                adicionarCell(table, String.valueOf(mediaFreq));
                adicionarCell(table, mediaPond.toPlainString());
                adicionarCell(table, String.valueOf(pos));
                adicionarCell(table, String.valueOf(neu));
                adicionarCell(table, String.valueOf(neg));
                adicionarCell(table, String.valueOf(lista.size()));
            }
            db.doc().add(table);
            db.doc().close();
            return db.baos().toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }


    private void adicionarHeader(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Paragraph(texto, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void adicionarCell(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Paragraph(texto, new Font(Font.FontFamily.HELVETICA, 9)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }


    private static class Status {
        int pos, neu, neg;
        final List<Double> notas = new ArrayList<>();
        final List<Double> freqs = new ArrayList<>();
    }

    private Doc iniciarPdf() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();
            return new Doc(doc, baos, new Fontes());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao iniciar PDF", e);
        }
    }

    private void adicionarTituloCabecalho(Document doc, Fontes fonts, String titulo, String subtitulo) throws Exception {
        Paragraph pTitulo = new Paragraph(titulo, fonts.h1());
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(pTitulo);
        doc.add(new Paragraph(subtitulo, fonts.h2()));
        doc.add(new Paragraph("Data: " + LocalDate.now(), fonts.normal()));
        doc.add(new Paragraph("\n"));
    }

    private PdfPTable criarTabelaDetalhada(float[] widths, String[] headers) throws Exception {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        for (String h : headers) adicionarHeader(table, h);
        return table;
    }

    private Metricas calcularMetricasLinha(Avaliacao a) {
        Double mediaNota = Optional.ofNullable(a.getMedia()).orElse(0d);
        double freq = extrairFrequenciaPercentual(a);
        BigDecimal pond = calcularMediaPonderada(mediaNota, freq);
        Sentimento sTexto = analisarSentimentoTexto(Optional.ofNullable(a.getFeedback()).map(Feedback::getComentario).orElse(null));
        Sentimento sNum = analisarSentimentoNumero(extrairFeedbackNumerico(a));
        Sentimento sGeral = combinarSentimento(sTexto, sNum);
        String resumoFeedback = Optional.ofNullable(a.getFeedback()).map(Feedback::getComentario)
                .map(t -> t.length() > 120 ? t.substring(0, 117) + "..." : t)
                .orElse("—");
        return new Metricas(mediaNota, freq, pond, sGeral, resumoFeedback);
    }

    private void adicionarLinhaDetalhada(PdfPTable table, String col1, String col2, Metricas m) {
        adicionarCell(table, col1);
        adicionarCell(table, col2);
        adicionarCell(table, String.valueOf(m.mediaNota()));
        adicionarCell(table, String.format(Locale.ROOT, "%.2f", m.freq()));
        adicionarCell(table, m.pond().toPlainString());
        adicionarCell(table, m.sentimento().name());
        adicionarCell(table, m.feedbackResumo());
    }

    private void acumular(Status s, Metricas m) {
        if (m.sentimento() == Sentimento.POSITIVO) s.pos++;
        else if (m.sentimento() == Sentimento.NEUTRO) s.neu++;
        else s.neg++;
        s.notas.add(m.mediaNota());
        s.freqs.add(m.freq());
    }

    private void adicionarResumo(Document doc, Fontes fonts, Status status) throws Exception {
        doc.add(new Paragraph("\nResumo", fonts.h2()));
        BigDecimal mediaNotasGeral = BigDecimal.valueOf(media(status.notas)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal mediaFreqsGeral = BigDecimal.valueOf(media(status.freqs)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pondGeral = calcularMediaPonderada(mediaNotasGeral.doubleValue(), mediaFreqsGeral.doubleValue());
        doc.add(new Paragraph("Média das Notas: " + mediaNotasGeral.toPlainString(), fonts.normal()));
        doc.add(new Paragraph("Média de Frequência (%): " + mediaFreqsGeral.toPlainString(), fonts.normal()));
        doc.add(new Paragraph("Média Ponderada Geral: " + pondGeral.toPlainString(), fonts.normal()));
        doc.add(new Paragraph("Sentimentos — Positivos: " + status.pos + ", Neutros: " + status.neu + ", Negativos: " + status.neg, fonts.normal()));
    }

    private void adicionarMediasPorTipoPergunta(Document doc, Fontes fonts, List<MediaPorTipoPergunta> medias) throws Exception {
        if (medias == null || medias.isEmpty()) return;
        
        doc.add(new Paragraph("\nMédias por Tipo de Pergunta", fonts.h2()));
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(70);
        table.setWidths(new float[]{3f, 1f});
        
        adicionarHeader(table, "Tipo de Pergunta");
        adicionarHeader(table, "Média");
        
        for (MediaPorTipoPergunta media : medias) {
            String tipoTexto = media.getTipo() != null ? formatarTipoPergunta(media.getTipo()) : "—";
            String mediaTexto = media.getMedia() != null ? 
                String.format(Locale.ROOT, "%.2f", media.getMedia()) : "—";
            adicionarCell(table, tipoTexto);
            adicionarCell(table, mediaTexto);
        }
        
        doc.add(table);
    }
    
    private String formatarTipoPergunta(Pergunta.TipoPergunta tipo) {
        switch (tipo) {
            case FREQUENCIA: return "Frequência";
            case DIDATICA: return "Didática";
            case PONTUALIDADE: return "Pontualidade";
            case ORGANIZACAO: return "Organização";
            case CONTEUDO: return "Conteúdo";
            case CARGA_HORARIA: return "Carga Horária";
            case SATISFACAO: return "Satisfação";
            case RECOMENDACAO: return "Recomendação";
            case OUTRO: return "Outro";
            default: return tipo.name();
        }
    }
    private <T> String nomeOuIdGenerico(T entidade, Function<T, String> nomeFn, Function<T, Object> idFn, String tipo) {
        if (entidade == null) return tipo + " ?";
        String nome = nomeFn.apply(entidade);
        if (nome != null && !nome.isBlank()) return nome;
        Object id = idFn.apply(entidade);
        return tipo + " " + (id != null ? id.toString() : "?");
    }

    private String nomeOuIdCurso(Curso c) {
        return nomeOuIdGenerico(c, Curso::getNome, Curso::getId, "Curso");
    }

    private String nomeOuIdInstrutor(Instrutor i) {
        return nomeOuIdGenerico(i, Instrutor::getNome, Instrutor::getId, "Instrutor");
    }

    private String nomeOuIdAluno(Aluno a) {
        return nomeOuIdGenerico(a, Aluno::getNome, Aluno::getId, "Aluno");
    }


    private Sentimento analisarSentimentoTexto(String texto) {
        if (texto == null || texto.isBlank()) return Sentimento.NEUTRO;
        String t = texto.toLowerCase(Locale.ROOT);
        String[] pos = {"excelente", "bom", "ótimo", "otimo", "positivo", "claro", "recomend", "gostei", "aprendi"};
        String[] neg = {"ruim", "péssimo", "pessimo", "negativo", "confuso", "fraco", "desorgan", "não gostei", "nao gostei"};
        int score = 0;
        for (String p : pos) if (t.contains(p)) score++;
        for (String n : neg) if (t.contains(n)) score--;
        if (score > 0) return Sentimento.POSITIVO;
        if (score < 0) return Sentimento.NEGATIVO;
        return Sentimento.NEUTRO;
    }

    private Sentimento analisarSentimentoNumero(Integer n) {
        if (n == null) return Sentimento.NEUTRO;
        if (n >= 4) return Sentimento.POSITIVO;
        if (n == 3) return Sentimento.NEUTRO;
        return Sentimento.NEGATIVO;
    }

    private Sentimento combinarSentimento(Sentimento a, Sentimento b) {
        if (a == b) return a;
        if (a == Sentimento.NEUTRO) return b;
        if (b == Sentimento.NEUTRO) return a;
        return Sentimento.NEUTRO;
    }

    private Integer extrairFeedbackNumerico(Avaliacao a) {
        if (a == null || a.getNotas() == null) return null;
        return a.getNotas().stream()
                .filter(Objects::nonNull)
                .map(Nota::getNota)
                .filter(v -> v != null && v >= 1 && v <= 5)
                .findFirst()
                .orElse(null);
    }

    public void exportarCsv(File destino) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(destino))) {
            String[] header = {"avaliacaoId", "cursoNome", "instrutorNome", "alunoNome", "notaMedia", "data"};
            writer.writeNext(header);

        List<AvaliacaoCsvRow> linhas = avaliacaoRepositorio.findCsvRows();
        linhas.stream()
            .map(row -> new String[]{
                row.avaliacaoId() != null ? String.valueOf(row.avaliacaoId()) : "",
                Optional.ofNullable(row.cursoNome()).orElse(""),
                Optional.ofNullable(row.instrutorNome()).orElse(""),
                Optional.ofNullable(row.alunoNome()).orElse(""),
                String.format(Locale.US, "%.2f", Optional.ofNullable(row.notaMedia()).orElse(0.0)),
                Optional.ofNullable(row.data()).map(Object::toString).orElse("")
            })
            .forEach(writer::writeNext);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao exportar CSV de avaliações", e);
        }
    }


    private byte[] gerarGraficoMediaPorCursoBytes(int largura, int altura) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            List<MediaPorCurso> medias = notaRespositorio.mediasPorCursoDto();
            medias.forEach(row ->
                    dataset.addValue(row.media() != null ? row.media() : 0.0, "Média", row.curso())
            );

            JFreeChart chart = ChartFactory.createBarChart(
                    "Média de Avaliações por Curso",
                    "Curso",
                    "Média (1-5)",
                    dataset
            );

            chart.setBackgroundPaint(Color.WHITE);
            chart.getPlot().setBackgroundPaint(new Color(245, 245, 250));
            chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(70, 130, 180));

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ChartUtils.writeChartAsPNG(baos, chart, largura, altura);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar gráfico", e);
        }
    }


     public void exportarPdfComGraficos(File destino) {
        exportarPdfComGraficos(destino, false);
    }

    public void exportarPdfComGraficos(File destino, boolean incluirSatisfacao) {
        try {
            try (OutputStream out = Files.newOutputStream(destino.toPath())) {
                Document document = new Document();
                PdfWriter.getInstance(document, out);
                document.open();

                Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
                Paragraph title = new Paragraph("Relatório de Avaliações - Com Gráfico", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));

                Paragraph stats = criarParagrafoAvaliacao();
                document.add(stats);
                document.add(new Paragraph(" "));
                int largura = 800;
                int altura = 400;
                
                byte[] graficoDesempenho = gerarGraficoMediaPorCursoBytes(largura, altura);
                if (graficoDesempenho.length > 0) {
                    document.add(new Paragraph("Gráfico de Desempenho (Média Ponderada)", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
                    Image img = Image.getInstance(graficoDesempenho);
                    img.scaleToFit(520, 260);
                    img.setAlignment(Element.ALIGN_CENTER);
                    document.add(img);
                    document.add(new Paragraph(" "));
                }
                
                if (incluirSatisfacao) {
                    byte[] graficoSatisfacao = gerarGraficoSatisfacaoPorCursoBytes(largura, altura);
                    if (graficoSatisfacao.length > 0) {
                        document.add(new Paragraph("Gráfico de Satisfação (Sentimento)", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
                        Image img = Image.getInstance(graficoSatisfacao);
                        img.scaleToFit(520, 260);
                        img.setAlignment(Element.ALIGN_CENTER);
                        document.add(img);
                        document.add(new Paragraph(" "));
                    }
                }

                document.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao exportar PDF com gráficos", e);
        }
    }

    private Paragraph criarParagrafoAvaliacao() {
        long totalAvaliacoes = avaliacaoRepositorio.totalAvaliacoes();
        Double media = notaRespositorio.mediaGeralNotas();
        double mediaGeral = media != null ? media : 0.0;
        return new Paragraph(String.format(Locale.US, "Total de avaliações: %d\nMédia geral: %.2f", totalAvaliacoes, mediaGeral));
    }

    @Transactional(readOnly = true)
    public void exportarPdfDaAvaliacao(Long avaliacaoId, File destino) {
        Avaliacao a = avaliacaoRepositorio.findById(avaliacaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
        byte[] pdf = montarPdfAvaliacaoUnica(a);
        try {
            Path p = destino.toPath();
            Files.createDirectories(p.getParent() != null ? p.getParent() : destino.toPath().toAbsolutePath().getParent());
            Files.write(p, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar PDF da avaliação", e);
        }
    }

    private byte[] montarPdfAvaliacaoUnica(Avaliacao a) {
        try {
            Doc db = iniciarPdf();
            String titulo = "Avaliação Individual";
            String subtitulo = String.format("Aluno: %s | Curso: %s | Instrutor: %s | ID Avaliação: %s",
                    nomeOuIdAluno(a.getAluno()),
                    nomeOuIdCurso(a.getCurso()),
                    nomeOuIdInstrutor(a.getInstrutor()),
                    Optional.ofNullable(a.getId()).map(Object::toString).orElse("—"));

            adicionarTituloCabecalho(db.doc(), db.fonts(), titulo, subtitulo);

            List<Nota> notas = Optional.ofNullable(a.getNotas()).orElse(Collections.emptyList());
            // Separar frequência das demais
            List<Nota> frequencias = new ArrayList<>();
            List<Nota> outras = new ArrayList<>();
            for (Nota n : notas) {
                if (n != null && n.getPergunta() != null && n.getPergunta().getTipo() == Pergunta.TipoPergunta.FREQUENCIA) {
                    frequencias.add(n);
                } else {
                    outras.add(n);
                }
            }

            if (!outras.isEmpty()) {
                db.doc().add(new Paragraph("Notas das Perguntas", db.fonts().h2()));
                float[] widths = new float[]{5f, 1.2f};
                PdfPTable table = criarTabelaDetalhada(widths, new String[]{"Pergunta", "Nota"});
                for (Nota n : outras) {
                    String pergunta = Optional.ofNullable(n.getPergunta()).map(Pergunta::getTexto).orElse("—");
                    String notaStr = Optional.ofNullable(n.getNota()).map(Object::toString).orElse("—");
                    adicionarCell(table, pergunta);
                    adicionarCell(table, notaStr);
                }
                db.doc().add(table);
            }

            if (!frequencias.isEmpty()) {
                db.doc().add(new Paragraph("\nFrequência", db.fonts().h2()));
                float[] wf = new float[]{4f, 2f};
                PdfPTable tFreq = criarTabelaDetalhada(wf, new String[]{"Indicador", "Valor (%)"});
                for (Nota n : frequencias) {
                    String pergunta = Optional.ofNullable(n.getPergunta()).map(Pergunta::getTexto).orElse("Frequência");
                    String valor = Optional.ofNullable(n.getNota()).map(v -> v + "%").orElse("—");
                    adicionarCell(tFreq, pergunta);
                    adicionarCell(tFreq, valor);
                }
                db.doc().add(tFreq);
            }

            db.doc().add(new Paragraph("\nFeedback", db.fonts().h2()));
            String fb = Optional.ofNullable(a.getFeedback()).map(Feedback::getComentario).orElse("—");
            db.doc().add(new Paragraph(fb, db.fonts().normal()));

            if (a.getMedia() != null) {
                db.doc().add(new Paragraph("\nMédia Geral (0-10): " + a.getMedia(), db.fonts().normal()));
            }

            db.doc().close();
            return db.baos().toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF da avaliação", e);
        }
    }


    public byte[] decodificarDocumento(Relatorio relatorio) {
        if (relatorio == null || relatorio.getDocumento() == null || relatorio.getDocumento().isEmpty()) {
            throw new IllegalArgumentException("Relatório ou documento inválido");
        }
        return Base64.getDecoder().decode(relatorio.getDocumento());
    }


    @Transactional(readOnly = true)
    public void exportarDocumentoRelatorio(Long relatorioId, File destino) {
        Relatorio relatorio = pegarRelatorioPorId(relatorioId);
        byte[] pdf = decodificarDocumento(relatorio);
        try {
            Path p = destino.toPath();
            Files.createDirectories(p.getParent() != null ? p.getParent() : destino.toPath().toAbsolutePath().getParent());
            Files.write(p, pdf, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar documento do relatório", e);
        }
    }
     private byte[] gerarGraficoSatisfacaoPorCursoBytes(int largura, int altura) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            Map<String, Map<Sentimento, Long>> sentimentoPorCurso = calcularSentimentoPorCurso();
            
            sentimentoPorCurso.forEach((curso, contagens) -> {
                dataset.addValue(contagens.getOrDefault(Sentimento.POSITIVO, 0L), "Positivo", curso);
                dataset.addValue(contagens.getOrDefault(Sentimento.NEUTRO, 0L), "Neutro", curso);
                dataset.addValue(contagens.getOrDefault(Sentimento.NEGATIVO, 0L), "Negativo", curso);
            });

            JFreeChart chart = ChartFactory.createStackedBarChart(
                    "Satisfação (Sentimento) por Curso",
                    "Curso",
                    "Contagem de Avaliações",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );
            
            chart.setBackgroundPaint(Color.WHITE);
            chart.getPlot().setBackgroundPaint(new Color(245, 245, 250));
            chart.getCategoryPlot().getRenderer().setSeriesPaint(0, new Color(60, 179, 113));
            chart.getCategoryPlot().getRenderer().setSeriesPaint(1, new Color(255, 193, 7)); 
            chart.getCategoryPlot().getRenderer().setSeriesPaint(2, new Color(220, 53, 69)); 

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ChartUtils.writeChartAsPNG(baos, chart, largura, altura);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar gráfico de satisfação", e);
        }
    }

    private Map<String, Map<Sentimento, Long>> calcularSentimentoPorCurso() {
        List<Avaliacao> todasAvaliacoes = new ArrayList<>();
        avaliacaoRepositorio.findAll().forEach(todasAvaliacoes::add);

        return todasAvaliacoes.stream()
                .collect(Collectors.groupingBy(
                        a -> nomeOuIdCurso(a.getCurso()),
                        Collectors.groupingBy(
                                a -> combinarSentimento(
                                        analisarSentimentoTexto(Optional.ofNullable(a.getFeedback()).map(Feedback::getComentario).orElse(null)),
                                        analisarSentimentoNumero(extrairFeedbackNumerico(a))
                                ),
                                Collectors.counting()
                        )
                ));
    }
}
