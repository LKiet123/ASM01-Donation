package org.ltkiet.donation.repository;

import org.ltkiet.donation.model.DonationCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationCampaignRepository extends JpaRepository<DonationCampaign, Long> {

    Page<DonationCampaign> findAll(Pageable pageable);

    Page<DonationCampaign> findByStatus(DonationCampaign.DonationStatus status, Pageable pageable);

    Page<DonationCampaign> findByCode(String code, Pageable pageable);

    Page<DonationCampaign> findByOrganizationNameContainingIgnoreCase(String organizationName, Pageable pageable);
    Page<DonationCampaign> findByPhoneNumberContaining(String phoneNumber, Pageable pageable);

    @Query("SELECT dc FROM DonationCampaign dc WHERE " +
            "(:status IS NULL OR dc.status = :status) AND " +
            "(LOWER(dc.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(dc.organizationName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "dc.phoneNumber LIKE CONCAT('%', :searchTerm, '%'))")
    Page<DonationCampaign> searchCampaigns(
            @Param("searchTerm") String searchTerm,
            @Param("status") DonationCampaign.DonationStatus status,
            Pageable pageable
    );

    @Query("SELECT dc FROM DonationCampaign dc WHERE " +
            "LOWER(dc.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(dc.organizationName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "dc.phoneNumber LIKE CONCAT('%', :searchTerm, '%')")
    Page<DonationCampaign> searchCampaignsWithoutStatus(
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}