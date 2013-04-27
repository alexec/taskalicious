package com.alexecollins.taskalicious.events;

import com.alexecollins.taskalicious.Tasks;
import com.alexecollins.taskalicious.Task;
import com.alexecollins.taskalicious.Tasks;
import lombok.Data;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Data
public class TaskAddedEvent {
	private final Tasks tasks;
	private final Task task;
}
