package birinchi.avaworld.model.entity;

import birinchi.avaworld.model.enums.TypeOfGovernment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.util.List;

@Entity
@Table(name = "nations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Comment("Название нации")
    @Column(name = "nation_name")
    private String nationName;

    @Comment("Численность населения")
    @Column(name = "total_population")
    private Long population;

    @Comment("Первый Аватар вышедший из нации")
    @Column(name = "first_avatar")
    private String firstAvatarFromNation;

    @Comment("Вид Правления")
    @Column(name = "type_of_government")
    private TypeOfGovernment typeOfGovernment;

    @OneToMany(mappedBy = "nation")
    private List<Character> characters;
}