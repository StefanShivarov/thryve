package bg.sofia.uni.fmi.webjava.backend.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Section extends  BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "order_number", nullable = false)
    private int orderNumber;

    @ManyToOne
    private Course course;

    @OneToMany(mappedBy = "section",cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Resource> resources = new ArrayList<>();

}
