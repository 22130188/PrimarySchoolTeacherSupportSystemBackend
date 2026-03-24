package vn.edu.primary.teacher_support.dto;

import java.util.List;

public class UpdateProfileRequest {

    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String position;
    private String phone;

    private String schoolName;
    private String province;
    private String district;
    private String ward;

    private List<ClassItem> classes;

    private String currentPassword;
    private String newPassword;

    public static class ClassItem {
        private String grade;
        private String subject;
        public String getGrade()   { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
    }

    public String getFullName()    { return fullName; }
    public void setFullName(String v)  { this.fullName = v; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String v) { this.dateOfBirth = v; }
    public String getGender()      { return gender; }
    public void setGender(String v)    { this.gender = v; }
    public String getPosition()    { return position; }
    public void setPosition(String v)  { this.position = v; }
    public String getPhone()       { return phone; }
    public void setPhone(String v)     { this.phone = v; }
    public String getSchoolName()  { return schoolName; }
    public void setSchoolName(String v){ this.schoolName = v; }
    public String getProvince()    { return province; }
    public void setProvince(String v)  { this.province = v; }
    public String getDistrict()    { return district; }
    public void setDistrict(String v)  { this.district = v; }
    public String getWard()        { return ward; }
    public void setWard(String v)      { this.ward = v; }
    public List<ClassItem> getClasses(){ return classes; }
    public void setClasses(List<ClassItem> v){ this.classes = v; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String v){ this.currentPassword = v; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String v){ this.newPassword = v; }
}