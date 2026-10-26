package com.meetswps.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ProfileDtos {

    private ProfileDtos() {
    }

    record UpdateProfileRequest(
            @NotBlank @Size(max = 50) String firstName,
            @NotNull @Past LocalDate birthDate,
            @NotNull Campus campus,
            @NotBlank @Size(max = 100) String program,
            @Min(1) @Max(6) int studyYear,
            @NotNull DegreeLevel degreeLevel,
            @Size(max = 500) String bio,
            @Size(max = 10) List<@NotBlank @Size(max = 40) String> interests,
            @Size(max = 6) List<@NotBlank @Size(max = 40) String> languages,
            @Size(max = 100) String instagram,
            @NotEmpty(message = "choose at least one thing you are looking for") Set<@NotNull Intent> intents,
            InternshipRole internshipRole,
            @Size(max = 100) String industry,
            @Size(max = 100) String workplace) {
    }

    public record PhotoResponse(UUID id, int position) {

        public static PhotoResponse from(Photo photo) {
            return new PhotoResponse(photo.getId(), photo.getPosition());
        }
    }

    /** The student's own profile, with every field. */
    record MyProfileResponse(UUID userId, String firstName, LocalDate birthDate, int age, Campus campus, String program,
                             int studyYear, DegreeLevel degreeLevel, String bio, List<String> interests,
                             List<String> languages, String instagram, Set<Intent> intents,
                             InternshipRole internshipRole, String industry, String workplace, boolean hidden,
                             List<PhotoResponse> photos) {
    }

    record ReorderPhotosRequest(@NotEmpty List<@NotNull UUID> photoIds) {
    }
}
