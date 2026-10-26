package com.meetswps.profile;

import com.meetswps.common.InvalidRequestException;
import com.meetswps.common.NotFoundException;
import com.meetswps.profile.ProfileDtos.MyProfileResponse;
import com.meetswps.profile.ProfileDtos.PhotoResponse;
import com.meetswps.profile.ProfileDtos.UpdateProfileRequest;
import com.meetswps.user.User;
import com.meetswps.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
class ProfileService {

    static final int MINIMUM_AGE = 18;

    // Accepts "name", "@name" and profile links such as https://www.instagram.com/name/?hl=pl
    private static final Pattern INSTAGRAM = Pattern.compile(
            "^(?:https?://)?(?:www\\.)?(?:instagram\\.com/)?@?([A-Za-z0-9._]{1,30})/?(?:\\?.*)?$");

    private final ProfileRepository profiles;
    private final PhotoRepository photos;
    private final UserRepository users;

    ProfileService(ProfileRepository profiles, PhotoRepository photos, UserRepository users) {
        this.profiles = profiles;
        this.photos = photos;
        this.users = users;
    }

    @Transactional(readOnly = true)
    MyProfileResponse get(UUID userId) {
        Profile profile = profiles.findById(userId)
                .orElseThrow(() -> new NotFoundException("You have not created your profile yet"));
        return toResponse(user(userId), profile);
    }

    /** Creates the profile on the first call (the end of onboarding) and replaces it afterwards. */
    @Transactional
    MyProfileResponse save(UUID userId, UpdateProfileRequest request) {
        if (Period.between(request.birthDate(), LocalDate.now()).getYears() < MINIMUM_AGE) {
            throw new InvalidRequestException("birthDate", "you must be at least " + MINIMUM_AGE + " to use Meet SWPS");
        }
        Set<Intent> intents = EnumSet.copyOf(request.intents());
        if (intents.contains(Intent.INTERNSHIP) && request.internshipRole() == null) {
            throw new InvalidRequestException("internshipRole",
                    "tell others whether you are looking for an internship or can help with one");
        }
        Set<Gender> interestedIn = request.interestedIn() == null || request.interestedIn().isEmpty()
                ? EnumSet.noneOf(Gender.class) : EnumSet.copyOf(request.interestedIn());
        if (intents.contains(Intent.DATING)) {
            if (request.gender() == null) {
                throw new InvalidRequestException("gender", "choose your gender to use the dating section");
            }
            if (interestedIn.isEmpty()) {
                throw new InvalidRequestException("interestedIn", "choose who you would like to meet");
            }
        }

        User user = user(userId);
        user.rename(request.firstName().strip());
        Profile profile = profiles.findById(userId).orElseGet(() -> new Profile(userId));
        profile.update(request.birthDate(), request.campus(), request.program().strip(), request.studyYear(),
                request.degreeLevel(), blankToNull(request.bio()), tags(request.interests()),
                tags(request.languages()), instagramUsername(request.instagram()), intents,
                request.internshipRole(), blankToNull(request.industry()), blankToNull(request.workplace()),
                request.gender(), interestedIn);
        profiles.save(profile);
        return toResponse(user, profile);
    }

    static String instagramUsername(String input) {
        String value = blankToNull(input);
        if (value == null) {
            return null;
        }
        Matcher matcher = INSTAGRAM.matcher(value);
        if (!matcher.matches()) {
            throw new InvalidRequestException("instagram", "enter your Instagram username or profile link");
        }
        return matcher.group(1).toLowerCase(Locale.ROOT);
    }

    /** Trimmed, without blanks or duplicates (ignoring case), in the order given. */
    private static List<String> tags(Collection<String> values) {
        if (values == null) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        return values.stream().map(String::strip).filter(value -> !value.isEmpty())
                .filter(value -> seen.add(value.toLowerCase(Locale.ROOT))).toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private User user(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private MyProfileResponse toResponse(User user, Profile p) {
        List<PhotoResponse> photoList = photos.findAllByUserIdOrderByPosition(user.getId()).stream()
                .map(PhotoResponse::from).toList();
        return new MyProfileResponse(user.getId(), user.getFirstName(), p.getBirthDate(), p.getAge(), p.getCampus(),
                p.getProgram(), p.getStudyYear(), p.getDegreeLevel(), p.getBio(), p.getInterests(), p.getLanguages(),
                p.getInstagram(), p.getIntents(), p.getInternshipRole(), p.getIndustry(), p.getWorkplace(),
                p.getGender(), p.getInterestedIn(), p.isHidden(), photoList);
    }
}
