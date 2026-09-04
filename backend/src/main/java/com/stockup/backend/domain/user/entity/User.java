package com.stockup.backend.domain.user.entity;

import com.stockup.backend.common.persistence.entity.AuditableEntity;
import com.stockup.backend.domain.user.entity.enums.AccountStatus;
import com.stockup.backend.domain.user.entity.enums.Role;
import com.stockup.backend.domain.user.exception.RoleAlreadyChosenException;
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

    // Uniqueness is per-role and enforced by partial indexes (V20), not by a
    // global constraint — a shopkeeper and a shopper may share one number.
    @Column(length = 15)
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
        // Deliberately no base role — see chooseRole.
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

    /**
     * Set during onboarding. A shopkeeper needs a name and number to hand an
     * order to a real person, so these stop being optional once someone is
     * actually transacting.
     */
    public void updateProfile(String firstName, String lastName, String phone) {
        if (firstName != null && !firstName.isBlank()) this.firstName = firstName.trim();
        if (lastName != null) this.lastName = lastName.isBlank() ? null : lastName.trim();
        if (phone != null && !phone.isBlank()) this.phone = phone.trim();
    }

    /** Whether we know enough about them to let them transact. */
    public boolean isProfileComplete() {
        return firstName != null && !firstName.isBlank()
                && phone != null && !phone.isBlank();
    }

    public void suspend() {
        this.accountStatus = AccountStatus.SUSPENDED;
    }

    /**
     * Picks the one side of the marketplace this account lives on, permanently.
     *
     * Shopping and shopkeeping are separate accounts, not two hats on one
     * login: a shopkeeper's Bharosa is the record of one shop's conduct, and
     * letting a person move between sides — or occupy both — would make that
     * record ambiguous about whose behaviour it describes.
     *
     * Answered once, straight after the first sign-in, and never revisited.
     * Someone who genuinely does both registers a second account; the
     * phone-number rules allow one number across the two sides precisely
     * because that is the same human.
     */
    public void chooseRole(Role role) {
        if (role != Role.CUSTOMER && role != Role.MERCHANT) {
            throw new IllegalArgumentException(
                    "An account is either a customer or a merchant."
            );
        }
        if (hasChosenRole()) {
            throw new RoleAlreadyChosenException(
                    "This account is already registered as a "
                            + getBaseRole().name().toLowerCase()
                            + ". That can't be changed."
            );
        }
        this.roles.add(role);
    }

    /** True once the account has been placed on one side of the marketplace. */
    public boolean hasChosenRole() {
        return getBaseRole() != null;
    }

    /** CUSTOMER or MERCHANT — never both, null until chosen. ADMIN is separate. */
    public Role getBaseRole() {
        if (roles.contains(Role.MERCHANT)) return Role.MERCHANT;
        if (roles.contains(Role.CUSTOMER)) return Role.CUSTOMER;
        return null;
    }

    /** Reserved for privileges layered on top of a base role, such as ADMIN. */
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