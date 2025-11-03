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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private final RelatorioRepositorio relatorioRepositorio;
    private final AvaliacaoRepositorio avaliacaoRepositorio;
    private final CursoRepositorio cursoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final FormularioRepositorio formularioRepositorio;

    public RelatorioService(RelatorioRepositorio relatorioRepositorio,
                            AvaliacaoRepositorio avaliacaoRepositorio,
                            CursoRepositorio cursoRepositorio,
                            UsuarioRepositorio usuarioRepositorio,
                            FormularioRepositorio formularioRepositorio) {
        this.relatorioRepositorio = relatorioRepositorio;
        this.avaliacaoRepositorio = avaliacaoRepositorio;
        this.cursoRepositorio = cursoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.formularioRepositorio = formularioRepositorio;
    }


    public Relatorio pegarRelatorioPorId(Long id) {
        return relatorioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Relatório não encontrado"));
    }

    public Iterable<Relatorio> pegarTodosRelatorios() {
        return relatorioRepositorio.findAll();
    }

    public void salvarRelatorio(Relatorio relatorio) {
        relatorioRepositorio.save(relatorio);
    }

    public Relatorio editarRelatorio(Long id, Relatorio novosDados) {
        Relatorio existente = relatorioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Relatório não encontrado"));

        if (novosDados.getTipo() != null) {
            existente.setTipo(novosDados.getTipo());
        }
        if (novosDados.getDocumento() != null && novosDados.getDocumento().length > 0) {
            existente.setDocumento(novosDados.getDocumento());
        }

        existente.setData(LocalDate.now());
        return relatorioRepositorio.save(existente);
    }

    public void excluirRelatorio(Long id) {
        relatorioRepositorio.deleteById(id);
    }


    public Relatorio gerarRelatorioComparativoPorCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));
        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(curso.getId());
        byte[] pdf = montarPdfComparativo(
                "Relatório Comparativo por Curso",
                "Curso: " + Optional.ofNullable(curso.getNome()).orElse("ID " + cursoId),
                agruparPorInstrutor(avaliacoes)
        );

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setDocumento(pdf);
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    public Relatorio gerarRelatorioComparativoPorInstrutor(String instrutorId) {
        Usuario i = usuarioRepositorio.findById(instrutorId)
                .orElseThrow(() -> new RuntimeException("Instrutor não encontrado"));
        if (i.getTipoUsuario() != Usuario.TipoUsuario.Instrutor) {
            throw new IllegalArgumentException("ID provido não é de um instrutor");
        }
        List<Avaliacao> avaliacoes = obterAvaliacoesDeInstrutor(instrutorId);

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

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.INSTRUTOR);
        r.setDocumento(pdf);
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    public Relatorio gerarRelatorioAluno(String alunoId) {
        Usuario u = usuarioRepositorio.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        if (u.getTipoUsuario() != Usuario.TipoUsuario.Aluno) {
            throw new IllegalArgumentException("ID provido não é de um aluno");
        }

        List<Avaliacao> avaliacoes = obterAvaliacoesDeAluno(alunoId);
        byte[] pdf = montarPdfAluno(u, avaliacoes);

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.ALUNO);
        r.setDocumento(pdf);
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    public Relatorio gerarRelatorioAlunosDoCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));
        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(cursoId);

        byte[] pdf = montarPdfComparativo(
                "Relatório de Alunos do Curso",
                "Curso: " + Optional.ofNullable(curso.getNome()).orElse("ID " + cursoId),
                agruparPorAluno(avaliacoes)
        );

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setDocumento(pdf);
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
        if (c == null) return "Curso ?";
        String nome = c.getNome();
        return (nome != null && !nome.isBlank()) ? nome : ("Curso " + c.getId());
    }

    private String chaveInstrutor(Avaliacao a) {
        Instrutor i = a == null ? null : a.getInstrutor();
        if (i == null) return "Instrutor ?";
        String nome = i.getNome();
        return (nome != null && !nome.isBlank()) ? nome : ("Instrutor " + i.getId());
    }

    private String chaveAluno(Avaliacao a) {
        Aluno al = a == null ? null : a.getAluno();
        if (al == null) return "Aluno ?";
        String nome = al.getNome();
        return (nome != null && !nome.isBlank()) ? nome : ("Aluno " + al.getId());
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


    private BigDecimal calcularMediaPonderada(double nota, double freqPercent) {
        double wNota = 0.7;
        double wFreq = 0.3;
        double freqNormalizada = Math.max(0.0, Math.min(100.0, freqPercent)) / 10.0;
        double somaPesos = wNota + wFreq;
        double valor = (wNota * nota + wFreq * freqNormalizada) / somaPesos;
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private Double extrairFrequenciaPercentual(Avaliacao a) {
        if (a == null || a.getNotas() == null) return 0.0;
        return a.getNotas().stream()
                .filter(n -> n != null && n.getPergunta() != null && n.getPergunta().getTipo() == Pergunta.TipoPergunta.FREQUENCIA)
                .map(Nota::getNota)
                .filter(Objects::nonNull)
                .map(Integer::doubleValue)
                .findFirst()
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


    public Relatorio gerarRelatorioCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));

        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(cursoId);
        byte[] pdf = montarPdfCurso(curso, avaliacoes);

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setDocumento(pdf);
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    public Relatorio gerarRelatorioDetalhadoInstrutor(String instrutorId) {
        Usuario u = usuarioRepositorio.findById(instrutorId)
                .orElseThrow(() -> new RuntimeException("Instrutor não encontrado"));
        if (u.getTipoUsuario() != Usuario.TipoUsuario.Instrutor) {
            throw new IllegalArgumentException("ID provido não é de um instrutor");
        }

        List<Avaliacao> avaliacoes = obterAvaliacoesDeInstrutor(instrutorId);
        byte[] pdf = montarPdfInstrutor(u, avaliacoes);

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.INSTRUTOR);
        r.setDocumento(pdf);
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    private byte[] montarPdfAluno(Usuario aluno, List<Avaliacao> avaliacoes) {
        try {
            Doc db = iniciarPdf();
            String nomeAluno = Optional.ofNullable(aluno.getNome()).filter(s -> !s.isBlank()).orElse("(sem nome)");
            adicionarTituloCabecalho(db.doc, db.fonts, "Relatório de Desempenho do Aluno",
                    "Aluno: " + nomeAluno + " (ID: " + aluno.getId() + ")");

            if (avaliacoes == null || avaliacoes.isEmpty()) {
                db.doc.add(new Paragraph("Não há avaliações registradas para este aluno.", db.fonts.normal));
                db.doc.close();
                return db.baos.toByteArray();
            }

            float[] widths = new float[]{2.5f, 2.2f, 1.2f, 1.2f, 1.3f, 1.2f, 4f};
            PdfPTable table = criarTabelaDetalhada(widths,
                    new String[]{"Curso", "Instrutor", "Média Nota", "Frequência %", "Média Pond", "Sentimento", "Feedback"});

            Status status = new Status();
            for (Avaliacao a : avaliacoes) {
                String col1 = nomeOuIdCurso(a.getCurso());
                String col2 = nomeOuIdInstrutor(a.getInstrutor());
                Metricas m = calcularMetricasLinha(a);
                adicionarLinhaDetalhada(table, col1, col2, m);
                acumular(status, m);
            }

            db.doc.add(table);
            adicionarResumo(db.doc, db.fonts, status);

            db.doc.close();
            return db.baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do aluno", e);
        }
    }

    private byte[] montarPdfCurso(Curso curso, List<Avaliacao> avaliacoes) {
        try {
            Doc db = iniciarPdf();
            String nomeCurso = Optional.ofNullable(curso.getNome()).filter(s -> !s.isBlank()).orElse("(sem nome)");
            adicionarTituloCabecalho(db.doc, db.fonts, "Relatório Detalhado do Curso",
                    "Curso: " + nomeCurso + " (ID: " + curso.getId() + ")");

            if (avaliacoes == null || avaliacoes.isEmpty()) {
                db.doc.add(new Paragraph("Não há avaliações registradas para este curso.", db.fonts.normal));
                db.doc.close();
                return db.baos.toByteArray();
            }

            float[] widths = new float[]{2.2f, 2.2f, 1.2f, 1.2f, 1.3f, 1.2f, 4f};
            PdfPTable table = criarTabelaDetalhada(widths,
                    new String[]{"Aluno", "Instrutor", "Média Nota", "Frequência %", "Média Pond", "Sentimento", "Feedback"});

            Status status = new Status();
            for (Avaliacao a : avaliacoes) {
                String col1 = nomeOuIdAluno(a.getAluno());
                String col2 = nomeOuIdInstrutor(a.getInstrutor());
                Metricas m = calcularMetricasLinha(a);
                adicionarLinhaDetalhada(table, col1, col2, m);
                acumular(status, m);
            }

            db.doc.add(table);
            adicionarResumo(db.doc, db.fonts, status);

            db.doc.close();
            return db.baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do curso", e);
        }
    }

    private byte[] montarPdfInstrutor(Usuario instrutor, List<Avaliacao> avaliacoes) {
        try {
            Doc db = iniciarPdf();
            String nomeInstrutor = Optional.ofNullable(instrutor.getNome()).filter(s -> !s.isBlank()).orElse("(sem nome)");
            adicionarTituloCabecalho(db.doc, db.fonts, "Relatório Detalhado do Instrutor",
                    "Instrutor: " + nomeInstrutor + " (ID: " + instrutor.getId() + ")");

            if (avaliacoes == null || avaliacoes.isEmpty()) {
                db.doc.add(new Paragraph("Não há avaliações registradas para este instrutor.", db.fonts.normal));
                db.doc.close();
                return db.baos.toByteArray();
            }

            float[] widths = new float[]{2.5f, 2.2f, 1.2f, 1.2f, 1.3f, 1.2f, 4f};
            PdfPTable table = criarTabelaDetalhada(widths,
                    new String[]{"Curso", "Aluno", "Média Nota", "Frequência %", "Média Pond", "Sentimento", "Feedback"});

            Status status = new Status();
            for (Avaliacao a : avaliacoes) {
                String col1 = nomeOuIdCurso(a.getCurso());
                String col2 = nomeOuIdAluno(a.getAluno());
                Metricas m = calcularMetricasLinha(a);
                adicionarLinhaDetalhada(table, col1, col2, m);
                acumular(status, m);
            }

            db.doc.add(table);
            adicionarResumo(db.doc, db.fonts, status);

            db.doc.close();
            return db.baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do instrutor", e);
        }
    }

    private byte[] montarPdfComparativo(String titulo, String subtitulo, Map<String, List<Avaliacao>> grupos) {
        try {
            Doc db = iniciarPdf();
            adicionarTituloCabecalho(db.doc, db.fonts, titulo, subtitulo);

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

            db.doc.add(table);
            db.doc.close();
            return db.baos.toByteArray();
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

    private static class Fontes {
        final Font h1;
        final Font h2;
        final Font normal;

        Fontes() {
            this.h1 = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            this.h2 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            this.normal = new Font(Font.FontFamily.HELVETICA, 10);
        }
    }

    private static class Doc {
        final Document doc;
        final ByteArrayOutputStream baos;
        final Fontes fonts;

        Doc(Document d, ByteArrayOutputStream b, Fontes f) {
            this.doc = d;
            this.baos = b;
            this.fonts = f;
        }
    }

    private static class Metricas {
        final Double mediaNota;
        final double freq;
        final BigDecimal pond;
        final Sentimento sentimento;
        final String feedbackResumo;

        Metricas(Double mediaNota, double freq, BigDecimal pond, Sentimento sentimento, String feedbackResumo) {
            this.mediaNota = mediaNota;
            this.freq = freq;
            this.pond = pond;
            this.sentimento = sentimento;
            this.feedbackResumo = feedbackResumo;
        }
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
        Paragraph pTitulo = new Paragraph(titulo, fonts.h1);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(pTitulo);
        doc.add(new Paragraph(subtitulo, fonts.h2));
        doc.add(new Paragraph("Data: " + LocalDate.now(), fonts.normal));
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
        adicionarCell(table, String.valueOf(m.mediaNota));
        adicionarCell(table, String.format(Locale.ROOT, "%.2f", m.freq));
        adicionarCell(table, m.pond.toPlainString());
        adicionarCell(table, m.sentimento.name());
        adicionarCell(table, m.feedbackResumo);
    }

    private void acumular(Status s, Metricas m) {
        if (m.sentimento == Sentimento.POSITIVO) s.pos++;
        else if (m.sentimento == Sentimento.NEUTRO) s.neu++;
        else s.neg++;
        s.notas.add(m.mediaNota);
        s.freqs.add(m.freq);
    }

    private void adicionarResumo(Document doc, Fontes fonts, Status status) throws Exception {
        doc.add(new Paragraph("\nResumo", fonts.h2));
        BigDecimal mediaNotasGeral = BigDecimal.valueOf(media(status.notas)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal mediaFreqsGeral = BigDecimal.valueOf(media(status.freqs)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pondGeral = calcularMediaPonderada(mediaNotasGeral.doubleValue(), mediaFreqsGeral.doubleValue());
        doc.add(new Paragraph("Média das Notas: " + mediaNotasGeral.toPlainString(), fonts.normal));
        doc.add(new Paragraph("Média de Frequência (%): " + mediaFreqsGeral.toPlainString(), fonts.normal));
        doc.add(new Paragraph("Média Ponderada Geral: " + pondGeral.toPlainString(), fonts.normal));
        doc.add(new Paragraph("Sentimentos — Positivos: " + status.pos + ", Neutros: " + status.neu + ", Negativos: " + status.neg, fonts.normal));
    }

    private String nomeOuIdCurso(Curso c) {
        if (c == null) return "Curso ?";
        String nome = c.getNome();
        return (nome != null && !nome.isBlank()) ? nome : ("Curso " + c.getId());
    }

    private String nomeOuIdInstrutor(Instrutor i) {
        if (i == null) return "Instrutor ?";
        String nome = i.getNome();
        return (nome != null && !nome.isBlank()) ? nome : ("Instrutor " + i.getId());
    }

    private String nomeOuIdAluno(Aluno a) {
        if (a == null) return "Aluno ?";
        String nome = a.getNome();
        return (nome != null && !nome.isBlank()) ? nome : ("Aluno " + a.getId());
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

    private enum Sentimento { POSITIVO, NEUTRO, NEGATIVO }
    public void exportarCsv(File destino) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(destino))) {
            String[] header = {"avaliacaoId", "cursoNome", "instrutorNome", "alunoNome", "notaMedia", "data"};
            writer.writeNext(header);

            List<Avaliacao> avaliacoes = avaliacaoRepositorio.findAll();
            for (Avaliacao a : avaliacoes) {
                String id = a.getId() != null ? String.valueOf(a.getId()) : "";
                String curso = Optional.ofNullable(a.getCurso()).map(Curso::getNome).orElse("");
                String instrutor = Optional.ofNullable(a.getInstrutor()).map(Instrutor::getNome).orElse("");
                String aluno = Optional.ofNullable(a.getAluno()).map(Aluno::getNome).orElse("");
                double media = 0.0;
                if (a.getNotas() != null && !a.getNotas().isEmpty()) {
                    double soma = 0.0;
                    int cont = 0;
                    for (Nota n : a.getNotas()) {
                        if (n != null && n.getNota() != null) {
                            soma += n.getNota().doubleValue();
                            cont++;
                        }
                    }
                    if (cont > 0) media = soma / cont;
                }
                String data = Optional.ofNullable(a.getData()).map(Object::toString).orElse("");
                String[] row = {id, curso, instrutor, aluno, String.format(Locale.US, "%.2f", media), data};
                writer.writeNext(row);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao exportar CSV de avaliações", e);
        }
    }


    private byte[] gerarGraficoMediaPorCursoBytes(int largura, int altura) {
        try {
            List<Avaliacao> avaliacoes = avaliacaoRepositorio.findAll();

            Map<String, double[]> somaCont = new HashMap<>();
            for (Avaliacao a : avaliacoes) {
                String cursoNome = Optional.ofNullable(a.getCurso()).map(Curso::getNome).orElse("Curso ?");
                double soma = 0;
                int cont = 0;
                if (a.getNotas() != null) {
                    for (Nota n : a.getNotas()) {
                        if (n != null && n.getNota() != null) {
                            soma += n.getNota().doubleValue();
                            cont++;
                        }
                    }
                }
                somaCont.computeIfAbsent(cursoNome, k -> new double[]{0.0, 0.0});
                somaCont.get(cursoNome)[0] += soma;
                somaCont.get(cursoNome)[1] += cont;
            }

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String, double[]> e : somaCont.entrySet()) {
                double soma = e.getValue()[0];
                double cont = e.getValue()[1];
                double media = (cont == 0) ? 0.0 : (soma / cont);
                dataset.addValue(media, "Média", e.getKey());
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Média de Avaliações por Curso",
                    "Curso",
                    "Média (1-5)",
                    dataset
            );

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ChartUtils.writeChartAsPNG(baos, chart, largura, altura);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar gráfico", e);
        }
    }


    public void exportarPdfComGraficos(File destino) {
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

                List<Avaliacao> avaliacoes = avaliacaoRepositorio.findAll();
                int totalAvaliacoes = avaliacoes.size();
                double somaAll = 0.0;
                int countAll = 0;
                for (Avaliacao a : avaliacoes) {
                    if (a.getNotas() != null) {
                        for (Nota n : a.getNotas()) {
                            if (n != null && n.getNota() != null) {
                                somaAll += n.getNota().doubleValue();
                                countAll++;
                            }
                        }
                    }
                }
                double mediaGeral = (countAll == 0) ? 0.0 : somaAll / countAll;

                Paragraph stats = new Paragraph(
                        String.format(Locale.US, "Total de avaliações: %d\nMédia geral: %.2f", totalAvaliacoes, mediaGeral)
                );
                document.add(stats);
                document.add(new Paragraph(" "));

                byte[] grafico = gerarGraficoMediaPorCursoBytes(800, 400);
                if (grafico != null && grafico.length > 0) {
                    Image img = Image.getInstance(grafico);
                    img.scaleToFit(520, 260);
                    img.setAlignment(Element.ALIGN_CENTER);
                    document.add(img);
                }

                document.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao exportar PDF com gráficos", e);
        }
    }
}
