package com.localgig.payment;

import com.localgig.application.JobApplication;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_records", uniqueConstraints =
        @UniqueConstraint(name = "uk_payment_application", columnNames = "application_id"))
public class PaymentRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal commission;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal freelancerAmount;
    @Column(nullable = false, length = 20) private String status = "PENDING";
    @Column(nullable = false, unique = true, length = 40) private String invoiceNumber;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime paidAt;
    protected PaymentRecord() { }
    public PaymentRecord(JobApplication application, BigDecimal amount) {
        this.application = application;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.commission = this.amount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        this.freelancerAmount = this.amount.subtract(this.commission);
        this.invoiceNumber = "LG-" + application.getId() + "-" + System.currentTimeMillis();
    }
    public void markPaid() { status = "PAID"; paidAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public JobApplication getApplication() { return application; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getCommission() { return commission; }
    public BigDecimal getFreelancerAmount() { return freelancerAmount; }
    public String getStatus() { return status; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
