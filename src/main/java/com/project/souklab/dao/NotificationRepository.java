package com.project.souklab.dao;

import com.project.souklab.model.Notification;
import com.project.souklab.model.NotificationType;
import com.project.souklab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Optional<Notification> findFirstByUserAndTypeAndTargetIdOrderByCreatedAtDesc(User user, NotificationType type, String targetId);
    Optional<Notification> findByIdAndUser(String id, User user);
}
