package com.microshoppe.erp.authentication.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.microshoppe.erp.address.model.Address;
import com.microshoppe.erp.authentication.enums.AccessLevel;
import com.microshoppe.erp.authentication.enums.UserRole;
import com.microshoppe.erp.store.model.Store;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "tb_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "user-store")
    @ToString.Exclude
    private Store store;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-address")
    @ToString.Exclude
    private List<Address> addresses;

    @Column(unique = true)
    private String merchantCode;
    private String name;
    private String email;
    private String password;
    private String telephone;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private AccessLevel accessLevel;

    private String resetToken;

    private LocalDateTime resetTokenExpiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean active;
    private boolean anonymous;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UserRole.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("SYSTEM_ADMIN"),
                    new SimpleGrantedAuthority("ADMIN"),
                    new SimpleGrantedAuthority("USER")
            );
        } else {
            return List.of(new SimpleGrantedAuthority("USER"));
        }
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
