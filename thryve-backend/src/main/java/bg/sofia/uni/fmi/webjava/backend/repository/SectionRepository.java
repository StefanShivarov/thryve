package bg.sofia.uni.fmi.webjava.backend.repository;

import bg.sofia.uni.fmi.webjava.backend.model.entity.Course;
import bg.sofia.uni.fmi.webjava.backend.model.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {

    Page<Section> findSectionsByCourseIdOrderByOrderNumber(UUID courseId, Pageable pageable);

}
