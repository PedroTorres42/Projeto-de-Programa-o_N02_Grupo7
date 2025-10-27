package br.com.unit.modulo_avaliacao_relatorio.Service;

import br.com.unit.modulo_avaliacao_relatorio.Modelos.Avaliacao;
import br.com.unit.modulo_avaliacao_relatorio.Modelos.Resposta;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.AvaliacaoRepositorio;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.RespostaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepositorio avaliacaoRepositorio;
    private final RespostaRepositorio respostaRepositorio;

    public Avaliacao salvarAvaliacao(Long avaliacaoId) {
        Avaliacao avaliacao = avaliacaoRepositorio.findById(avaliacaoId)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));

        if (avaliacao.getData() == null) {
            avaliacao.setData(LocalDate.now());
        }

        List<Resposta> respostas = respostaRepositorio.buscarRespostasPorAvaliacao(avaliacaoId);

        if (!respostas.isEmpty()) {
            double mediaCalculada = respostas.stream()
                    .filter(r -> r.getNota() != null)
                    .mapToDouble(Resposta::getNota)
                    .average()
                    .orElse(0.0);
            avaliacao.setMedia(mediaCalculada);

            String comentarios = respostas.stream()
                    .map(r -> r.getPergunta().getTexto() + ": " + r.getNota())
                    .reduce((c1, c2) -> c1 + "; " + c2)
                    .orElse("");
            avaliacao.setComentario(comentarios);
        }

        if (avaliacao.getMedia() < 0 || avaliacao.getMedia() > 10) {
            throw new IllegalArgumentException("A média deve ser entre 0 e 10.");
        }
        if (avaliacao.getComentario() == null || avaliacao.getComentario().isEmpty()) {
            throw new IllegalArgumentException("O comentário não pode estar vazio.");
        }

        return avaliacaoRepositorio.save(avaliacao);
    }

    public List<Avaliacao> listarAvaliacoes() {
        return avaliacaoRepositorio.findAll();
    }

    public Avaliacao buscarPorId(Long id) {
        return avaliacaoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada para ID: " + id));
    }

    public void deletarAvaliacao(Long id) {
        Avaliacao avaliacao = buscarPorId(id);
        avaliacaoRepositorio.delete(avaliacao);
    }
}
