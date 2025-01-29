package com.dean.microservices.notification_service.service;

import com.dean.microservices.notification_service.order.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.kafka.config.*;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @KafkaListener(topics = "order-placed", groupId = "notification-service")
    public void listen(OrderPlacedEvent orderPlacedEvent) {
        logger.info("Received message from order-service: {}", orderPlacedEvent);
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("springshop@email.com");
            messageHelper.setTo(orderPlacedEvent.getEmail());
            messageHelper.setSubject(String.format("Your Order with OrderNumber %s is placed successfully", orderPlacedEvent.getOrderNumber()));
            messageHelper.setText(String.format("""
                            Hi,

                            Your order with order number %s is now placed successfully.

                            Best Regards,
                            Spring Shop
                            """, orderPlacedEvent.getOrderNumber()));
        };
        try {
            javaMailSender.send(messagePreparator);
            logger.info("Order Notification email sent successfully for order: {}", orderPlacedEvent.getOrderNumber());
        } catch (MailException e) {
            logger.error("Exception occurred when sending mail: {}", e.getMessage());
            throw new RuntimeException("Exception occurred when sending mail", e);
        }
    }
}
