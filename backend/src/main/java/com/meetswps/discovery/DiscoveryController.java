package com.meetswps.discovery;

import com.meetswps.auth.CurrentUser;
import com.meetswps.connection.ProfileCard;
import com.meetswps.profile.Campus;
import com.meetswps.profile.Intent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
class DiscoveryController {

    private final DiscoveryService discovery;

    DiscoveryController(DiscoveryService discovery) {
        this.discovery = discovery;
    }

    @GetMapping("/api/discover")
    List<ProfileCard> discover(CurrentUser user, @RequestParam Intent intent,
                               @RequestParam(required = false) Campus campus,
                               @RequestParam(required = false) String program,
                               @RequestParam(required = false) String interest,
                               @RequestParam(defaultValue = "0") int page) {
        return discovery.discover(user.id(), intent, new DiscoveryService.Filters(campus, program, interest), page);
    }

    @GetMapping("/api/profiles/{userId}")
    ProfileCard profile(CurrentUser user, @PathVariable UUID userId) {
        return discovery.profile(user.id(), userId);
    }
}
