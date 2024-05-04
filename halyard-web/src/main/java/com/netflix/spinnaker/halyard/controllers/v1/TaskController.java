package com.netflix.spinnaker.halyard.controllers.v1;

import com.google.longrunning.GetOperationRequest;
import com.google.longrunning.OperationsGrpc;
import com.netflix.spinnaker.halyard.config.model.v1.node.Halconfig;
import com.netflix.spinnaker.halyard.core.tasks.v1.DaemonTask;
import com.netflix.spinnaker.halyard.core.tasks.v1.ShallowTaskList;
import com.netflix.spinnaker.halyard.core.tasks.v1.TaskRepository;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import retrofit.http.Body;

@GRpcService
@RestController
@RequestMapping("/v1/tasks")
public class TaskController extends OperationsGrpc.OperationsImplBase {
  @GetMapping(value = "/{uuid:.+}/")
  DaemonTask<Halconfig, Void> getTask(@PathVariable String uuid) {
    return TaskRepository.getTask(uuid);
  }

  @PutMapping(value = "/{uuid:.+}/interrupt")
  void interruptTask(@PathVariable String uuid, @Body String ignored) {
    DaemonTask task = TaskRepository.getTask(uuid);

    if (task == null) {
      throw new TaskNotFoundException("No such task with UUID " + uuid);
    }

    task.interrupt();
  }

  @GetMapping(value = "/")
  ShallowTaskList getTasks() {
    return TaskRepository.getTasks();
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  public final class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String msg) {
      super(msg);
    }
  }

  @Override
  public void getOperation(GetOperationRequest request, io.grpc.stub.StreamObserver<com.google.longrunning.Operation> responseObserver) {
    responseObserver.onNext(TaskRepository.getTask(request.getName()).getLRO());
    responseObserver.onCompleted();
  }
}
