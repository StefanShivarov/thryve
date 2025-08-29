package bg.sofia.uni.fmi.webjava.backend.repository;

import bg.sofia.uni.fmi.webjava.backend.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("select n from Notification n where n.sender.id = :senderId order by n.createdAt desc")
    Page<Notification> findPageBySenderId(UUID senderId, Pageable pageable);

    @Modifying
    @Query("delete from Notification n where n.sender.id = :senderId")
    int deleteAllBySenderId(UUID senderId);
}
