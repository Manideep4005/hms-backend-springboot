package com.hms.entity;

import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // ADMIN, DOCTOR, RECEPTIONIST, PATIENT
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
    
    // constructors
    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    // getters & setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
