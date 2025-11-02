package br.com.unit.modulo_avaliacao_relatorio.Service;


import br.com.unit.modulo_avaliacao_relatorio.Modelos.Usuario;
import br.com.unit.modulo_avaliacao_relatorio.Repositorios.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UsuarioService {


    private final UsuarioRepositorio usuarioRepositorio;


    public Usuario salvarUsuario(Usuario usuario) {
        return usuarioRepositorio.save(usuario);
    }


    public List<Usuario> listarUsuarios() {
        return usuarioRepositorio.findAll();
    }


    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepositorio.findById(id);
    }


    public void deletarUsuario(String id) {
        usuarioRepositorio.deleteById(id);
    }

    public List<Aluno> listarAlunos() {
    return usuarioRepositorio.findAll().stream()
        .filter(u -> u instanceof Aluno)
        .map(u -> (Aluno) u)
        .collect(Collectors.toList());
}

public List<Instrutor> listarInstrutores() {
    return usuarioRepositorio.findAll().stream()
        .filter(u -> u instanceof Instrutor)
        .map(u -> (Instrutor) u)
        .collect(Collectors.toList());
}
}
