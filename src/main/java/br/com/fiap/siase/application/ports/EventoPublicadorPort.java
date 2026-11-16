package br.com.fiap.siase.application.ports;


import br.com.fiap.siase.domain.model.EventoBilling;

public interface EventoPublicadorPort {

  /** Publica atualização de status para o os-service (fila os-updates). */
  void publicarParaOs(EventoBilling evento);
}
