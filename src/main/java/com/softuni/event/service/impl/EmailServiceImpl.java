package com.softuni.event.service.impl;

import com.softuni.event.model.entity.EventEntity;
import com.softuni.event.model.entity.UserEntity;
import com.softuni.event.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

import java.time.format.DateTimeFormatter;

/**
 * Implementation of the EmailService interface
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
    
    private final JavaMailSender emailSender;
    
    @Value("${event.notification.email.from}")
    private String fromAddress;
    
    @Value("${event.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }
    
    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Would have sent to: {} subject: {}", to, subject);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            logger.info("Email sent to: {} with subject: {}", to, subject);
        } catch (Exception e) {
            logger.error("Failed to send email to {} with subject: {}", to, subject, e);
        }
    }
    
    @Override
    public void sendHtmlMessage(String to, String subject, String htmlContent) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Would have sent HTML email to: {} subject: {}", to, subject);
            return;
        }
        
        logger.debug("Attempting to send HTML email to: {} with subject: {}", to, subject);
        
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            logger.debug("Email prepared, sending now...");
            emailSender.send(message);
            logger.info("HTML email sent successfully to: {} with subject: {}", to, subject);
        } catch (MailAuthenticationException mae) {
            logger.error("Authentication error when sending email. Please check your Gmail credentials and make sure you're using an App Password: {}", mae.getMessage());
            logger.error("If using Gmail, make sure 2-factor authentication is enabled and you've created an App Password");
        } catch (MailSendException mse) {
            logger.error("Error sending email to: {} - {}", to, mse.getMessage());
            logger.error("Detailed error information: {}", mse.getFailedMessages());
        } catch (MessagingException me) {
            logger.error("Failed to prepare email message to {} with subject: {}", to, subject, me);
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {} with subject: {}", to, subject, e);
        }
    }
    
    @Override
    public void sendEventReminder(UserEntity user, EventEntity event, boolean isOrganizer) {
        try {
            if (user == null) {
                logger.error("Cannot send email to null user");
                return;
            }
            
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                logger.warn("Cannot send email to user {}. No email address available.", 
                        user.getUsername() != null ? user.getUsername() : "unknown");
                return;
            }
            
            if (event == null) {
                logger.error("Cannot send email for null event");
                return;
            }
            
            String subject;
            String htmlContent;
            
            try {
                if (isOrganizer) {
                    subject = "Reminder: Your Event '" + event.getTitle() + "' is Happening Tomorrow";
                    htmlContent = buildOrganizerReminderEmail(user, event);
                } else {
                    subject = "Reminder: Event '" + event.getTitle() + "' is Happening Tomorrow";
                    htmlContent = buildAttendeeReminderEmail(user, event);
                }
                
                sendHtmlMessage(user.getEmail(), subject, htmlContent);
            } catch (Exception e) {
                logger.error("Error preparing email content for user {} and event {}", 
                        user.getUsername(), event.getId(), e);
            }
        } catch (Exception e) {
            logger.error("Unexpected error in sendEventReminder", e);
        }
    }
    
    private String buildOrganizerReminderEmail(UserEntity organizer, EventEntity event) {
        try {
            // Safely handle null values
            String firstName = (organizer != null && organizer.getFirstName() != null) ? 
                    organizer.getFirstName() : "Organizer";
            
            String formattedDate = "soon";
            if (event.getEventDate() != null) {
                formattedDate = event.getEventDate().format(dateFormatter);
            }
            
            String locationName = "TBA";
            String locationAddress = "TBA";
            if (event.getLocation() != null) {
                locationName = event.getLocation().getName() != null ? 
                        event.getLocation().getName() : "TBA";
                
                String address = event.getLocation().getAddress() != null ? 
                        event.getLocation().getAddress() : "";
                String city = event.getLocation().getCity() != null ? 
                        event.getLocation().getCity() : "";
                
                locationAddress = !address.isEmpty() && !city.isEmpty() ? 
                        address + ", " + city : 
                        (!address.isEmpty() ? address : (!city.isEmpty() ? city : "TBA"));
            }
            
            int attendeeCount = 0;
            if (event.getAttendees() != null) {
                attendeeCount = event.getAttendees().size();
            }
            
            // Rest of the method remains the same, but using our safe variables
            return "<html>"
                    + "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                    + "<div style='background-color: #3498db; color: white; padding: 15px; text-align: center;'>"
                    + "<h1>Event Reminder</h1>"
                    + "</div>"
                    + "<div style='padding: 20px; border: 1px solid #ddd; border-top: none;'>"
                    + "<p>Hello <strong>" + firstName + "</strong>,</p>"
                    + "<p>This is a reminder that your event is happening tomorrow:</p>"
                    + "<div style='background-color: #f8f9fa; padding: 15px; margin: 15px 0; border-left: 4px solid #3498db;'>"
                    + "<h2 style='color: #3498db; margin-top: 0;'>" + event.getTitle() + "</h2>"
                    + "<p><strong>When:</strong> " + formattedDate + "</p>"
                    + "<p><strong>Where:</strong> " + locationName + " - " + locationAddress + "</p>"
                    + "</div>"
                    + "<p>As the organizer, you should arrive early to prepare for your attendees.</p>"
                    + "<p>Currently, you have <strong>" + attendeeCount + "</strong> attendees registered for this event.</p>"
                    + "<p><a href='http://localhost:8080/events/details/" + event.getId() + "' "
                    + "style='background-color: #3498db; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px;'>"
                    + "View Event Details</a></p>"
                    + "<p>Good luck with your event!</p>"
                    + "<p>Best regards,<br>Event Management Team</p>"
                    + "</div>"
                    + "<div style='text-align: center; padding: 10px; color: #888; font-size: 12px;'>"
                    + "&copy; " + java.time.Year.now().getValue() + " Event Management System. All rights reserved."
                    + "</div>"
                    + "</body>"
                    + "</html>";
        } catch (Exception e) {
            logger.error("Error building organizer reminder email", e);
            return "<html><body><p>Event reminder for " + event.getTitle() + "</p></body></html>";
        }
    }
    
    private String buildAttendeeReminderEmail(UserEntity attendee, EventEntity event) {
        try {
            // Safely handle null values
            String firstName = (attendee != null && attendee.getFirstName() != null) ? 
                    attendee.getFirstName() : "Attendee";
            
            String formattedDate = "soon";
            if (event.getEventDate() != null) {
                formattedDate = event.getEventDate().format(dateFormatter);
            }
            
            String locationName = "TBA";
            String locationAddress = "TBA";
            if (event.getLocation() != null) {
                locationName = event.getLocation().getName() != null ? 
                        event.getLocation().getName() : "TBA";
                
                String address = event.getLocation().getAddress() != null ? 
                        event.getLocation().getAddress() : "";
                String city = event.getLocation().getCity() != null ? 
                        event.getLocation().getCity() : "";
                
                locationAddress = !address.isEmpty() && !city.isEmpty() ? 
                        address + ", " + city : 
                        (!address.isEmpty() ? address : (!city.isEmpty() ? city : "TBA"));
            }
            
            String organizerName = "Event Organizer";
            if (event.getOrganizer() != null) {
                String orgFirstName = event.getOrganizer().getFirstName() != null ? 
                        event.getOrganizer().getFirstName() : "";
                String orgLastName = event.getOrganizer().getLastName() != null ? 
                        event.getOrganizer().getLastName() : "";
                        
                organizerName = !orgFirstName.isEmpty() && !orgLastName.isEmpty() ? 
                        orgFirstName + " " + orgLastName : 
                        (!orgFirstName.isEmpty() ? orgFirstName : 
                         (!orgLastName.isEmpty() ? orgLastName : "Event Organizer"));
            }
            
            // Rest of the method remains the same, but using our safe variables
            return "<html>"
                    + "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                    + "<div style='background-color: #3498db; color: white; padding: 15px; text-align: center;'>"
                    + "<h1>Event Reminder</h1>"
                    + "</div>"
                    + "<div style='padding: 20px; border: 1px solid #ddd; border-top: none;'>"
                    + "<p>Hello <strong>" + firstName + "</strong>,</p>"
                    + "<p>This is a reminder that an event you're attending is happening tomorrow:</p>"
                    + "<div style='background-color: #f8f9fa; padding: 15px; margin: 15px 0; border-left: 4px solid #3498db;'>"
                    + "<h2 style='color: #3498db; margin-top: 0;'>" + event.getTitle() + "</h2>"
                    + "<p><strong>When:</strong> " + formattedDate + "</p>"
                    + "<p><strong>Where:</strong> " + locationName + " - " + locationAddress + "</p>"
                    + "<p><strong>Organized by:</strong> " + organizerName + "</p>"
                    + "</div>"
                    + "<p>We look forward to seeing you there!</p>"
                    + "<p><a href='http://localhost:8080/events/details/" + event.getId() + "' "
                    + "style='background-color: #3498db; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px;'>"
                    + "View Event Details</a></p>"
                    + "<p>Best regards,<br>Event Management Team</p>"
                    + "</div>"
                    + "<div style='text-align: center; padding: 10px; color: #888; font-size: 12px;'>"
                    + "&copy; " + java.time.Year.now().getValue() + " Event Management System. All rights reserved."
                    + "</div>"
                    + "</body>"
                    + "</html>";
        } catch (Exception e) {
            logger.error("Error building attendee reminder email", e);
            return "<html><body><p>Event reminder for " + event.getTitle() + "</p></body></html>";
        }
    }
} 