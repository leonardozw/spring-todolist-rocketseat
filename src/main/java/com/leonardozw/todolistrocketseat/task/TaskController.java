package com.leonardozw.todolistrocketseat.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }
    
    @PostMapping
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        var id = request.getAttribute("userId");
        taskModel.setUserId((UUID) id);

        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(400).body("Start/End date must be after current date");
        }

        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(400).body("Task start date must be before task end date");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(200).body(taskModel);
    }

    @GetMapping
    public List<TaskModel> list(HttpServletRequest request){
        var id = request.getAttribute("userId");
        return this.taskRepository.findByUserId((UUID) id);
    }

    //@PutMapping("{id}")
    @PutMapping
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request/* , @PathVariable UUID id*/){

        /* Decided not to use this due to redundancy reasons
         * var task = this.taskRepository.findById(id).orElseThrow(()-> new RuntimeException("Task not found"));
         * 
         * Utils.copyNonNullProperties(taskModel, task);
         * 
         * return this.taskRepository.save(task);
         */

        var id = request.getAttribute("userId");
        if(id.toString().equals(taskModel.getUserId().toString())){
            return ResponseEntity.status(200).body(this.taskRepository.save(taskModel));
        }else{
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

}
