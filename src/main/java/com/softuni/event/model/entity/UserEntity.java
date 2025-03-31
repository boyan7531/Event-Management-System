package com.softuni.event.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phone;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserRoleEntity> roles = new HashSet<>();

    @OneToMany(mappedBy = "organizer")
    private Set<EventEntity> organizedEvents = new HashSet<>();

    @ManyToMany(mappedBy = "attendees")
    private Set<EventEntity> attendedEvents = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<TicketEntity> tickets = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<PaymentEntity> payments = new HashSet<>();
    
    // Explicit getter to ensure it's available
    public Set<UserRoleEntity> getRoles() {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        return this.roles;
    }
    
    // Explicit setter for roles
    public void setRoles(Set<UserRoleEntity> roles) {
        this.roles = roles;
    }
} 