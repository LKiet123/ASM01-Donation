package org.ltkiet.donation.controller;

import jakarta.validation.Valid;
import org.ltkiet.donation.exception.UserNotFoundException;
import org.ltkiet.donation.model.Role;
import org.ltkiet.donation.model.User;
import org.ltkiet.donation.repository.RoleRepository;
import org.ltkiet.donation.service.UserService;
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

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public String listUsers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "id,asc") String[] sort,
                            @RequestParam(required = false) String keyword) {
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "asc";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<User> userPage = userService.searchUsers(keyword, pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDirection);
        model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);
        return "admin/account";
    }

    @GetMapping("/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleRepository.findAll());
        model.addAttribute("allStatuses", User.UserStatus.values());
        model.addAttribute("isEditMode", false);
        return "admin/account";
    }

    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("user") User user,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            result.rejectValue("username", "error.user", "Ten dang nhap da ton tai.");
        }
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            result.rejectValue("email", "error.user", "Email da ton tai.");
        }
        if (userService.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            result.rejectValue("phoneNumber", "error.user", "So dien thoai da ton tai.");
        }

        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("allStatuses", User.UserStatus.values());
            model.addAttribute("isEditMode", false);
            log.warn("Loi xac thuc khi them nguoi dung: {}", result.getAllErrors());
            return "admin/account";
        }

        try {
            if (user.getRole() == null || user.getRole().getId() == null) {
                throw new IllegalArgumentException("Vai tro la bat buoc.");
            }
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Them nguoi dung thanh cong!");
        } catch (IllegalArgumentException e) {
            log.error("Loi tham so khi them nguoi dung: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Loi khi them nguoi dung: " + e.getMessage());
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("allStatuses", User.UserStatus.values());
            model.addAttribute("isEditMode", false);
            model.addAttribute("user", user);
            return "admin/account";
        } catch (Exception e) {
            log.error("Loi khi them nguoi dung", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Da xay ra loi khong mong muon khi them nguoi dung.");
            return "redirect:/admin";
        }
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new UserNotFoundException("Khong tim thay nguoi dung voi ID: " + id));
            model.addAttribute("user", user);
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("allStatuses", User.UserStatus.values());
            model.addAttribute("isEditMode", true);
            return "admin/account";
        } catch (UserNotFoundException e) {
            log.warn("Khong tim thay nguoi dung de chinh sua: ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("user") User userDetails,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("allStatuses", User.UserStatus.values());
            model.addAttribute("isEditMode", true);
            userDetails.setId(id);
            log.warn("Loi xac thuc khi cap nhat nguoi dung {}: {}", id, result.getAllErrors());
            return "admin/account";
        }

        try {
            if (userDetails.getRole() == null || userDetails.getRole().getId() == null) {
                throw new IllegalArgumentException("Vai tro la bat buoc.");
            }
            userService.updateUser(id, userDetails);
            redirectAttributes.addFlashAttribute("successMessage", "Cap nhat nguoi dung thanh cong!");
        } catch (UserNotFoundException e) {
            log.warn("Khong tim thay nguoi dung de cap nhat: ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Loi tham so khi cap nhat nguoi dung {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Loi khi cap nhat nguoi dung: " + e.getMessage());
            model.addAttribute("allRoles", roleRepository.findAll());
            model.addAttribute("allStatuses", User.UserStatus.values());
            model.addAttribute("isEditMode", true);
            userDetails.setId(id);
            model.addAttribute("user", userDetails);
            return "admin/account";
        } catch (Exception e) {
            log.error("Loi khi cap nhat nguoi dung {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Da xay ra loi khong mong muon khi cap nhat nguoi dung.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xoa nguoi dung thanh cong!");
        } catch (UserNotFoundException e) {
            log.warn("Khong the xoa nguoi dung khong ton tai: ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Loi khi xoa nguoi dung {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Loi khi xoa nguoi dung. Ho co the co cac khoan quyen gop lien quan.");
        }
        return "redirect:/admin/home";
    }

    @PostMapping("/lock/{id}")
    public String lockUser(@PathVariable("id") Long id,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.lockUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Khoa nguoi dung thanh cong!");
        } catch (UserNotFoundException e) {
            log.warn("Khong the khoa nguoi dung khong ton tai: ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Loi khi khoa nguoi dung {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Da xay ra loi khong mong muon khi khoa nguoi dung.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/unlock/{id}")
    public String unlockUser(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.unlockUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Mo khoa nguoi dung thanh cong!");
        } catch (UserNotFoundException e) {
            log.warn("Khong the mo khoa nguoi dung khong ton tai: ID {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Loi khi mo khoa nguoi dung {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Da xay ra loi khong mong muon khi mo khoa nguoi dung.");
        }
        return "redirect:/admin";
    }

    @GetMapping("/detail/{id}")
    public String viewUserDetails(@PathVariable("id") Long id,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new UserNotFoundException("Khong tim thay nguoi dung voi ID: " + id));
            Pageable pageable = PageRequest.of(page, size);
            model.addAttribute("user", user);
            model.addAttribute("userDonationPage", userService.getDonationsByUserId(id, pageable));
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            return "admin/detail";
        } catch (UserNotFoundException e) {
            log.warn("Yeu cau chi tiet nguoi dung cho ID khong ton tai: {}", id);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin";
        }
    }
}