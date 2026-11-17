package cm.yowgifter.yow_gifter_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "donation_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
