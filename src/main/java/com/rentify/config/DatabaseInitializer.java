package com.rentify.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rentify.modules.user.entity.Permission;
import com.rentify.modules.user.entity.Role;
import com.rentify.modules.user.entity.User;
import com.rentify.modules.user.repository.PermissionRepository;
import com.rentify.modules.user.repository.RoleRepository;
import com.rentify.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info(">>> KIỂM TRA VÀ KHỞI TẠO DỮ LIỆU BAN ĐẦU (SEEDING)...");

        // 1. Khởi tạo Permissions nếu chưa có
        if (this.permissionRepository.count() == 0) {
            List<Permission> permissions = new ArrayList<>();

            // Module USERS
            permissions.add(createPermission("Xem danh sách người dùng", "/api/v1/users", "GET", "USERS"));
            permissions.add(createPermission("Xem chi tiết người dùng", "/api/v1/users/{id}", "GET", "USERS"));
            permissions.add(createPermission("Cập nhật hồ sơ cá nhân", "/api/v1/users/profile", "PUT", "USERS"));
            permissions.add(createPermission("Khóa/Mở khóa người dùng", "/api/v1/users/{id}/status", "PATCH", "USERS"));

            // Module ROOMS (Quản lý Bất động sản)
            permissions.add(createPermission("Tạo tin đăng bất động sản mới", "/api/v1/rooms", "POST", "ROOMS"));
            permissions.add(createPermission("Cập nhật thông tin bất động sản", "/api/v1/rooms/{id}", "PUT", "ROOMS"));
            permissions.add(createPermission("Xóa/Ẩn tin đăng bất động sản", "/api/v1/rooms/{id}", "DELETE", "ROOMS"));
            permissions.add(createPermission("Xem chi tiết bất động sản", "/api/v1/rooms/{id}", "GET", "ROOMS"));
            permissions.add(createPermission("Kiểm duyệt tin đăng bất động sản", "/api/v1/rooms/{id}/approve", "PATCH", "ROOMS"));

            // Module ESCROW (Ký quỹ cọc 48h)
            permissions.add(createPermission("Đặt cọc ký quỹ giữ chỗ 48h", "/api/v1/escrow/deposit", "POST", "ESCROW"));
            permissions.add(createPermission("Xác nhận bàn giao bất động sản (Giải ngân cọc)", "/api/v1/escrow/{id}/release", "PATCH", "ESCROW"));
            permissions.add(createPermission("Khiếu nại bàn giao / Yêu cầu hoàn cọc", "/api/v1/escrow/{id}/dispute", "PATCH", "ESCROW"));
            permissions.add(createPermission("Xem chi tiết hợp đồng ký quỹ cọc", "/api/v1/escrow/{id}", "GET", "ESCROW"));

            // Module CHAT
            permissions.add(createPermission("Gửi tin nhắn", "/api/v1/chat/messages", "POST", "CHAT"));
            permissions.add(createPermission("Xem danh sách trò chuyện", "/api/v1/chat/conversations", "GET", "CHAT"));

            // Module FORUM (Diễn đàn cư dân)
            permissions.add(createPermission("Đăng bài viết trên diễn đàn cư dân", "/api/v1/forum/topics", "POST", "FORUM"));
            permissions.add(createPermission("Thả tim bài viết diễn đàn", "/api/v1/forum/topics/{id}/like", "POST", "FORUM"));
            permissions.add(createPermission("Gửi bình luận diễn đàn cư dân", "/api/v1/forum/topics/{id}/comments", "POST", "FORUM"));

            // Module WALLET
            permissions.add(createPermission("Nạp tiền VNPAY", "/api/v1/wallet/topup", "POST", "WALLET"));
            permissions.add(createPermission("Xem lịch sử ví", "/api/v1/wallet/transactions", "GET", "WALLET"));

            this.permissionRepository.saveAll(permissions);
            log.info(">>> Đã nạp thành công {} Permissions vào Database!", permissions.size());
        }

        // 2. Khởi tạo Roles nếu chưa có
        if (this.roleRepository.count() == 0) {
            List<Permission> allPermissions = this.permissionRepository.findAll();

            // ROLE_ADMIN: Sở hữu toàn bộ quyền hạn
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Quản trị viên toàn quyền hệ sinh thái PropTech Rentify");
            adminRole.setPermissions(new HashSet<>(allPermissions));
            this.roleRepository.save(adminRole);

            // ROLE_LANDLORD: Quyền chủ nhà (đăng phòng, quản lý cọc, chat, ví...)
            Set<Permission> landlordPermissions = new HashSet<>();
            for (Permission p : allPermissions) {
                if (!p.getName().contains("Duyệt tin") && !p.getName().contains("Khóa/Mở khóa")) {
                    landlordPermissions.add(p);
                }
            }
            Role landlordRole = new Role();
            landlordRole.setName("ROLE_LANDLORD");
            landlordRole.setDescription("Chủ nhà / Đối tác sở hữu bất động sản cho thuê (Host)");
            landlordRole.setPermissions(landlordPermissions);
            this.roleRepository.save(landlordRole);

            // ROLE_USER: Quyền người thuê bình thường
            Set<Permission> userPermissions = new HashSet<>();
            for (Permission p : allPermissions) {
                if (p.getModule().equals("CHAT") || p.getModule().equals("FORUM") || p.getModule().equals("WALLET")
                        || p.getName().equals("Xem chi tiết phòng trọ") || p.getName().equals("Đặt cọc giữ phòng 48h")
                        || p.getName().equals("Khiếu nại / Hủy cọc") || p.getName().equals("Xem thông tin đơn cọc")
                        || p.getName().equals("Cập nhật hồ sơ cá nhân")) {
                    userPermissions.add(p);
                }
            }
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Khách thuê / Cư dân tìm kiếm bất động sản (Tenant)");
            userRole.setPermissions(userPermissions);
            this.roleRepository.save(userRole);

            log.info(">>> Đã nạp thành công 3 Roles (ADMIN, LANDLORD, USER) vào Database!");
        }

        // 3. Khởi tạo 1 tài khoản Admin mẫu nếu chưa có User nào
        if (this.userRepository.count() == 0) {
            Role adminRole = this.roleRepository.findByName("ROLE_ADMIN").orElse(null);

            User admin = new User();
            admin.setPhoneNumber("0999999999");
            admin.setEmail("admin@rentify.com");
            admin.setUsername("admin");
            // Chuỗi mã hóa BCrypt của mật khẩu "123456"
            admin.setPasswordHash("$2a$10$g0f0mJ337oH1x6H4Y70mke8rX4uN9b4OsqoG.rN8y0n0Nsm8E9eCW");
            admin.setFullName("Quản Trị Viên Hệ Thống");
            admin.setRole(adminRole);
            admin.setIsActive(true);
            admin.setIsPhoneVerified(true);
            admin.setWalletBalance(BigDecimal.ZERO);

            this.userRepository.save(admin);
            log.info(">>> Đã tạo tài khoản Admin mặc định: Phone: 0999999999 / Pass: 123456");
        }

        log.info(">>> KHỞI TẠO DỮ LIỆU BAN ĐẦU HOÀN TẤT!");
    }

    private Permission createPermission(String name, String apiPath, String method, String module) {
        Permission p = new Permission();
        p.setName(name);
        p.setApiPath(apiPath);
        p.setMethod(method);
        p.setModule(module);
        return p;
    }
}