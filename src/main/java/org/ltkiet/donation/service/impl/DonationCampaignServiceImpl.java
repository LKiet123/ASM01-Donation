package org.ltkiet.donation.service.impl;

import org.ltkiet.donation.exception.DonationNotFoundException;
import org.ltkiet.donation.exception.InvalidDonationStatusException;
import org.ltkiet.donation.model.DonationCampaign;
import org.ltkiet.donation.repository.DonationCampaignRepository;
import org.ltkiet.donation.service.DonationCampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class DonationCampaignServiceImpl implements DonationCampaignService {

    private static final Logger log = LoggerFactory.getLogger(DonationCampaignServiceImpl.class);

    @Autowired
    private DonationCampaignRepository campaignRepository;

    @Override
    @Transactional
    public DonationCampaign createCampaign(DonationCampaign campaign) {
        log.info("Dang thuc hien tao moi chien dich quyet tam: {}", campaign.getName());
        validateCampaignDates(campaign);

        campaign.setStatus(DonationCampaign.DonationStatus.NEW);
        campaign.setMoney(BigDecimal.ZERO); // Chac chan so tien bat dau la 0

        if (!StringUtils.hasText(campaign.getCode())) {
            campaign.setCode(generateUniqueCode());
            log.info("Tao ma chien dich duy nhat: {}", campaign.getCode());
        }
        // Chac chan createdAt duoc gan qua @PrePersist trong entity

        DonationCampaign savedCampaign = campaignRepository.save(campaign);
        log.info("Tao moi chien dich thanh cong voi ID: {} va Ma: {}", savedCampaign.getId(), savedCampaign.getCode());
        return savedCampaign;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonationCampaign> getCampaignById(Long id) {
        log.debug("Dang lay chien dich theo ID: {}", id);
        return campaignRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationCampaign> getAllCampaigns(Pageable pageable) {
        log.debug("Dang lay tat ca chien dich voi phan trang: {}", pageable);
        return campaignRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationCampaign> searchCampaigns(String searchTerm, DonationCampaign.DonationStatus status, Pageable pageable) {
        String term = StringUtils.hasText(searchTerm) ? searchTerm.trim() : null;
        log.debug("Dang tim chien dich voi tu khoa: '{}', trang thai: {}, phan trang: {}", term, status, pageable);

        if (status != null && term != null) {
            return campaignRepository.searchCampaigns(term, status, pageable);
        } else if (status != null) {
            return campaignRepository.findByStatus(status, pageable);
        } else if (term != null) {
            return campaignRepository.searchCampaignsWithoutStatus(term, pageable);
        } else {
            return campaignRepository.findAll(pageable);
        }
    }

    @Override
    @Transactional
    public DonationCampaign updateCampaign(Long id, DonationCampaign campaignDetails) {
        log.info("Dang thuc hien cap nhat chien dich voi ID: {}", id);
        DonationCampaign existingCampaign = campaignRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cap nhat that bai: Khong tim thay chien dich voi ID: {}", id);
                    return new DonationNotFoundException("Khong tim thay chien dich quyet tam voi ID: " + id);
                });

        validateCampaignDates(campaignDetails);

        existingCampaign.setName(campaignDetails.getName());
        existingCampaign.setStartDate(campaignDetails.getStartDate());
        existingCampaign.setEndDate(campaignDetails.getEndDate());
        existingCampaign.setOrganizationName(campaignDetails.getOrganizationName());
        existingCampaign.setPhoneNumber(campaignDetails.getPhoneNumber());
        existingCampaign.setDescription(campaignDetails.getDescription());

        DonationCampaign updatedCampaign = campaignRepository.save(existingCampaign);
        log.info("Cap nhat chien dich thanh cong voi ID: {}", updatedCampaign.getId());
        return updatedCampaign;
    }

    @Override
    @Transactional
    public void deleteCampaignIfNew(Long id) {
        log.info("Dang thuc hien xoa chien dich voi ID: {}", id);
        DonationCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Xoa that bai: Khong tim thay chien dich voi ID: {}", id);
                    return new DonationNotFoundException("Khong tim thay chien dich quyet tam voi ID: " + id);
                });

        if (campaign.getStatus() != DonationCampaign.DonationStatus.NEW) {
            log.warn("Xoa that bai: Chi chien dich moi moi co the bi xoa. Trang thai hien tai: {}", campaign.getStatus());
            throw new InvalidDonationStatusException("Khong the xoa chien dich: Trang thai chien dich khong phai moi.");
        }

        campaignRepository.delete(campaign);
        log.info("Xoa chien dich thanh cong voi ID: {}", id);
    }

    @Override
    @Transactional
    public DonationCampaign updateCampaignStatus(Long id, DonationCampaign.DonationStatus newStatus) {
        log.info("Dang thuc hien cap nhat trang thai cho chien dich {} sang trang thai {}", id, newStatus);
        DonationCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cap nhat trang thai that bai: Khong tim thay chien dich voi ID: {}", id);
                    return new DonationNotFoundException("Khong tim thay chien dich quyet tam voi ID: " + id);
                });

        if (campaign.getStatus() == DonationCampaign.DonationStatus.CLOSED) {
            log.warn("Cap nhat trang thai that bai: Chien dich {} da bi dong.", id);
            throw new InvalidDonationStatusException("Khong the thay doi trang thai: Chien dich da bi dong.");
        }
        if (campaign.getStatus() == newStatus) {
            log.warn("Cap nhat trang thai bi bo qua: Chien dich {} da co trang thai {}", id, newStatus);
            return campaign;
        }

        campaign.setStatus(newStatus);
        DonationCampaign updatedCampaign = campaignRepository.save(campaign);
        log.info("Cap nhat trang thai cho chien dich {} sang {}", id, newStatus);
        return updatedCampaign;
    }

    @Override
    @Transactional
    public void addDonationMoney(Long campaignId, BigDecimal money) {
        log.debug("Dang them so tien ({}) cho chien dich ID: {}", money, campaignId);
        if (money == null || money.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("So tien quyet tam phai lon hon 0.");
        }

        DonationCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("Them so tien that bai: Khong tim thay chien dich voi ID: {}", campaignId);
                    return new DonationNotFoundException("Khong tim thay chien dich quyet tam voi ID: " + campaignId);
                });

        campaign.setMoney(campaign.getMoney().add(money));
        campaignRepository.save(campaign);
        log.info("Them {} vao chien dich {} thanh cong. Tong so tien moi: {}", money, campaignId, campaign.getMoney());
    }

    private void validateCampaignDates(DonationCampaign campaign) {
        if (campaign.getStartDate() != null && campaign.getEndDate() != null &&
                campaign.getStartDate().isAfter(campaign.getEndDate())) {
            log.warn("Kiem tra that bai: Ngay bat dau ({}) sau ngay ket thuc ({}) cho chien dich {}",
                    campaign.getStartDate(), campaign.getEndDate(), campaign.getName());
            throw new IllegalArgumentException("Ngay bat dau phai truoc hoac bang ngay ket thuc.");
        }
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = "CAMP-" + UUID.randomUUID().toString().toUpperCase().substring(0, 8);
            attempts++;
            if (attempts > 10) {
                log.error("Tao ma chien dich duy nhat that bai sau {} lan thu.", attempts);
                throw new RuntimeException("Khong the tao ma chien dich duy nhat.");
            }
        } while (campaignRepository.findByCode(code, Pageable.unpaged()).hasContent());
        return code;
    }
}
