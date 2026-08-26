package com.sih.procurement.service;

import com.sih.procurement.entity.Notification;
import com.sih.procurement.entity.User;
import com.sih.procurement.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
  private final NotificationRepository notifications;

  public NotificationService(NotificationRepository notifications) {
    this.notifications = notifications;
  }

  public void send(User user, String message) {
    Notification n = new Notification();
    n.user = user;
    n.message = message;
    n.createdAt = LocalDateTime.now();
    notifications.save(n);
  }

  public List<Notification> forUser(Long userId) {
    return notifications.findByUserIdOrderByCreatedAtDesc(userId);
  }
}
