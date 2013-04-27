package com.alexecollins.taskalicious.events;

import com.alexecollins.taskalicious.Task;
import lombok.Data;

/**
 * @author alexec (alex.e.c@gmail.com)
 */
@Data
public class TaskChanged {
	private final Task task;
}
