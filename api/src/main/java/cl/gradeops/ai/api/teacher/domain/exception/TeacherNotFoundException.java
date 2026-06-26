package cl.gradeops.ai.api.teacher.domain.exception;

import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;

public class TeacherNotFoundException extends ResourceNotFoundException {
    public TeacherNotFoundException(String uid) {
        super("Teacher not found: " + uid);
    }
}
