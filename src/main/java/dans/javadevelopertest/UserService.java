package dans.javadevelopertest;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserDetailsServiceImpl userDetailsService;

    public UserService(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public boolean isValidUser(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new BCryptPasswordEncoder().matches(password, userDetails.getPassword());
    }
}