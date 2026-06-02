package com.medvault.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "personal_details")
public class PersonalDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(length = 10)
    private String gender;

    @Column(length = 15)
    private String phone;

    @Column(name = "profile_photo_path", length = 500)
    private String profilePhotoPath;

    // ── Constructors ──────────────────────────────────────────────────────
    public PersonalDetails() {}

    public PersonalDetails(Long id, User user, String firstName, String lastName,
                           LocalDate dob, String gender, String phone, String profilePhotoPath) {
        this.id = id; this.user = user; this.firstName = firstName;
        this.lastName = lastName; this.dob = dob; this.gender = gender;
        this.phone = phone; this.profilePhotoPath = profilePhotoPath;
    }

    // ── Builder ───────────────────────────────────────────────────────────
    public static PersonalDetailsBuilder builder() { return new PersonalDetailsBuilder(); }

    public static class PersonalDetailsBuilder {
        private final PersonalDetails pd = new PersonalDetails();
        public PersonalDetailsBuilder user(User v)              { pd.user = v;             return this; }
        public PersonalDetailsBuilder firstName(String v)       { pd.firstName = v;        return this; }
        public PersonalDetailsBuilder lastName(String v)        { pd.lastName = v;         return this; }
        public PersonalDetailsBuilder dob(LocalDate v)          { pd.dob = v;              return this; }
        public PersonalDetailsBuilder gender(String v)          { pd.gender = v;           return this; }
        public PersonalDetailsBuilder phone(String v)           { pd.phone = v;            return this; }
        public PersonalDetailsBuilder profilePhotoPath(String v){ pd.profilePhotoPath = v; return this; }
        public PersonalDetails build() { return pd; }
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public Long      getId()               { return id; }
    public User      getUser()             { return user; }
    public String    getFirstName()        { return firstName; }
    public String    getLastName()         { return lastName; }
    public LocalDate getDob()              { return dob; }
    public String    getGender()           { return gender; }
    public String    getPhone()            { return phone; }
    public String    getProfilePhotoPath() { return profilePhotoPath; }

    // ── Setters ───────────────────────────────────────────────────────────
    public void setId(Long v)                { this.id = v; }
    public void setUser(User v)              { this.user = v; }
    public void setFirstName(String v)       { this.firstName = v; }
    public void setLastName(String v)        { this.lastName = v; }
    public void setDob(LocalDate v)          { this.dob = v; }
    public void setGender(String v)          { this.gender = v; }
    public void setPhone(String v)           { this.phone = v; }
    public void setProfilePhotoPath(String v){ this.profilePhotoPath = v; }
}