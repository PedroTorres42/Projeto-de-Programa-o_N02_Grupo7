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
        Usuario u = usuarioRepositorio.findById(alunoId).orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        if (u.getTipoUsuario() != Usuario.TipoUsuario.Aluno) {
            throw new IllegalArgumentException("ID provido não é de um aluno");
        }
        List<Avaliacao> avaliacoes = obterAvaliacoesDeAluno(alunoId);

        String nomeAluno = Optional.ofNullable(u.getNome()).filter(s -> !s.isBlank()).orElse("ID " + alunoId);

        byte[] pdf = montarPdfComparativo(
                "Relatório de Desempenho do Aluno",
                "Aluno: " + nomeAluno,
                agruparPorCurso(avaliacoes)
        );

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

    public Relatorio gerarRelatorioDetalhadoAluno(String alunoId) {
        Usuario u = usuarioRepositorio.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        if (u.getTipoUsuario() != Usuario.TipoUsuario.Aluno) {
            throw new IllegalArgumentException("ID provido não é de um aluno");
        }

        List<Avaliacao> avaliacoes = obterAvaliacoesDeAluno(alunoId);
        byte[] pdf = montarPdfDetalhadoAluno(u, avaliacoes);

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.ALUNO);
        r.setDocumento(pdf);
        return relatorioRepositorio.save(r);
    }

    private byte[] montarPdfDetalhadoAluno(Usuario aluno, List<Avaliacao> avaliacoes) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font h1 = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font h2 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.FontFamily.HELVETICA, 10);

            String nomeAluno = Optional.ofNullable(aluno.getNome()).filter(s -> !s.isBlank()).orElse("(sem nome)");
            Paragraph pTitulo = new Paragraph("Relatório de Desempenho do Aluno", h1);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitulo);
            doc.add(new Paragraph("Aluno: " + nomeAluno + " (ID: " + aluno.getId() + ")", h2));
            doc.add(new Paragraph("Data: " + LocalDate.now(), normal));
            doc.add(new Paragraph("\n"));

            if (avaliacoes == null || avaliacoes.isEmpty()) {
                doc.add(new Paragraph("Não há avaliações registradas para este aluno.", normal));
                doc.close();
                return baos.toByteArray();
            }

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2.2f, 1.2f, 1.2f, 1.3f, 1.2f, 4f});

            adicionarHeader(table, "Curso");
            adicionarHeader(table, "Instrutor");
            adicionarHeader(table, "Média Nota");
            adicionarHeader(table, "Frequência %");
            adicionarHeader(table, "Média Pond");
            adicionarHeader(table, "Sentimento");
            adicionarHeader(table, "Feedback");

            int pos = 0, neu = 0, neg = 0;
            List<Double> mediasNotas = new ArrayList<>();
            List<Double> mediasFreqs = new ArrayList<>();

            for (Avaliacao a : avaliacoes) {
                String nomeCurso = Optional.ofNullable(a.getCurso()).map(Curso::getNome).filter(s -> !s.isBlank())
                        .orElseGet(() -> "Curso " + Optional.ofNullable(a.getCurso()).map(Curso::getId).orElse(null));
                String nomeInstrutor = Optional.ofNullable(a.getInstrutor()).map(Instrutor::getNome).filter(s -> !s.isBlank())
                        .orElseGet(() -> "Instrutor " + Optional.ofNullable(a.getInstrutor()).map(Instrutor::getId).orElse("?"));

                Double mediaNota = Optional.ofNullable(a.getMedia()).orElse(0d);
                double freq = extrairFrequenciaPercentual(a);
                BigDecimal pond = calcularMediaPonderada(mediaNota, freq);

                Sentimento sTexto = analisarSentimentoTexto(Optional.ofNullable(a.getFeedback()).map(Feedback::getComentario).orElse(null));
                Sentimento sNum = analisarSentimentoNumero(extrairFeedbackNumerico(a));
                Sentimento sGeral = combinarSentimento(sTexto, sNum);
                if (sGeral == Sentimento.POSITIVO) pos++; else if (sGeral == Sentimento.NEUTRO) neu++; else neg++;

                String resumoFeedback = Optional.ofNullable(a.getFeedback()).map(Feedback::getComentario)
                        .map(t -> t.length() > 120 ? t.substring(0, 117) + "..." : t)
                        .orElse("—");

                adicionarCell(table, nomeCurso);
                adicionarCell(table, nomeInstrutor);
                adicionarCell(table, String.valueOf(mediaNota));
                adicionarCell(table, String.format(Locale.ROOT, "%.2f", freq));
                adicionarCell(table, pond.toPlainString());
                adicionarCell(table, sGeral.name());
                adicionarCell(table, resumoFeedback);

                mediasNotas.add(mediaNota);
                mediasFreqs.add(freq);
            }

            doc.add(table);

            doc.add(new Paragraph("\nResumo", h2));
            BigDecimal mediaNotasGeral = BigDecimal.valueOf(media(mediasNotas)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal mediaFreqsGeral = BigDecimal.valueOf(media(mediasFreqs)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pondGeral = calcularMediaPonderada(mediaNotasGeral.doubleValue(), mediaFreqsGeral.doubleValue());
            doc.add(new Paragraph("Média das Notas: " + mediaNotasGeral.toPlainString(), normal));
            doc.add(new Paragraph("Média de Frequência (%): " + mediaFreqsGeral.toPlainString(), normal));
            doc.add(new Paragraph("Média Ponderada Geral: " + pondGeral.toPlainString(), normal));
            doc.add(new Paragraph("Sentimentos — Positivos: " + pos + ", Neutros: " + neu + ", Negativos: " + neg, normal));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do aluno", e);
        }
    }

    private byte[] montarPdfComparativo(String titulo, String subtitulo, Map<String, List<Avaliacao>> grupos) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // TODO: Mudar fontes e estilização
            Font h1 = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font h2 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normal = new Font(Font.FontFamily.HELVETICA, 10);

            Paragraph pTitulo = new Paragraph(titulo, h1);
            pTitulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTitulo);
            doc.add(new Paragraph(subtitulo, h2));
            doc.add(new Paragraph("Data: " + LocalDate.now(), normal));
            doc.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1.2f, 1.2f, 1.2f, 1f, 1f, 1f, 1f});

            adicionarHeader(table, "Grupo");
            adicionarHeader(table, "Média Nota");
            adicionarHeader(table, "Média Freq%");
            adicionarHeader(table, "Média Pond");
            adicionarHeader(table, "Positivos");
            adicionarHeader(table, "Neutros");
            adicionarHeader(table, "Negativos");
            adicionarHeader(table, "Total");

            List<Map.Entry<String, List<Avaliacao>>> ordenado = new ArrayList<>(grupos.entrySet());
            ordenado.sort((e1, e2) -> {
                BigDecimal p1 = mediaPonderadaGrupo(e1.getValue());
                BigDecimal p2 = mediaPonderadaGrupo(e2.getValue());
                return p2.compareTo(p1);
            });

            for (Map.Entry<String, List<Avaliacao>> e : ordenado) {
                String nomeGrupo = e.getKey();
                List<Avaliacao> lista = e.getValue();

                Double mediaNota = media(lista.stream()
                        .map(Avaliacao::getMedia)
                        .toList());

                Double mediaFreq = media(lista.stream()
                        .map(this::extrairFrequenciaPercentual)
                        .toList());

                BigDecimal mediaPond = calcularMediaPonderada(
                        mediaNota,
                        mediaFreq
                );

                int pos = 0, neu = 0, neg = 0;
                for (Avaliacao a : lista) {
                    Sentimento sTexto = analisarSentimentoTexto(a.getFeedback().getComentario());
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

            doc.add(table);
            doc.close();
            return baos.toByteArray();
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
