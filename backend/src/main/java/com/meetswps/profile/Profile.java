package com.meetswps.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Campus campus;

    @Column(nullable = false, length = 100)
    private String program;

    @Column(name = "study_year", nullable = false)
    private int studyYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree_level", nullable = false, length = 20)
    private DegreeLevel degreeLevel;

    @Column(length = 500)
    private String bio;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, length = 40)
    private List<String> interests = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, length = 40)
    private List<String> languages = new ArrayList<>();

    @Column(length = 30)
    private String instagram;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, length = 20)
    private List<String> intents = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "internship_role", length = 20)
    private InternshipRole internshipRole;

    @Column(length = 100)
    private String industry;

    @Column(length = 100)
    private String workplace;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "interested_in", nullable = false, length = 20)
    private List<String> interestedIn = new ArrayList<>();

    @Column(nullable = false)
    private boolean hidden;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Profile() {
    }

    Profile(UUID userId) {
        this.userId = userId;
    }

    /** Replaces every editable field at once; the caller has validated the combination. */
    void update(LocalDate birthDate, Campus campus, String program, int studyYear, DegreeLevel degreeLevel, String bio,
                List<String> interests, List<String> languages, String instagram, Set<Intent> intents,
                InternshipRole internshipRole, String industry, String workplace, Gender gender,
                Set<Gender> interestedIn) {
        this.birthDate = birthDate;
        this.campus = campus;
        this.program = program;
        this.studyYear = studyYear;
        this.degreeLevel = degreeLevel;
        this.bio = bio;
        this.interests = new ArrayList<>(interests);
        this.languages = new ArrayList<>(languages);
        this.instagram = instagram;
        this.intents = names(intents);
        // Section-specific answers are dropped when the student leaves that section, so they are never shown later.
        boolean internship = intents.contains(Intent.INTERNSHIP);
        this.internshipRole = internship ? internshipRole : null;
        this.industry = internship ? industry : null;
        this.workplace = internship ? workplace : null;
        boolean dating = intents.contains(Intent.DATING);
        this.gender = dating ? gender : null;
        this.interestedIn = dating ? names(interestedIn) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public int getAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public Campus getCampus() {
        return campus;
    }

    public String getProgram() {
        return program;
    }

    public int getStudyYear() {
        return studyYear;
    }

    public DegreeLevel getDegreeLevel() {
        return degreeLevel;
    }

    public String getBio() {
        return bio;
    }

    public List<String> getInterests() {
        return List.copyOf(interests);
    }

    public List<String> getLanguages() {
        return List.copyOf(languages);
    }

    public String getInstagram() {
        return instagram;
    }

    public Set<Intent> getIntents() {
        Set<Intent> result = EnumSet.noneOf(Intent.class);
        intents.forEach(name -> result.add(Intent.valueOf(name)));
        return result;
    }

    public InternshipRole getInternshipRole() {
        return internshipRole;
    }

    public String getIndustry() {
        return industry;
    }

    public String getWorkplace() {
        return workplace;
    }

    public Gender getGender() {
        return gender;
    }

    public Set<Gender> getInterestedIn() {
        Set<Gender> result = EnumSet.noneOf(Gender.class);
        interestedIn.forEach(name -> result.add(Gender.valueOf(name)));
        return result;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    private static List<String> names(Collection<? extends Enum<?>> values) {
        return new ArrayList<>(values.stream().map(Enum::name).sorted().toList());
    }
}
