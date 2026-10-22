package birinchi.avaworld.model.entity;

import birinchi.avaworld.model.enums.PowerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Comment("Полное имя персонажа")
    @Column(name = "full_name")
    private String fullName;

    @ManyToOne
    @JoinColumn(name = "nation_id")
    private Nation nation;
    //при выборе национальности показать племена которые связаны с этой национальностью

    @Comment("Описание персонжа")
    @Column(name = "description")
    private String descriptionOfCharacter;

    @Comment("Место рождения")
    @Column(name = "place_if_birth")
    private String placeOfBirth;

    @Comment("Возраст персонажа")
    @Column(name = "age")
    private Long age;

    @Enumerated(EnumType.STRING)
    @Column(name = "power_type")
    private PowerType powerType;
}