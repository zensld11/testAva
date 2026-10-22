package birinchi.avaworld.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "white_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhiteListCounterparty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Comment("Название")
    @Column(name = "name")
    private String name;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] document;

    @Column(name = "front_uri")
    private String frontUri;

    @Column(name = "back_uri")
    private String backUri;
}