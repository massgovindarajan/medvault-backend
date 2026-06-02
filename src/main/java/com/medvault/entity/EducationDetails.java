package com.medvault.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "education_details")
public class EducationDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String degree;

    @Column(length = 200)
    private String institution;

    @Column(name = "year_of_completion")
    private Integer yearOfCompletion;

    @Column(name = "certificate_path", length = 500)
    private String certificatePath;

    // ── Constructors ──────────────────────────────────────────────────────
    public EducationDetails() {}

    public EducationDetails(Long id, User user, String degree, String institution,
                            Integer yearOfCompletion, String certificatePath) {
        this.id = id; this.user = user; this.degree = degree;
        this.institution = institution; this.yearOfCompletion = yearOfCompletion;
        this.certificatePath = certificatePath;
    }

    // ── Builder ───────────────────────────────────────────────────────────
    public static EducationDetailsBuilder builder() { return new EducationDetailsBuilder(); }

    public static class EducationDetailsBuilder {
        private final EducationDetails e = new EducationDetails();
        public EducationDetailsBuilder user(User v)              { e.user = v;             return this; }
        public EducationDetailsBuilder degree(String v)          { e.degree = v;           return this; }
        public EducationDetailsBuilder institution(String v)     { e.institution = v;      return this; }
        public EducationDetailsBuilder yearOfCompletion(Integer v){ e.yearOfCompletion = v; return this; }
        public EducationDetailsBuilder certificatePath(String v) { e.certificatePath = v;  return this; }
        public EducationDetails build() { return e; }
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public Long    getId()               { return id; }
    public User    getUser()             { return user; }
    public String  getDegree()           { return degree; }
    public String  getInstitution()      { return institution; }
    public Integer getYearOfCompletion() { return yearOfCompletion; }
    public String  getCertificatePath()  { return certificatePath; }

    // ── Setters ───────────────────────────────────────────────────────────
    public void setId(Long v)                { this.id = v; }
    public void setUser(User v)              { this.user = v; }
    public void setDegree(String v)          { this.degree = v; }
    public void setInstitution(String v)     { this.institution = v; }
    public void setYearOfCompletion(Integer v){ this.yearOfCompletion = v; }
    public void setCertificatePath(String v) { this.certificatePath = v; }
}