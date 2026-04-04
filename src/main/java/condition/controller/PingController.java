package condition.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;

@AllArgsConstructor
@RestController
@RequestMapping("/ping")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PingController {

    private final EntityManager entityManager;

    @GetMapping("/200")
    public ResponseEntity<Integer> ping() {
        Integer rowCount = (Integer) entityManager.createNativeQuery("SELECT 1").getSingleResult();
        return ResponseEntity.ok(rowCount);
    }
}
