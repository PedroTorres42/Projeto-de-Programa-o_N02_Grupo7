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
        Relatorio relatorio = relatorioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Relatório nâo encontrado"));
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
        r.setData(LocalDate.now());
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
        r.setData(LocalDate.now());
        r.setDocumento(pdf);
        return relatorioRepositorio.save(r);
    }

    private Map<String, List<Avaliacao>> agruparPorInstrutor(List<Avaliacao> avaliacoes) {
        Map<String, List<Avaliacao>> grupos = new HashMap<>();
        for (Avaliacao av : avaliacoes) {
            Instrutor i = av.getInstrutor();
            String nome = i.getNome();
            String chave = (nome != null && !nome.isBlank()) ? nome : ("Instrutor " + i.getId());
            List<Avaliacao> lista = grupos.computeIfAbsent(chave, k -> new ArrayList<>());
            lista.add(av);
        }

        return grupos;
    }

    private Map<String, List<Avaliacao>> agruparPorCurso(List<Avaliacao> avaliacoes) {
        Map<String, List<Avaliacao>> grupos = new HashMap<>();
        for (Avaliacao av : avaliacoes) {
            Curso c = av.getCurso();
            String nome = c.getNome();
            String chave = (nome != null && !nome.isBlank()) ? nome : ("Curso " + c.getId());

            List<Avaliacao> lista = grupos.computeIfAbsent(chave, k -> new ArrayList<>());
            lista.add(av);
        }
        return grupos;
    }

    private List<Avaliacao> obterAvaliacoesDeInstrutor(String instrutorId) {
        return avaliacaoRepositorio.findByInstrutorID(instrutorId);
    }
    private List<Avaliacao> obterAvaliacoesDeCurso(Long cursoID) {
        return avaliacaoRepositorio.findByCursoID(cursoID);
    }

    private BigDecimal calcularMediaPonderada(double nota, double freqPercent) {
        double wNota = 0.7;
        double wFreq = 0.3;
        double freqNormalizada = Math.max(0.0, Math.min(100.0, freqPercent)) / 10.0;
        double somaPesos = wNota + wFreq;
        double valor = (wNota * nota + wFreq * freqNormalizada) / somaPesos;
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private double extrairFrequenciaPercentual(Avaliacao a) {
        Optional<Resposta> r = a.getRespostas().stream()
                .filter(resp -> {
                    String pergunta = resp.getPergunta() != null ? resp.getPergunta().getTexto() : null;
                    return pergunta != null && pergunta.toLowerCase(Locale.ROOT).contains("frequ");
                })
                .findFirst();
        if (r.isPresent()) {
            try {
                String valor = String.valueOf(r.get().getNota());
                String limpo = valor.replace("%", "").trim().replace(",", ".");
                return Double.parseDouble(limpo);
            } catch (Exception ignore) {
                return 0d;
            }
        }
        return 0d;
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
                    Sentimento sTexto = analisarSentimentoTexto(a.getComentario());
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


    // TODO: Trocar para analise de sentimento com IA(Se for mais simples)
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
        if (a.getRespostas() == null) return null;
        return a.getRespostas().stream()
                .map(Resposta::getNota)
                .filter(Objects::nonNull)
                .filter(v -> v >= 1 && v <= 5)
                .findFirst()
                .orElse(null);
    }

    private enum Sentimento { POSITIVO, NEUTRO, NEGATIVO }
}
