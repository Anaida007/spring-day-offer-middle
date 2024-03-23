package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import com.onedayoffer.taskdistribution.DTO.TaskStatus;
import com.onedayoffer.taskdistribution.repositories.EmployeeRepository;
import com.onedayoffer.taskdistribution.repositories.TaskRepository;
import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import com.onedayoffer.taskdistribution.repositories.entities.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDTO> getEmployees(@Nullable String sortDirection) {
        List<Employee> employees;
        if (sortDirection == null || sortDirection.isEmpty()) {
            employees = employeeRepository.findAll();
        } else {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            employees = employeeRepository.findAllAndSort(Sort.by(direction, "fio"));
        }
        Type listType = new TypeToken<List<EmployeeDTO>>() {}.getType();
        return modelMapper.map(employees, listType);
    }

    @Transactional
    public EmployeeDTO getOneEmployee(Integer id) {
        EmployeeDTO employeeDTO = null;
        var employee = employeeRepository.findById(id);
        if (employee.isEmpty()) {
            log.error("There is no employee with id=" + id);
        } else {
            employeeDTO = modelMapper.map(employee.get(), EmployeeDTO.class);
        }
        return employeeDTO;
    }

    public List<TaskDTO> getTasksByEmployeeId(Integer id) {
        return getOneEmployee(id).getTasks();
    }

    @Transactional
    public void changeTaskStatus(Integer taskId, TaskStatus status) {
        var task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            log.error("There is no task with id=" + taskId);
        } else {
            var taskEntity = task.get();
            taskEntity.setStatus(status);
            taskRepository.saveAndFlush(taskEntity);
        }
    }

    @Transactional
    public void postNewTask(Integer employeeId, TaskDTO newTask) {
        var employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            log.error("There is no employee with id=" + employeeId + " for new task");
        } else {
            Task task = modelMapper.map(newTask, Task.class);
            taskRepository.save(task);
            var employeeEntity = employee.get();
            employeeEntity.addTask(task);
            employeeRepository.saveAndFlush(employeeEntity);
        }
    }
}
