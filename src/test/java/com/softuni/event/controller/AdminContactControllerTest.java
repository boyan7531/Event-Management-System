package com.softuni.event.controller;

import com.softuni.event.model.dto.ContactMessageDTO;
import com.softuni.event.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowMessages() throws Exception {
        List<ContactMessageDTO> messages = Collections.emptyList();
        when(contactService.getAllMessages()).thenReturn(messages);
        when(contactService.countUnreadMessages()).thenReturn(0L);

        mockMvc.perform(get("/admin/messages"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-messages"))
                .andExpect(model().attribute("messages", messages))
                .andExpect(model().attribute("unreadCount", 0L))
                .andExpect(model().attribute("unreadOnly", false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowUnreadMessages() throws Exception {
        List<ContactMessageDTO> messages = Collections.emptyList();
        when(contactService.getUnreadMessages()).thenReturn(messages);
        when(contactService.countUnreadMessages()).thenReturn(0L);

        mockMvc.perform(get("/admin/messages/unread"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-messages"))
                .andExpect(model().attribute("messages", messages))
                .andExpect(model().attribute("unreadCount", 0L))
                .andExpect(model().attribute("unreadOnly", true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewMessage_MarkAsRead() throws Exception {
        long messageId = 1L;
        ContactMessageDTO message = new ContactMessageDTO();
        message.setId(messageId);
        message.setName("Sender");
        message.setEmail("sender@example.com");
        message.setSubject("Subject");
        message.setMessage("Message");
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);
        
        when(contactService.getMessageById(messageId)).thenReturn(message);
        doNothing().when(contactService).markAsRead(messageId);

        mockMvc.perform(get("/admin/messages/view/{id}", messageId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-message-detail"))
                .andExpect(model().attribute("message", message));

        verify(contactService, times(1)).markAsRead(messageId);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testViewMessage_AlreadyRead() throws Exception {
        long messageId = 2L;
        ContactMessageDTO message = new ContactMessageDTO();
        message.setId(messageId);
        message.setName("Sender2");
        message.setEmail("sender2@example.com");
        message.setSubject("Subject2");
        message.setMessage("Message2");
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(true);
        
        when(contactService.getMessageById(messageId)).thenReturn(message);

        mockMvc.perform(get("/admin/messages/view/{id}", messageId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-message-detail"))
                .andExpect(model().attribute("message", message));

        verify(contactService, never()).markAsRead(messageId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMarkAsRead_WithReturnUrl() throws Exception {
        long messageId = 3L;
        String returnUrl = "/admin/messages/unread";
        doNothing().when(contactService).markAsRead(messageId);

        mockMvc.perform(post("/admin/messages/mark-read/{id}", messageId)
                        .param("returnUrl", returnUrl)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(returnUrl));

        verify(contactService, times(1)).markAsRead(messageId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testMarkAsRead_NoReturnUrl() throws Exception {
        long messageId = 4L;
        doNothing().when(contactService).markAsRead(messageId);

        mockMvc.perform(post("/admin/messages/mark-read/{id}", messageId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/messages"));

        verify(contactService, times(1)).markAsRead(messageId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteMessage() throws Exception {
        long messageId = 5L;
        doNothing().when(contactService).deleteMessage(messageId);

        mockMvc.perform(post("/admin/messages/delete/{id}", messageId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/messages"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(contactService, times(1)).deleteMessage(messageId);
    }
} 