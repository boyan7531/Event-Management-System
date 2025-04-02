package com.softuni.event.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.softuni.event.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
public class UserRoleEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @JsonBackReference(value = "user-roles")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    public UserRoleEntity(UserRole role) {
        this.role = role;
    }
    
    // Explicit getter for role
    public UserRole getRole() {
        return this.role;
    }
    
    // Explicit setter for role
    public void setRole(UserRole role) {
        this.role = role;
    }
} 