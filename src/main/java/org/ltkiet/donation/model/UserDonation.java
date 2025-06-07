package org.ltkiet.donation.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_donations")
public class UserDonation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id" )
    private DonationCampaign donationCampaign;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal money;

    @Column(name = "created_at")
     private LocalDateTime createAt;

    @Lob
    @Column(name = "text", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserDonationStatus status = UserDonationStatus.CONFIRMED;

    // Set ngày mặc định khi insert
    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
    }
//*
    public String getDonationId() {
        if(donationCampaign == null)
            return null;
        return donationCampaign.getCode();
    }

    public Object getUserId() {
        if(user == null)
            return null;
        return user.getId();
    }
//*
    public enum UserDonationStatus {
        CONFIRM , CONFIRMING, CONFIRMED
    }


}

