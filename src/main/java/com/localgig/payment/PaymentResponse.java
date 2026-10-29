package com.localgig.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, Long applicationId, Long projectId, String projectTitle, String invoiceNumber,
                              BigDecimal amount, BigDecimal commission, BigDecimal freelancerAmount,
                              String status, LocalDateTime createdAt, LocalDateTime paidAt) {
    public static PaymentResponse from(PaymentRecord p) {
        return new PaymentResponse(p.getId(), p.getApplication().getId(), null, p.getApplication().getOpportunity().getTitle(),
                p.getInvoiceNumber(), p.getAmount(), p.getCommission(), p.getFreelancerAmount(),
                p.getStatus(), p.getCreatedAt(), p.getPaidAt());
    }

    public static PaymentResponse from(ProjectPaymentRecord p) {
        return new PaymentResponse(p.getId(), null, p.getProject().getId(), p.getProject().getProposal().getOpportunity().getTitle(),
                p.getInvoiceNumber(), p.getAmount(), p.getCommission(), p.getFreelancerAmount(),
                p.getStatus(), p.getCreatedAt(), p.getPaidAt());
    }
}
