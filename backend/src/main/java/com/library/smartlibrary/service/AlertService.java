package com.library.smartlibrary.service;

import com.library.smartlibrary.entity.Alert;
import com.library.smartlibrary.entity.Book;
import com.library.smartlibrary.entity.Member;
import com.library.smartlibrary.exception.ResourceNotFoundException;
import com.library.smartlibrary.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    public Alert createAlert(Member member, Book book, String alertType, String message) {
        Alert alert = new Alert();
        alert.setMember(member);
        alert.setBook(book);
        alert.setAlertType(alertType);
        alert.setMessage(message);
        alert.setCreatedDate(LocalDateTime.now());
        alert.setReadStatus("UNREAD");
        return alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public List<Alert> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedDateDesc();
    }

    @Transactional(readOnly = true)
    public List<Alert> getUnreadAlerts() {
        return alertRepository.findByReadStatus("UNREAD");
    }

    @Transactional(readOnly = true)
    public List<Alert> getRecentAlerts() {
        return alertRepository.findTop10ByOrderByCreatedDateDesc();
    }

    public Alert markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        alert.setReadStatus("READ");
        return alertRepository.save(alert);
    }

    public void markAllAsRead() {
        List<Alert> unread = alertRepository.findByReadStatus("UNREAD");
        for (Alert a : unread) {
            a.setReadStatus("READ");
        }
        alertRepository.saveAll(unread);
    }
}
