package com.citycare.service;

import com.citycare.dto.NotificationResponse;
import com.citycare.entity.Complaint;
import com.citycare.entity.Notification;
import com.citycare.entity.User;
import com.citycare.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(User user, Complaint complaint, String message) {
        if (user == null) return null;
        Notification notification = new Notification(user, complaint, message);
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    private NotificationResponse mapToResponse(Notification n) {
        Long complaintId = n.getComplaint() != null ? n.getComplaint().getId() : null;
        String complaintRef = n.getComplaint() != null ? n.getComplaint().getComplaintId() : null;
        return new NotificationResponse(
                n.getId(),
                complaintId,
                complaintRef,
                n.getMessage(),
                n.getRead(),
                n.getCreatedAt()
        );
    }
}
