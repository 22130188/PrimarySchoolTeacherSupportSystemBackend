package vn.edu.primary.teacher_support.dto;

public class LoginResponse {

    private final String token;
    private final Integer roleId;
    private final String roleName;

    public LoginResponse(String token, Integer roleId, String roleName) {
        this.token = token;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public String getToken() {
        return token;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }
}
