package cm.yowgifter.yow_gifter_backend.domain;

import cm.yowgifter.yow_gifter_backend.domain.enums.DonationCondition;
import cm.yowgifter.yow_gifter_backend.domain.enums.DonationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "donations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DonationCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private DonationStatus status = DonationStatus.AVAILABLE;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String city = "Yaoundé";

    @Column(nullable = false, length = 150)
    private String neighborhood;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @OneToMany(mappedBy = "donation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DonationImage> images = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
