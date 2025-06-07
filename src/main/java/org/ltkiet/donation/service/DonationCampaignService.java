package org.ltkiet.donation.service;

import jakarta.validation.Valid;
import org.ltkiet.donation.model.DonationCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface DonationCampaignService {

    DonationCampaign createCampaign(@Valid DonationCampaign campaign);

    Optional<DonationCampaign> getCampaignById(Long id);

    Page<DonationCampaign> getAllCampaigns(Pageable pageable);

    Page<DonationCampaign> searchCampaigns(String searchTerm, DonationCampaign.DonationStatus status, Pageable pageable);

    DonationCampaign updateCampaign(Long id, @Valid DonationCampaign campaignDetails);

    void deleteCampaignIfNew(Long id);

    DonationCampaign updateCampaignStatus(Long id, DonationCampaign.DonationStatus newStatus);

    void addDonationMoney(Long campaignId, BigDecimal money);
}

