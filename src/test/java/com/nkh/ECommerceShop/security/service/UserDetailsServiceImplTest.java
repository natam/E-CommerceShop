package com.nkh.ECommerceShop.security.service;

import com.nkh.ECommerceShop.model.Role;
import com.nkh.ECommerceShop.model.Users;
import com.nkh.ECommerceShop.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {
    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private UserDetailsServiceImpl usersService;

    @Test
    void returnUserDetailsIfUserWithUsernameExists() {
        String email = "test@test.com";
        Users user = new Users("test", email, "pwTest123", Role.USER);
        Mockito.when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));
        UserDetails receivedUser = usersService.loadUserByUsername(email);
        assertNotNull(receivedUser);
        assertEquals(email, receivedUser.getUsername());
        assertEquals("pwTest123", receivedUser.getPassword());
        assertEquals(List.of(new SimpleGrantedAuthority(Role.USER.name())), receivedUser.getAuthorities());
    }

    @Test
    void whenUserNotExistsThrowException() {
        String email = "test@test.com";
        Mockito.when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> usersService.loadUserByUsername(email));
        assertEquals("Bad credentials", exception.getMessage());
    }
}