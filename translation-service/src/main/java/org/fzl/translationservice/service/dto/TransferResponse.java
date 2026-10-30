package org.fzl.translationservice.service.dto;
/** Un virement produit deux transactions : le débit de la source et le crédit de la destination. */
public record TransferResponse(TransactionResponse debit, TransactionResponse credit) {
}
