package cm.yowgifter.yow_gifter_backend.domain;

import cm.yowgifter.yow_gifter_backend.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "requests", uniqueConstraints = {
    @UniqueConstraint(name = "uq_donation_beneficiary", columnNames = {"donation_id", "beneficiary_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private User beneficiary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private RequestStatus status = RequestStatus.WAITING;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
