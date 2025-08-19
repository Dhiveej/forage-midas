package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /balance?userId=123
     * Return the user’s balance if found, else 0.
     */
    @GetMapping
    public Balance getBalance(@RequestParam("userId") Long userId) {
        UserRecord user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Cast double balance to float since your Balance DTO uses float
            return new Balance((float) user.getBalance());
        } else {
            return new Balance(0f);
        }
    }
}
