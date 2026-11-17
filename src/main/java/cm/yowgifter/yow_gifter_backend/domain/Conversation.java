package cm.yowgifter.yow_gifter_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations", uniqueConstraints = {
    @UniqueConstraint(name = "uq_conversation_donation_beneficiary", columnNames = {"donation_id", "beneficiary_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private User beneficiary;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
