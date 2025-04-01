package com.softuni.event.service.impl;

import com.softuni.event.model.dto.ContactMessageDTO;
import com.softuni.event.model.entity.ContactMessageEntity;
import com.softuni.event.repository.ContactMessageRepository;
import com.softuni.event.service.ContactService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final ModelMapper modelMapper;

    public ContactServiceImpl(ContactMessageRepository contactMessageRepository, ModelMapper modelMapper) {
        this.contactMessageRepository = contactMessageRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public void saveContactMessage(String name, String email, String subject, String message) {
        ContactMessageEntity contactMessage = new ContactMessageEntity(name, email, subject, message);
        contactMessageRepository.save(contactMessage);
    }

    @Override
    public List<ContactMessageDTO> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContactMessageDTO> getUnreadMessages() {
        return contactMessageRepository.findByReadOrderByCreatedAtDesc(false)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ContactMessageDTO getMessageById(Long id) {
        return contactMessageRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with id: " + id));
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        ContactMessageEntity message = contactMessageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with id: " + id));
        
        message.setRead(true);
        contactMessageRepository.save(message);
    }

    @Override
    @Transactional
    public void deleteMessage(Long id) {
        contactMessageRepository.deleteById(id);
    }

    @Override
    public long countUnreadMessages() {
        return contactMessageRepository.countByRead(false);
    }
    
    private ContactMessageDTO mapToDTO(ContactMessageEntity entity) {
        return modelMapper.map(entity, ContactMessageDTO.class);
    }
} 