package org.ltkiet.donation.service;

import jakarta.validation.Valid;
import org.ltkiet.donation.model.UserDonation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserDonationService {

    UserDonation createDonation(@Valid UserDonation userDonation);

    Optional<UserDonation> getDonationById(Long id);

    Page<UserDonation> getDonationsByCampaignId(Long campaignId, Pageable pageable);

    Page<UserDonation> getDonationsByUserId(Long userId, Pageable pageable);


}

