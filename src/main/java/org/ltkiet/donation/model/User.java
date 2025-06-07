package org.ltkiet.donation.model;


import jakarta.persistence.*;
 import jakarta.validation.constraints.*;
import lombok.*;
 import java.time.LocalDateTime;
import java.util.Set;

//Định nghĩa lớp user, ánh xạ tới bảng 'users' trong csdl
@Entity
@Table(name = "users")
@Getter
@Setter
 @NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

//    primary key tu dong tang
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    luu ten nguoi dung, khong duoc de trong
    @NonNull
    @Size(min = 2, max = 100)
    @Column(name = "full_name", nullable = false)
    private String fullName;

//    email la duy nhat va bat buoc
    @NonNull
    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

//    soDT la duy nhat toi da 15 ky tu
    @NonNull
    @Size(min = 10, max = 15)
    @Column(name = "phone_number", nullable = false, unique = true, length = 15)
    private String phoneNumber;

//    note
    @Column(name = "note")
    private String note;

//    dia chi nguoi dung
    @Column(name = "address")
    private String address;

//    username la duy nhat toi da 100 ky tu
    @NonNull
    @Size(min = 6, max = 100)
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

//  password
    @NonNull
    @Size(min = 6)
    @Column(nullable = false)
    private String password;

//    quan he mot nhieu voi bang 'roles'
    @ManyToOne
    @JoinColumn(name="role_id", nullable= false)
    private Role role;

//trang thai cua user
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ACTIVE', 'LOCKED') DEFAULT 'ACTIVE'")
    private UserStatus status = UserStatus.ACTIVE;

//thoi gian tao va cap nhat
    @Column(name="created_at", nullable = false)
    private LocalDateTime createAt;

//    quna he 1-n voi bang UserDonation
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserDonation> userDonations;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
     }


    //dinh nghia enum cho trang thai cua user
    public enum UserStatus {
        ACTIVE, LOCKED
    }


}
