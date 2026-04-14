package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStats {

    private long totalClassrooms;
    private long totalDeletedClassrooms;
    private long totalMembers;
    private long totalPendingInvitations;
    private List<AdminClassroomResponse> recentClassrooms;
}
