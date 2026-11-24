package br.udesc.edu.demandatech.service;

import br.udesc.edu.demandatech.repository.FuncionarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AutenticacaoService implements UserDetailsService {

    private final FuncionarioRepository funcionarioRepository;

    @Override
    public UserDetails loadUserByUsername(String matricula) throws UsernameNotFoundException {
        try {
            return funcionarioRepository.findById(Long.valueOf(matricula.trim()))
                    .orElseThrow(() -> new UsernameNotFoundException("Matrícula não encontrada"));
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Matrícula inválida");
        }
    }
}
