package org.ltkiet.donation.controller;

import jakarta.validation.Valid;
import org.ltkiet.donation.model.DonationCampaign;
import org.ltkiet.donation.model.User;
import org.ltkiet.donation.model.UserDonation;
import org.ltkiet.donation.service.DonationCampaignService;
import org.ltkiet.donation.service.UserDonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/donation")
public class DonationController {

    @Autowired
    private DonationCampaignService donationService;

    @Autowired
    private UserDonationService userDonationService;

    @GetMapping("/{id}")
    public String viewDonationDetails(@PathVariable("id") Long id,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "5") int size,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Optional<DonationCampaign> campaign = donationService.getCampaignById(id);
        if (campaign.isPresent()) {
            Pageable pageable = PageRequest.of(page, size);
            model.addAttribute("donation", campaign.get());
            model.addAttribute("userDonationPage", userDonationService.getDonationsByCampaignId(id, pageable));
            model.addAttribute("pageSize", size);
            model.addAttribute("currentPage", page);
            model.addAttribute("userDonation", new UserDonation());
            return "public/detail";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Donation campaign not found.");
            return "redirect:/public";
        }
    }
}

