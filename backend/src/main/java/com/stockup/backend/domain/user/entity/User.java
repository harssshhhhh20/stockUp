package com.stockup.backend.domain.user.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.user.entity.enums.AccountStatus;
import com.stockup.backend.domain.user.entity.enums.Role;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(unique = true, length = 15)
    private String phone;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<Role> roles = new HashSet<>();

    protected User() {
        // Required by JPA
    }

    public User(String email) {
        this.email = email;
        this.accountStatus = AccountStatus.PENDING_VERIFICATION;
        this.roles.add(Role.CUSTOMER);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }

    public void suspend() {
        this.accountStatus = AccountStatus.SUSPENDED;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public void verify() {

        if (accountStatus != AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException(
                    "User can only be verified from PENDING_VERIFICATION state."
            );
        }

        this.accountStatus = AccountStatus.ACTIVE;
    }
}