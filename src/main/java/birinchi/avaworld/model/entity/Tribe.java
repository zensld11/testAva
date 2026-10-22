package birinchi.avaworld.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "tribes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Comment("Название племени")
    @Column(name = "tribe_name")
    private String tribeName;

    @Comment("Местоположение")
    @Column(name = "location")
    private String location;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality")
    private Nation nation;
}