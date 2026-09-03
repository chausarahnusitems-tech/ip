package chausistant.command;

import chausistant.storage.Storage;
import chausistant.task.DeadlineTask;
import chausistant.task.EventTask;
import chausistant.task.Task;
import chausistant.task.TaskList;
import chausistant.ui.Ui;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Command that shows events and deadlines relevant to one calendar date. */
public class WhatsOnCommand extends Command {
    private final LocalDate date;

    /** Creates a command that displays work scheduled on the supplied date. */
    public WhatsOnCommand(LocalDate date) {
        this.date = date;
    }

    /** Displays events and deadlines that occur on the requested date. */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ArrayList<EventTask> events = new ArrayList<>();
        ArrayList<DeadlineTask> deadlines = new ArrayList<>();

        for (Task task : tasks.getTasks()) {
            if (task instanceof EventTask event
                    && !event.getFrom().toLocalDate().isAfter(date)
                    && !event.getTo().toLocalDate().isBefore(date)) {
                events.add(event);
            } else if (task instanceof DeadlineTask deadline
                    && deadline.getDeadline().toLocalDate().equals(date)) {
                deadlines.add(deadline);
            }
        }

        events.sort(Comparator.comparing(EventTask::getFrom));
        deadlines.sort(Comparator.comparing(DeadlineTask::getDeadline));

        List<String> eventDetails = events.stream().map(EventTask::printTask).toList();
        List<String> deadlineDetails = deadlines.stream().map(DeadlineTask::printTask).toList();
        ui.showSchedule(Task.formatDateForDisplay(date), eventDetails, deadlineDetails);
    }
}
