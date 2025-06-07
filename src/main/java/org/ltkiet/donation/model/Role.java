package org.ltkiet.donation.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

//dinh nghia lop role, anh xa toi lop roles
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

//    khoa chinh tu dong tang
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    ten vai tro, bat buoc
    @Column(nullable = false, length = 50, unique = true)
    private String name;

    //quan he 1-n voi bang User
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> users;

}
