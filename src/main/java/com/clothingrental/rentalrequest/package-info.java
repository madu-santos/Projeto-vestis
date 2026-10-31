/**
 * Módulo de solicitações de interesse.
 *
 * <p>Responsável pelas solicitações enviadas pela vitrine pública (peça, data desejada
 * e dados de contato) e pelo seu acompanhamento no painel administrativo.
 * Uma solicitação NÃO é uma reserva e NÃO bloqueia a peça.
 *
 * <p>Depende de {@code reservation} para consultar disponibilidade e para converter
 * uma solicitação em locação oficial.
 *
 * <p>Camadas previstas: controller, dto, model, repository, service.
 */
package com.clothingrental.rentalrequest;
