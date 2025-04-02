package com.softuni.event.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phone;

    @JsonManagedReference(value = "user-roles")
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserRoleEntity> roles = new HashSet<>();

    @JsonBackReference(value = "user-organized-events")
    @OneToMany(mappedBy = "organizer")
    private Set<EventEntity> organizedEvents = new HashSet<>();

    @JsonBackReference(value = "user-attended-events")
    @ManyToMany(mappedBy = "attendees")
    private Set<EventEntity> attendedEvents = new HashSet<>();

    @JsonBackReference(value = "user-tickets")
    @OneToMany(mappedBy = "user")
    private Set<TicketEntity> tickets = new HashSet<>();

    @JsonBackReference(value = "user-payments")
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