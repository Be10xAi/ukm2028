package com.tech10x.ukm.serviceimpl;

import com.tech10x.ukm.dto.request.SupplierRegisterRequest;
import com.tech10x.ukm.dto.response.SupplierResponse;
import com.tech10x.ukm.entity.Role;
import com.tech10x.ukm.entity.Supplier;
import com.tech10x.ukm.entity.Users;
import com.tech10x.ukm.entity.VerificationStatus;
import com.tech10x.ukm.exception.ApiException;
import com.tech10x.ukm.repository.RoleRepository;
import com.tech10x.ukm.repositoryproxy.SupplierProxyRepository;
import com.tech10x.ukm.repositoryproxy.UserRepository;
import com.tech10x.ukm.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private static final String SUPPLIER_ROLE = "ROLE_SUPPLIER";

    private final UserRepository usersRepository;
    private final SupplierProxyRepository supplierProxyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SupplierResponse registerSupplier(SupplierRegisterRequest request) {
        if (usersRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        Role role = roleRepository.findByName(SUPPLIER_ROLE)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(SUPPLIER_ROLE).description("Hotel/dharamshala/camp owner").build()));

        Users user = usersRepository.save(Users.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .email(request.getUserId())
                .mobileNo(request.getMobileNo())
                .password(passwordEncoder.encode(request.getPassword()))
                .dob(request.getDob())
                .gender(request.getGender())
                .roles(Set.of(role))
                .build());

        // Supplier.user is a real @OneToOne foreign key to the user just saved.
        Supplier supplier = supplierProxyRepository.save(Supplier.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .contactPerson(request.getContactPerson())
                .gstNumber(request.getGstNumber())
                .panNumber(request.getPanNumber())
                .bankAccountNo(request.getBankAccountNo())
                .bankIfsc(request.getBankIfsc())
                .verificationStatus(VerificationStatus.PENDING)
                .build());

        return toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getMyProfile(String userId) {
        return toResponse(supplierProxyRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No supplier profile for this account")));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getBySupplierId(String supplierId) {
        return toResponse(findOrThrow(supplierId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> list(VerificationStatus status) {
        List<Supplier> suppliers = status == null
                ? supplierProxyRepository.findAll()
                : supplierProxyRepository.findByVerificationStatus(status);
        return suppliers.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public SupplierResponse updateVerificationStatus(String supplierId, VerificationStatus status) {
        Supplier supplier = findOrThrow(supplierId);
        supplier.setVerificationStatus(status);
        return toResponse(supplierProxyRepository.save(supplier));
    }

    private Supplier findOrThrow(String supplierId) {
        return supplierProxyRepository.findBySupplierId(supplierId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Supplier not found: " + supplierId));
    }

    /** bankAccountNo is intentionally never included in responses. */
    private SupplierResponse toResponse(Supplier supplier) {
        Users user = supplier.getUser();
        return SupplierResponse.builder()
                .supplierId(supplier.getSupplierId())
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .mobileNo(user.getMobileNo())
                .businessName(supplier.getBusinessName())
                .contactPerson(supplier.getContactPerson())
                .gstNumber(supplier.getGstNumber())
                .panNumber(supplier.getPanNumber())
                .bankIfsc(supplier.getBankIfsc())
                .verificationStatus(supplier.getVerificationStatus())
                .createdAt(supplier.getCreatedAt())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
