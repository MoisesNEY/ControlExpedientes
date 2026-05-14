package ni.edu.mney.repository;

import java.util.List;
import ni.edu.mney.domain.AppNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    List<AppNotification> findByUserLoginAndReadAtIsNullOrderByTimestampDesc(String login, Pageable pageable);

    @Modifying
    @Query("update AppNotification n set n.readAt = current_timestamp where n.user.login = :login and n.readAt is null")
    int markAllReadByUserLogin(@Param("login") String login);

    @Modifying
    @Query("update AppNotification n set n.readAt = current_timestamp where n.id = :id and n.user.login = :login and n.readAt is null")
    int markReadByIdAndUserLogin(@Param("id") Long id, @Param("login") String login);
}
