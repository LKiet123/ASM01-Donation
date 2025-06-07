package org.ltkiet.donation.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

//dinh nghia lop donation campaign
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Entity
@Table(name = "donations")
public class DonationCampaign {

//  khoa chinh tu dong tang
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//ma chien dich va bat buoc
    @Column(nullable = false, unique = true)
    private String code;

//    ten chien dich, khong duoc de trong
    @Column(nullable = false)
    private String name;

//  ngay bat dau va ket thuc
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

//    Ten to chuc thuc hien chien dich
    @Column(name = "organization_name", nullable = false)
    private String organizationName;

//    soDT lien he, toi da 15 ky tu
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

//    mo ta chi tiet ve chien dich
    @Lob //dung de luu du lieu lon
    @Column(columnDefinition = "TEXT")
    private String description;

//    so tien hien tai quyen gop duoc
    @Column(name = "money", precision = 15, scale = 2)
    private BigDecimal money = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('NEW', 'IN_PROGRESS', 'ENDED', 'CLOSED') DEFAULT 'NEW'")
    private DonationStatus status = DonationStatus.NEW;

//    thoi gian tao
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "donationCampaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserDonation> userDonations;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        if(money == null)
            money = BigDecimal.ZERO;
    }

    public enum DonationStatus {
      NEW, IN_PROGRESS, ENDED, CLOSED
    }

}
