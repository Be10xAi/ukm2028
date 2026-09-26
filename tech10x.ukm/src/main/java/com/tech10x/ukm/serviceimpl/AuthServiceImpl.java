package com.tech10x.ukm.serviceimpl;

import com.tech10x.ukm.dto.request.LoginRequest;
import com.tech10x.ukm.dto.request.RegisterRequest;
import com.tech10x.ukm.dto.response.AuthResponse;
import com.tech10x.ukm.dto.response.UserResponse;
import com.tech10x.ukm.entity.OtpToken;
import com.tech10x.ukm.entity.Role;
import com.tech10x.ukm.entity.Users;
import com.tech10x.ukm.exception.ApiException;
import com.tech10x.ukm.repository.RoleRepository;
import com.tech10x.ukm.repositoryproxy.OtpRepository;
import com.tech10x.ukm.repositoryproxy.UserRepository;
import com.tech10x.ukm.security.CustomUserDetailsService;
import com.tech10x.ukm.security.JwtService;
import com.tech10x.ukm.service.AuthService;
import com.tech10x.ukm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_EMPLOYEE";

    private final UserRepository usersRepository;
    private final OtpRepository otpRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final NotificationService notificationService;
    private final JwtService jwtService;
    private final OtpServiceImpl otpServiceImpl;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.otp.max.attempt.allow}")
    private long maxAttemptAllow;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (usersRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }

        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(DEFAULT_ROLE).description("Default role").build()));

        Users user = Users.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .email(request.getUserId())
                .mobileNo(request.getMobileNo())
                .password(passwordEncoder.encode(request.getPassword()))
                .dob(request.getDob())
                .gender(request.getGender())
                .roles(Set.of(role))
                .build();

        Users saved = usersRepository.save(user);
        return toUserResponse(saved);
    }

    @Override
    public void requestOtp(String email) {
        Users user = findByEmailOrThrow(email);
        Optional<OtpToken> otpTokenOptional= otpRepository.findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getUserId());
        if(otpTokenOptional.isPresent())
        {
            OtpToken otpToken=otpTokenOptional.get();
            if (!otpToken.isUsed()&& otpToken.getNoOfAttempt()<maxAttemptAllow)
            {
                notificationService.dispatch(otpToken.getUserId(), otpToken.getOtp());
                otpToken.setNoOfAttempt(otpToken.getNoOfAttempt()+1);
                otpRepository.save(otpToken);
                return;
            }
            else {
                if(Duration.between(otpToken.getCreatedAt(), LocalDateTime.now()).toSeconds()>=30)
                {
                    String otp= otpServiceImpl.generateOtp(user.getUserId(), email);
                    notificationService.dispatch(email,otp);
                    return;
                }
                otpToken.setUsed(true);
                otpRepository.save(otpToken);
                throw  new ApiException(HttpStatus.NOT_ACCEPTABLE, "You reached Max attempt");
            }
        }
        String otp= otpServiceImpl.generateOtp(user.getUserId(), email);
        notificationService.dispatch(email,otp);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        boolean hasPassword = StringUtils.hasText(request.getPassword());
        boolean hasOtp = StringUtils.hasText(request.getOtp());

        if (hasPassword == hasOtp) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Provide either password or otp, not both");
        }

        Users user = findByEmailOrThrow(request.getEmail());

        if (hasPassword) {
            loginWithPassword(user, request.getPassword());
        } else {
            otpServiceImpl.verifyOtp(user.getUserId(), request.getOtp());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserId());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .expiresInMs(jwtExpirationMs)
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public void loginWithPassword(Users user, String rawPassword) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserId(), rawPassword));
        } catch (BadCredentialsException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    public Users findByEmailOrThrow(String email) {
        return usersRepository.findByUserId(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "No account found for this email"));
    }

    private String generateUserId() {
        return "U" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public UserResponse toUserResponse(Users user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .mobileNo(user.getMobileNo())
                .dob(user.getDob())
                .gender(user.getGender())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
