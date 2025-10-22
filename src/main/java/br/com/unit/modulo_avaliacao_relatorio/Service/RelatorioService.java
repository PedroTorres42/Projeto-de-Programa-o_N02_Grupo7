package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Curso;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Instrutor;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Relatorio;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Resposta;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioService {
    private final RelatorioRepositorio relatorioRepositorio;
    private final AvaliacaoRepositorio avaliacaoRepositorio;
    private final CursoRepositorio cursoRepositorio;

    public RelatorioService(RelatorioRepositorio relatorioRepositorio, AvaliacaoRepositorio avaliacaoRepositorio,CursoRepositorio cursoRepositorio) {
        this.relatorioRepositorio = relatorioRepositorio;
        this.avaliacaoRepositorio = avaliacaoRepositorio;
        this.cursoRepositorio = cursoRepositorio;
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
        Avaliacao avaliacao = avaliacaoRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Relatório nâo encontrado"));
    }
    public void excluirRelatorio(Long id){
        relatorioRepositorio.deleteById(id);
    }

    public Relatorio gerarRelatorioComparativoPorCurso(Long cursoId) {
        Curso curso = cursoRepositorio.findById(cursoId).orElseThrow(() -> new RuntimeException("Curso não encontrado"));
        List<Avaliacao> avaliacoes = obterAvaliacoesDeCurso(curso);
        String nomeCurso = curso.getNome();
        byte[] pdf = montarPdfComparativo(
                "Relatório Comparativo por Curso",
                "Curso: " + nomeCurso,
                agruparPorInstrutor(avaliacoes)
        );

        Relatorio r = new Relatorio();
        r.setTipo(Relatorio.TipoRelatorio.CURSO);
        r.setData(LocalDate.now());
        return relatorioRepositorio.save(r);
    }

    public Relatorio gerarRelatorioComparativoPorInstrutor(String instrutorId) {
        List<Avaliacao> avaliacoes = avaliacaoRepositorio.findAll().stream()
                .filter(a -> a.getInstrutor() != null && Objects.equals(a.getInstrutor().getId(), instrutorId))
                .collect(Collectors.toList());

        String subtitulo = "Instrutor: " + avaliacoes.stream()
                .map(Avaliacao::getInstrutor)
                .filter(Objects::nonNull)
                .map(Instrutor::getNome)
                .filter(Objects::nonNull)
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
        r.setDocumento(Base64.getEncoder().encodeToString(pdf));
        return relatorioRepositorio.save(r);
    }


    private Map<String, List<Avaliacao>> agruparPorInstrutor(List<Avaliacao> avaliacoes) {
        Map<String, List<Avaliacao>> grupos = new HashMap<>();
        for (Avaliacao av : avaliacoes) {
            String chave;
            Instrutor i = av.getInstrutor();
            if (i == null) {
                chave = "Instrutor N/D";
            } else {
                String nome = i.getNome();
                chave = (nome != null && !nome.isBlank()) ? nome : ("Instrutor " + i.getId());
            }

            List<Avaliacao> lista = grupos.computeIfAbsent(chave, k -> new ArrayList<>());
            lista.add(av);
        }

        return grupos;
    }

    private Map<String, List<Avaliacao>> agruparPorCurso(List<Avaliacao> avaliacoes) {
        Map<String, List<Avaliacao>> grupos = new HashMap<>();
        for (Avaliacao av : avaliacoes) {
            String chave;
            Curso c = av.getCurso();
            if (c == null) {
                chave = "Curso N/D";
            } else {
                String nome = c.getNome();
                chave = (nome != null && !nome.isBlank()) ? nome : ("Curso " + c.getId());
            }

            List<Avaliacao> lista = grupos.computeIfAbsent(chave, k -> new ArrayList<>());
            lista.add(av);
        }
        return grupos;
    }

    private List<Avaliacao> obterAvaliacoesDeCurso(Curso curso) {
        return avaliacaoRepositorio.findAll().stream()
                .filter(a -> a.getCurso() != null && Objects.equals(a.getCurso().getId(), curso.getId()))
                .collect(Collectors.toList());
    }

    private BigDecimal calcularMediaPonderada(double nota, double freqPercent, double wNota, double wFreq) {
        double freqNormalizada = Math.max(0.0, Math.min(100.0, freqPercent)) / 10.0;
        double somaPesos = wNota + wFreq;
        if (somaPesos <= 0) return BigDecimal.ZERO;
        double valor = (wNota * nota + wFreq * freqNormalizada) / somaPesos;
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private double extrairFrequenciaPercentual(Avaliacao a) {
        if (a.getRespostas() == null) return 0d;
        Optional<Resposta> r = a.getRespostas().stream()
                .filter(Objects::nonNull)
                .filter(resp -> {
                    String pergunta = resp.getPergunta() != null ? resp.getPergunta().getTexto() : null;
                    return pergunta != null && pergunta.toLowerCase(Locale.ROOT).contains("frequ");
                })
                .findFirst();
        if (r.isPresent()) {
            try {
                String valor = String.valueOf(r.get().getNota());
                if (valor == null) return 0d;
                String limpo = valor.replace("%", "").trim().replace(",", ".");
                return Double.parseDouble(limpo);
            } catch (Exception ignore) {
                return 0d;
            }
        }
        return 0d;
    }

    private byte[] montarPdfComparativo(String titulo, String subtitulo, Map<String, List<Avaliacao>> grupos) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();

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

                BigDecimal mediaNota = media(lista.stream()
                        .map(Avaliacao::getMedia)
                        .filter(Objects::nonNull)
                        .toList());

                BigDecimal mediaFreq = media(lista.stream()
                        .map(this::extrairFrequenciaPercentual)
                        .toList());

                BigDecimal mediaPond = calcularMediaPonderada(
                        mediaNota.doubleValue(),
                        mediaFreq.doubleValue(),
                        0.7, 0.3
                );

                adicionarCell(table, nomeGrupo);
                adicionarCell(table, mediaNota.toPlainString());
                adicionarCell(table, mediaFreq.toPlainString());
                adicionarCell(table, mediaPond.toPlainString());
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

    private BigDecimal media(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return BigDecimal.ZERO;
        double soma = 0d;
        int c = 0;
        for (Double v : valores) {
            if (v != null && !v.isNaN() && !v.isInfinite()) {
                soma += v;
                c++;
            }
        }
        if (c == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(soma / c).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal mediaPonderadaGrupo(List<Avaliacao> lista) {
        BigDecimal mediaNota = media(lista.stream()
                .map(Avaliacao::getMedia)
                .filter(Objects::nonNull)
                .toList());
        BigDecimal mediaFreq = media(lista.stream()
                .map(this::extrairFrequenciaPercentual)
                .toList());
        return calcularMediaPonderada(
                mediaNota.doubleValue(),
                mediaFreq.doubleValue(),
                0.7, 0.3
        );
    }


}
