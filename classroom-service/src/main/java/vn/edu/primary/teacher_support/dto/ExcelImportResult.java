package vn.edu.primary.teacher_support.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelImportResult {

    private int total;
    private int invitedSuccess;
    private int waitingRegister;
    private int alreadyMember;
    private int alreadyInvited;
    private int invalidEmail;
    private int duplicateInFile;
    private List<String> invalidEmails;
    private List<String> errors;
}
