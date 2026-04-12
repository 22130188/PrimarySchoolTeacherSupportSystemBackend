package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.primary.teacher_support.dto.ExcelImportResult;
import vn.edu.primary.teacher_support.entity.Classroom;
import vn.edu.primary.teacher_support.exception.BusinessException;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private final ClassroomService classroomService;
    private final InvitationService invitationService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Transactional
    public ExcelImportResult importExcel(Long classroomId, MultipartFile file, Long teacherId) {
        Classroom classroom = classroomService.getActiveClassroom(classroomId);

        if (file.isEmpty()) {
            throw new BusinessException("File không được để trống");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException("Chỉ hỗ trợ file Excel (.xlsx, .xls)");
        }

        List<String> emails = new ArrayList<>();
        List<String> invalidEmails = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenEmails = new HashSet<>();

        int total = 0;
        int invitedSuccess = 0;
        int waitingRegister = 0;
        int alreadyMember = 0;
        int alreadyInvited = 0;
        int invalidEmail = 0;
        int duplicateInFile = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cell = row.getCell(0);
                if (cell == null) continue;

                String email = getCellStringValue(cell).trim().toLowerCase();
                if (email.isEmpty()) continue;

                total++;

                if (!EMAIL_PATTERN.matcher(email).matches()) {
                    invalidEmail++;
                    invalidEmails.add(email);
                    continue;
                }

                if (seenEmails.contains(email)) {
                    duplicateInFile++;
                    continue;
                }
                seenEmails.add(email);

                try {
                    String result = invitationService.inviteByEmailForBatch(
                            classroomId, classroom, email, teacherId);

                    switch (result) {
                        case "invited_success" -> invitedSuccess++;
                        case "waiting_register" -> waitingRegister++;
                        case "already_member" -> alreadyMember++;
                        case "already_invited" -> alreadyInvited++;
                        case "invalid_role" -> {
                            invalidEmail++;
                            invalidEmails.add(email + " (không phải tài khoản học sinh)");
                        }
                        default -> errors.add("Email " + email + ": " + result);
                    }
                } catch (Exception e) {
                    errors.add("Email " + email + ": " + e.getMessage());
                    log.error("Error processing email {} in Excel import: {}", email, e.getMessage());
                }
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Lỗi đọc file Excel: " + e.getMessage());
        }

        log.info("Excel import for classroom {}: total={}, success={}, waiting={}, member={}, invited={}, invalid={}, duplicate={}",
                classroomId, total, invitedSuccess, waitingRegister, alreadyMember, alreadyInvited, invalidEmail, duplicateInFile);

        return ExcelImportResult.builder()
                .total(total)
                .invitedSuccess(invitedSuccess)
                .waitingRegister(waitingRegister)
                .alreadyMember(alreadyMember)
                .alreadyInvited(alreadyInvited)
                .invalidEmail(invalidEmail)
                .duplicateInFile(duplicateInFile)
                .invalidEmails(invalidEmails)
                .errors(errors)
                .build();
    }

    private String getCellStringValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return "";
    }
}
