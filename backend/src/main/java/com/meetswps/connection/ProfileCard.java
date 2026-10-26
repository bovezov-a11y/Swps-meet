package com.meetswps.connection;

import com.meetswps.profile.Campus;
import com.meetswps.profile.DegreeLevel;
import com.meetswps.profile.Intent;
import com.meetswps.profile.InternshipRole;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Another student as the viewer is allowed to see them. Internship details appear only when both are in the
 * internship section, and the Instagram username only once they have matched.
 */
public record ProfileCard(UUID userId, String firstName, int age, Campus campus, String program, int studyYear,
                          DegreeLevel degreeLevel, String bio, List<String> interests, List<String> languages,
                          List<UUID> photoIds, Set<Intent> sharedIntents, InternshipRole internshipRole,
                          String industry, String workplace, Set<Intent> likedIn, UUID matchId, String instagram) {
}
