package com.localgig.payment;

import com.localgig.project.Project;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_payment_records", uniqueConstraints =
        @UniqueConstraint(name = "uk_project_payment_project", columnNames = "project_id"))
public class ProjectPaymentRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal commission;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal freelancerAmount;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(nullable = false, unique = true, length = 40)
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime paidAt;

    protected ProjectPaymentRecord() { }

    public ProjectPaymentRecord(Project project, BigDecimal amount) {
        this.project = project;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.commission = this.amount.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        this.freelancerAmount = this.amount.subtract(this.commission);
        this.invoiceNumber = "LG-P" + project.getId() + "-" + System.currentTimeMillis();
    }

    public void markPaid() { status = "PAID"; paidAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getCommission() { return commission; }
    public BigDecimal getFreelancerAmount() { return freelancerAmount; }
    public String getStatus() { return status; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
}
