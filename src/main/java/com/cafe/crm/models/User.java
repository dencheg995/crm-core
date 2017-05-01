package com.cafe.crm.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "login", nullable = false, unique = true)
    private String login;


    @Column(name = "password", length = 30, nullable = false)
    private String password;

    @Column(name = "salary")
    private long salary;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = Role.class)
    @JoinTable(name = "permissions",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private Set<Role> roles;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;


//    @OneToMany(fetch = FetchType.EAGER, targetEntity = Shift.class)
//    @JoinTable(name = "permissions_shifts",
//            joinColumns = {@JoinColumn(name = "user_id")},
//            inverseJoinColumns = {@JoinColumn(name = "shift_id")})
//    private Set<Shift> allShifts;

//    @Column(name = "countShift")
//    private int countShift;

    public User() {
    }

//    public int getCountShift() {
//        return countShift;
//    }
//
//    public void setCountShift(int countShift) {
//        this.countShift = countShift;
//    }

    public User(String name) {
        this.name = name;
    }

//    public Set<Shift> getAllShifts() {
//        return allShifts;
//    }
//
//    public void setAllShifts(Set<Shift> allShifts) {
//        this.allShifts = allShifts;
//    }

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return login;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", salary=" + salary +
                ", roles=" + roles +
                ", enabled=" + enabled +
//                ", allShifts=" + allShifts +
                '}';
    }
}
