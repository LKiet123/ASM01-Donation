package org.ltkiet.donation.repository;

import org.ltkiet.donation.model.UserDonation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDonationRepository extends JpaRepository<UserDonation, Long> {

    Page<UserDonation> findByDonationCampaignId(Long campaignId, Pageable pageable);

    Page<UserDonation> findByUserId(Long userId, Pageable pageable);

    List<UserDonation> findByDonationCampaignId(Long campaignId);
}