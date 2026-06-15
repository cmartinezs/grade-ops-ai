package cl.gradeops.ai.api.auth;

import cl.gradeops.ai.api.security.AuthenticatedTeacher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResult result = authService.register(request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(new RegisterResponse(result.uid()));
    }

    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut() {
        AuthenticatedTeacher teacher = (AuthenticatedTeacher)
            SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authService.signOut(teacher.uid());
    }
}
