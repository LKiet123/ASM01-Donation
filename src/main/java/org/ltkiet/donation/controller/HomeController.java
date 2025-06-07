package org.ltkiet.donation.controller;

import jakarta.validation.Valid;
import org.ltkiet.donation.model.DonationCampaign;
import org.ltkiet.donation.model.User;
import org.ltkiet.donation.model.UserDonation;
import org.ltkiet.donation.service.DonationCampaignService;
import org.ltkiet.donation.service.UserDonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/public")
public class HomeController {

    @Autowired
    private DonationCampaignService donationService;

    @Autowired
    private UserDonationService userDonationService;

    @GetMapping
    public String listCampaigns(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DonationCampaign> campaignPage = donationService.searchCampaigns(keyword, null, pageable);
        model.addAttribute("campaignPage", campaignPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("userDonation", new UserDonation());
        return "public/home";
    }

    @PostMapping("/donation/{id}")
    public String addDonation(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("userDonation") UserDonation userDonation,
                              BindingResult bindingResult,
                              @SessionAttribute(value = "user", required = false) User sessionUser,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        DonationCampaign campaign = donationService.getCampaignById(id).orElse(null);
        String redirectPath = campaign != null ? "redirect:/donation/" + id : "redirect:/public";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid donation information. Please check the details.");
            return redirectPath;
        }

        try {
            if (sessionUser != null) {
                userDonation.setUser(sessionUser);
                if (userDonation.getName() == null || userDonation.getName().trim().isEmpty()) {
                    userDonation.setName(sessionUser.getFullName());
                }
            } else {
                userDonation.setUser(null);
                if (userDonation.getName() == null || userDonation.getName().trim().isEmpty()) {
                    userDonation.setName("Anonymous");
                }
            }

            userDonation.setStatus(UserDonation.UserDonationStatus.CONFIRM);
            userDonationService.createDonation(userDonation);
            redirectAttributes.addFlashAttribute("successMessage", "Donation submitted successfully! Thank you.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error submitting donation: " + e.getMessage());
        }

        return redirectPath;
    }
}