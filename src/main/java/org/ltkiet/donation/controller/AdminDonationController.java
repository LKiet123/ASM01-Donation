package org.ltkiet.donation.controller;

import jakarta.validation.Valid;
import org.ltkiet.donation.exception.DonationNotFoundException;
import org.ltkiet.donation.exception.InvalidDonationStatusException;
import org.ltkiet.donation.model.DonationCampaign;
import org.ltkiet.donation.model.UserDonation;
import org.ltkiet.donation.service.DonationCampaignService;
import org.ltkiet.donation.service.UserDonationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/donation")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDonationController {

    private static final Logger log = LoggerFactory.getLogger(AdminDonationController.class);

    @Autowired
    private DonationCampaignService donationCampaignService;

    @Autowired
    private UserDonationService userDonationService;

    @GetMapping
    public String listCampaigns(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "id,asc") String[] sort,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) DonationCampaign.DonationStatus status) {
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "asc";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<DonationCampaign> campaignPage = donationCampaignService.searchCampaigns(keyword, status, pageable);

        model.addAttribute("campaignPage", campaignPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDirection);
        model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("campaign", new DonationCampaign());
        model.addAttribute("allStatuses", DonationCampaign.DonationStatus.values());
        return "admin/donation";
    }

    @PostMapping("/add")
    public String addCampaign(@Valid @ModelAttribute("campaign") DonationCampaign campaign,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allStatuses", DonationCampaign.DonationStatus.values());
            log.warn("Validation errors adding campaign: {}", result.getAllErrors());
            return "admin/donation";
        }
        try {
            donationCampaignService.createCampaign(campaign);
            redirectAttributes.addFlashAttribute("successMessage", "Campaign added successfully!");
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument adding campaign: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding campaign: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error adding campaign", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while adding the campaign.");
        }
        return "redirect:/admin/donation";
    }

    @PostMapping("/edit/{id}")
    public String updateCampaign(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("campaign") DonationCampaign campaign,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allStatuses", DonationCampaign.DonationStatus.values());
            campaign.setId(id);
            log.warn("Validation errors updating campaign {}: {}", id, result.getAllErrors());
            return "admin/donation";
        }
        try {
            donationCampaignService.updateCampaign(id, campaign);
            redirectAttributes.addFlashAttribute("successMessage", "Campaign updated successfully!");
        } catch (DonationNotFoundException e) {
            log.warn("Campaign not found for update: ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument updating campaign {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating campaign: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating campaign {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while updating the campaign.");
        }
        return "redirect:/admin/donation";
    }

    @PostMapping("/delete/{id}")
    public String deleteCampaign(@PathVariable("id") Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            donationCampaignService.deleteCampaignIfNew(id);
            redirectAttributes.addFlashAttribute("successMessage", "Campaign deleted successfully!");
        } catch (DonationNotFoundException | InvalidDonationStatusException e) {
            log.warn("Cannot delete campaign {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting campaign {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while deleting the campaign.");
        }
        return "redirect:/admin/donation";
    }

    @PostMapping("/status/{id}")
    public String updateCampaignStatus(@PathVariable("id") Long id,
                                       @RequestParam("newStatus") DonationCampaign.DonationStatus newStatus,
                                       RedirectAttributes redirectAttributes) {
        try {
            donationCampaignService.updateCampaignStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Campaign status updated successfully!");
        } catch (DonationNotFoundException | InvalidDonationStatusException e) {
            log.warn("Cannot update status for campaign {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error updating status for campaign {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while updating campaign status.");
        }
        return "redirect:/admin/donation";
    }

    @GetMapping("/details/{id}")
    public String viewDonationDetails(@PathVariable("id") Long id,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "id,asc") String[] sort,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Optional<DonationCampaign> campaignOpt = donationCampaignService.getCampaignById(id);
        if (campaignOpt.isPresent()) {
            String sortField = sort[0];
            String sortDirection = sort.length > 1 ? sort[1] : "asc";
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            Page<UserDonation> userDonationPage = userDonationService.getDonationsByCampaignId(id, pageable);

            model.addAttribute("campaign", campaignOpt.get());
            model.addAttribute("userDonationPage", userDonationPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDirection);
            model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
            return "admin/detail";
        } else {
            log.warn("Donation campaign details requested for non-existent ID: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", "Donation campaign not found.");
            return "redirect:/admin/donation";
        }
    }
}