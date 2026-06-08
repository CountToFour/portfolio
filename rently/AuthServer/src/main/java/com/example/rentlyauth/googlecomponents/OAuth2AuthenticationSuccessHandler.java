package com.example.rentlyauth.googlecomponents;



import com.example.rentlyauth.model.RolesEntity;
import com.example.rentlyauth.model.UsersEntity;
import com.example.rentlyauth.repository.RoleRepository;
import com.example.rentlyauth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
private final UserRepository userRepository;
private  final RoleRepository roleRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        UsersEntity user=userRepository.findByEmail(oidcUser.getEmail()).orElseGet(()->registerUser(oidcUser));
        user.setFirstName(oidcUser.getGivenName());
        user.setLastName(oidcUser.getFamilyName());
        userRepository.flush();
        String base = user.getFirstName().substring(0, 1)
                + user.getLastName();
        base = base.replaceAll("\\s+", "").toLowerCase();
        user.setUsername(base + "#" + user.getId());

        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Transactional
    protected UsersEntity registerUser(OidcUser oidcUser) {
        RolesEntity userRole = roleRepository.findByName("USER").orElseGet(() -> {
            RolesEntity newRole = new RolesEntity();
            newRole.setName("USER");
            newRole.setDescription("Default UserRole");
            return roleRepository.save(newRole);
        });
        UsersEntity user=new UsersEntity();
        user.setEmail(oidcUser.getEmail());
        user.setUsername(oidcUser.getEmail());
        user.setEnabled(true);
        user.setAccount_locked(false);
        user.setAccount_expired(false);
        user.setCredentials_expired(false);
        user.setRoles(Collections.singleton(userRole));
        UsersEntity u = userRepository.save(user);
        return u;
    }
}
