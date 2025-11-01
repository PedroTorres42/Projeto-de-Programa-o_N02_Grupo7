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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public RelatorioService(RelatorioRepositorio relatorioRepositorio, AvaliacaoRepositorio avaliacaoRepositorio,CursoRepositorio cursoRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.relatorioRepositorio = relatorioRepositorio;
        this.avaliacaoRepositorio = avaliacaoRepositorio;
        this.cursoRepositorio = cursoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Relatorio pegarRelatorioPorId(Long id){
        return relatorioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Relatório não encontrado"));
    }

    public Iterable<Relatorio> pegarTodosRelatorios(){
        return relatorioRepositorio.findAll();
    }

    public void salvarRelatorio(Relatorio relatorio){
        relatorioRepositorio.save(relatorio);
    }

    public void editarRelatorio(Long id){
        relatorioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Relatório nâo encontrado"));
        // TODO: implementar edição de relatório (atualizar metadados ou regenerar PDF)
    }
    public void excluirRelatorio(Long id){
        relatorioRepositorio.deleteById(id);
    }

    public Relatorio gerarRelatorioComparativoPorCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId).orElseThrow(() -> new RuntimeException("Curso não encontrado"));
        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(curso.getId());
        String nomeCurso = curso.getNome();
        byte[] pdf = montarPdfComparativo(
                "Relatório Comparativo por Curso",
                "Curso: " + nomeCurso,
                agruparPorInstrutor(avaliacoes)
        );

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setDocumento(pdf);
        return relatorioRepositorio.save(r);
    }

    public Relatorio gerarRelatorioComparativoPorInstrutor(String instrutorId) {
        Usuario i = usuarioRepositorio.findById(instrutorId).orElseThrow(() -> new RuntimeException("Instrutor não encontrado"));
        if (i.getTipoUsuario() != Usuario.TipoUsuario.Instrutor) {
            throw new IllegalArgumentException("ID provido não é de um instrutor");
        }
        List<Avaliacao> avaliacoes = obterAvaliacoesDeInstrutor(instrutorId);

        String subtitulo = "Instrutor: " + avaliacoes.stream()
                .map(Avaliacao::getInstrutor)
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
        return relatorioRepositorio.save(r);
    }

    public Relatorio gerarRelatorioAlunosDoCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId).orElseThrow(() -> new RuntimeException("Curso não encontrado"));
        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(cursoId);

        byte[] pdf = montarPdfComparativo(
                "Relatório de Alunos do Curso",
                "Curso: " + Optional.ofNullable(curso.getNome()).orElse("ID " + cursoId),
                agruparPorAluno(avaliacoes)
        );

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setDocumento(pdf);
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
        return avaliacoes.stream()
                .collect(Collectors.groupingBy(
                        keyFn,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private String chaveCurso(Avaliacao a) {
        Curso c = a.getCurso();
        if (c == null) return "Curso ?";
        String nome = c.getNome();
        return (!nome.isBlank()) ? nome : ("Curso " + c.getId());
    }

    private String chaveInstrutor(Avaliacao a) {
        Instrutor i = a.getInstrutor();
        if (i == null) return "Instrutor ?";
        String nome = i.getNome();
        return (!nome.isBlank()) ? nome : ("Instrutor " + i.getId());
    }

    private String chaveAluno(Avaliacao a) {
        Aluno al = a.getAluno();
        if (al == null) return "Aluno ?";
        String nome = al.getNome();
        return (!nome.isBlank()) ? nome : ("Aluno " + al.getId());
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
        return a.getNotas().stream()
                .filter(n -> n.getPergunta().getTipo() == Pergunta.TipoPergunta.FREQUENCIA)
                .map(Nota::getNota)
                .findFirst()
                .map(Integer::doubleValue)
                .orElse(0.0);
    }

    private Double media(List<Double> valores) {
        double soma = 0d;
        for (Double v : valores) {
            soma += v;
        }
        return soma/ valores.size();
    }

    private BigDecimal mediaPonderadaGrupo(List<Avaliacao> lista) {
        Double mediaNota = media(lista.stream()
                .map(Avaliacao::getMedia)
                .toList());
        Double mediaFreq = media(lista.stream()
                .map(this::extrairFrequenciaPercentual)
                .toList());
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

            Status Status = new Status();
            for (Avaliacao a : avaliacoes) {
                String col1 = nomeOuIdCurso(a.getCurso());
                String col2 = nomeOuIdInstrutor(a.getInstrutor());
                Metricas m = calcularMetricasLinha(a);
                adicionarLinhaDetalhada(table, col1, col2, m);
                acumular(Status, m);
            }

            db.doc.add(table);
            adicionarResumo(db.doc, db.fonts, Status);

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

            Status Status = new Status();
            for (Avaliacao a : avaliacoes) {
                String col1 = nomeOuIdAluno(a.getAluno());
                String col2 = nomeOuIdInstrutor(a.getInstrutor());
                Metricas m = calcularMetricasLinha(a);
                adicionarLinhaDetalhada(table, col1, col2, m);
                acumular(Status, m);
            }

            db.doc.add(table);
            adicionarResumo(db.doc, db.fonts, Status);

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

            Status Status = new Status();
            for (Avaliacao a : avaliacoes) {
                String col1 = nomeOuIdCurso(a.getCurso());
                String col2 = nomeOuIdAluno(a.getAluno());
                Metricas m = calcularMetricasLinha(a);
                adicionarLinhaDetalhada(table, col1, col2, m);
                acumular(Status, m);
            }

            db.doc.add(table);
            adicionarResumo(db.doc, db.fonts, Status);

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
                        .toList());

                Double mediaFreq = media(lista.stream()
                        .map(this::extrairFrequenciaPercentual)
                        .toList());

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
                adicionarCell(table, String.valueOf(mediaPond));
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
            this.doc = d; this.baos = b; this.fonts = f;
        }
    }

    private static class Metricas {
        final Double mediaNota;
        final double freq;
        final BigDecimal pond;
        final Sentimento sentimento;
        final String feedbackResumo;
        Metricas(Double mediaNota, double freq, BigDecimal pond, Sentimento sentimento, String feedbackResumo) {
            this.mediaNota = mediaNota; this.freq = freq; this.pond = pond; this.sentimento = sentimento; this.feedbackResumo = feedbackResumo;
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

    private void adicionarResumo(Document doc, Fontes fonts, Status Status) throws Exception {
        doc.add(new Paragraph("\nResumo", fonts.h2));
        BigDecimal mediaNotasGeral = BigDecimal.valueOf(media(Status.notas)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal mediaFreqsGeral = BigDecimal.valueOf(media(Status.freqs)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pondGeral = calcularMediaPonderada(mediaNotasGeral.doubleValue(), mediaFreqsGeral.doubleValue());
        doc.add(new Paragraph("Média das Notas: " + mediaNotasGeral.toPlainString(), fonts.normal));
        doc.add(new Paragraph("Média de Frequência (%): " + mediaFreqsGeral.toPlainString(), fonts.normal));
        doc.add(new Paragraph("Média Ponderada Geral: " + pondGeral.toPlainString(), fonts.normal));
        doc.add(new Paragraph("Sentimentos — Positivos: " + Status.pos + ", Neutros: " + Status.neu + ", Negativos: " + Status.neg, fonts.normal));
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
        if (a.getNotas() == null) return null;
        return a.getNotas().stream()
                .map(Nota::getNota)
                .filter(v -> v >= 1 && v <= 5)
                .findFirst()
                .orElse(null);
    }

    private enum Sentimento { POSITIVO, NEUTRO, NEGATIVO }
}
