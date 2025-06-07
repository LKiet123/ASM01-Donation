package org.ltkiet.donation.service.impl;

import org.ltkiet.donation.exception.DonationNotFoundException;
import org.ltkiet.donation.exception.UserNotFoundException;
import org.ltkiet.donation.model.DonationCampaign;
import org.ltkiet.donation.model.User;
import org.ltkiet.donation.model.UserDonation;
import org.ltkiet.donation.repository.DonationCampaignRepository;
import org.ltkiet.donation.repository.UserDonationRepository;
import org.ltkiet.donation.repository.UserRepository;
import org.ltkiet.donation.service.DonationCampaignService;
import org.ltkiet.donation.service.UserDonationService;
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

@Service
public class UserDonationServiceImpl implements UserDonationService {

    private static final Logger log = LoggerFactory.getLogger(UserDonationServiceImpl.class);

    @Autowired
    private UserDonationRepository userDonationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationCampaignRepository donationCampaignRepository;

    @Autowired
    private DonationCampaignService donationCampaignService;

    @Override
    @Transactional
    public UserDonation createDonation(UserDonation userDonation) {
        log.info("Dang ghi nhan don gop cua nguoi dung.");

        if (userDonation.getMoney() == null || userDonation.getMoney().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("So tien don gop phai lon hon 0.");
        }

        if (userDonation.getDonationCampaign() == null || userDonation.getDonationCampaign().getId() == null) {
            throw new IllegalArgumentException("ID chien dich don gop la bat buoc.");
        }
        DonationCampaign campaign = donationCampaignRepository.findById(userDonation.getDonationCampaign().getId())
                .orElseThrow(() -> {
                    log.warn("Tao don gop that bai: Khong tim thay chien dich voi ID: {}", userDonation.getDonationCampaign().getId());
                    return new DonationNotFoundException("Khong tim thay chien dich don gop voi ID: " + userDonation.getDonationCampaign().getId());
                });

        userDonation.setDonationCampaign(campaign);

        if (userDonation.getUser() != null && userDonation.getUser().getId() != null) {
            User user = userRepository.findById(userDonation.getUser().getId())
                    .orElseThrow(() -> {
                        log.warn("Tao don gop that bai: Khong tim thay nguoi dung voi ID: {}", userDonation.getUser().getId());
                        return new UserNotFoundException("Khong tim thay nguoi dung voi ID: " + userDonation.getUser().getId());
                    });
            userDonation.setUser(user);

            if (!StringUtils.hasText(userDonation.getName())) {
                userDonation.setName(user.getFullName());
            }
        } else {
            userDonation.setUser(null);
            if (!StringUtils.hasText(userDonation.getName())) {
                userDonation.setName("Anonynous");
            }
        }

        if (userDonation.getStatus() == null) {
            userDonation.setStatus(UserDonation.UserDonationStatus.CONFIRMED);
        }

        UserDonation savedDonation = userDonationRepository.save(userDonation);
        log.info("Da ghi nhan don gop cua nguoi dung thanh cong voi ID: {} cho chien dich ID: {}", savedDonation.getId(), campaign.getId());

        try {
            donationCampaignService.addDonationMoney(campaign.getId(), savedDonation.getMoney());
        } catch (Exception e) {
            log.error("Cap nhat so tien chien dich khong thanh cong cho chien dich ID {} sau khi luu don gop ID {}: {}",
                    campaign.getId(), savedDonation.getId(), e.getMessage(), e);
            throw e;
        }

        return savedDonation;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDonation> getDonationById(Long id) {
        log.debug("Lay don gop cua nguoi dung theo ID: {}", id);
        return userDonationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDonation> getDonationsByCampaignId(Long campaignId, Pageable pageable) {
        log.debug("Lay danh sach don gop cho chien dich ID: {} voi phan trang: {}", campaignId, pageable);
        if (!donationCampaignRepository.existsById(campaignId)) {
            log.warn("Thuc hien lay don gop cho chien dich khong ton tai voi ID: {}", campaignId);
            throw new DonationNotFoundException("Khong tim thay chien dich don gop voi ID: " + campaignId);
        }
        return userDonationRepository.findByDonationCampaignId(campaignId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDonation> getDonationsByUserId(Long userId, Pageable pageable) {
        log.warn("getDonationsByUserId duoc goi trong UserDonationService - can xem xet su dung UserService thay the.");
        if (!userRepository.existsById(userId)) {
            log.warn("Thuc hien lay don gop cho nguoi dung khong ton tai voi ID: {}", userId);
            throw new UserNotFoundException("Khong tim thay nguoi dung voi ID: " + userId);
        }
        return userDonationRepository.findByUserId(userId, pageable);
    }
}
