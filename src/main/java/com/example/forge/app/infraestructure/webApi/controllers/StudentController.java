package com.example.forge.app.infraestructure.webApi.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.forge.app.application.services.StudentService;
import com.example.forge.app.domain.entities.StudentEntity;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/student")
public class StudentController {
	@Autowired
	private StudentService studentService;

	@GetMapping
	public ResponseEntity<Map<String, Object>> getAllStudents() {
		List<StudentEntity> students = studentService.getAll();

		String responseMessage = students.isEmpty() ? "No existen estudiantes" : "Estudiantes encontrados";
		HttpStatus responseStatus = students.isEmpty() ? HttpStatus.NOT_FOUND : HttpStatus.OK;

		Map<String, Object> response = new HashMap<>();
		response.put("success", responseStatus == HttpStatus.OK);
		response.put("msg", responseMessage);
		response.put("data", students);

		return ResponseEntity.status(responseStatus).body(response);
	}

	@PostMapping
	public ResponseEntity<Object> createStudent(@Valid @RequestBody StudentEntity student, BindingResult bindingResult) {
		if (studentService.isEmailDuplicated(student.getEmail())) {
			bindingResult.rejectValue("email", "duplicate", "El email ya está en uso");
		}

		if (studentService.isDniDuplicated(student.getDni())) {
			bindingResult.rejectValue("dni", "duplicate", "El DNI ya está en uso");
		}

		if (bindingResult.hasErrors()) {
			List<String> errorMessages = bindingResult.getFieldErrors().stream()
					.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
					.collect(Collectors.toList());

			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("msg", "Errores de validación");
			response.put("errors", errorMessages);

			return ResponseEntity.badRequest().body(response);
		}

		StudentEntity createdStudent = studentService.create(student);
		if (createdStudent != null) {
			return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/check-email/{email}")
	public ResponseEntity<Boolean> checkEmailDuplicate(@PathVariable String email) {
		boolean isDuplicated = studentService.isEmailDuplicated(email);
		return ResponseEntity.ok(isDuplicated);
	}

	@GetMapping("/check-dni/{dni}")
	public ResponseEntity<Boolean> checkDniDuplicate(@PathVariable String dni) {
		boolean isDuplicated = studentService.isDniDuplicated(dni);
		return ResponseEntity.ok(isDuplicated);
	}

	@GetMapping("{id}")
	public ResponseEntity<?> getStudent(@PathVariable Long id) {
		StudentEntity student = studentService.getById(id);

		if (student != null) {
			return ResponseEntity.ok().body(student);
		} else {
			Map<String, Object> msg = new HashMap<>();
			msg.put("success", Boolean.FALSE);
			msg.put("msg", String.format("No se encontró el estudiante con ID %d", id));
			msg.put("status", HttpStatus.NOT_FOUND.value());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
		}
	}

	@PutMapping("{id}")
	public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody StudentEntity student) {
		StudentEntity updatedStudent = studentService.updateById(id, student);

		if (updatedStudent != null) {
			return ResponseEntity.ok().body(updatedStudent);
		} else {
			Map<String, Object> msg = new HashMap<>();
			msg.put("success", Boolean.FALSE);
			msg.put("msg", String.format("No se pudo actualizar el estudiante con ID %d", id));
			msg.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}
	}
	//
	@DeleteMapping("{id}")
	public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
		boolean deleted = studentService.deleteById(id);

		if (deleted) {
			Map<String, Object> msg = new HashMap<>();
			msg.put("success", Boolean.TRUE);
			msg.put("msg", String.format("Estudiante con ID %d eliminado exitosamente", id));
			msg.put("status", HttpStatus.OK.value());
			return ResponseEntity.ok().body(msg);
		} else {
			Map<String, Object> msg = new HashMap<>();
			msg.put("success", Boolean.FALSE);
			msg.put("msg", String.format("No se pudo eliminar el estudiante con ID %d", id));
			msg.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
		}
	}
}
