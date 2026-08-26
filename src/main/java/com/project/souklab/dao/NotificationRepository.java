package com.project.souklab.dao;

import com.project.souklab.model.Notification;
import com.project.souklab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.project.souklab.model.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Optional<Notification> findFirstByUserAndTypeAndTargetIdOrderByCreatedAtDesc(User user, NotificationType type, Long targetId);
    Optional<Notification> findByIdAndUser(Long id, User user);
}
