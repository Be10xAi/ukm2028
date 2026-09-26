package com.tech10x.ukm.repositoryproxy;

import com.tech10x.ukm.entity.Users;
import com.tech10x.ukm.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepository {

    private  final UsersRepository usersRepository;

    public Optional<Users> findByUserId (String userId)
    {
        return usersRepository.findByUserId(userId);
    }
    public Users save (Users users)
    {
        return usersRepository.save(users);
    }
}
