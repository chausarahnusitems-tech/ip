# Chausistant UI test plan

## Project setup

- Working directory: repository root
- Java version: Java 25
- Compile command: `javac --release 25 -d <temporary-classes> src/main/java/Chausistant.java`
- Run command: `java -cp <temporary-classes> Chausistant`
- Each test case starts a fresh program session and sends its inputs in order.
- The program prints a startup banner. These tests use `Match: contains`, so
  each expected-output block must appear verbatim in the complete console
  output while the banner remains visible in the transcript.
- Testing stops at the first failed case.

## Test case: Add a todo task

Aim: Verify that a `todo` command creates a todo task and reports the updated task count.

Match: contains

### Inputs

```text
todo borrow book
bye
```

### Expected output

```text
Got it. I've added this task:
[T][ ] borrow book
Now you have 1 tasks in the list.
Bye. Hope to see you again soon!
```

## Test case: Add a deadline with punctuation

Aim: Verify that a deadline keeps its complete `/by` text, including punctuation and spaces.

Match: contains

### Inputs

```text
deadline do homework /by no idea :-p
bye
```

### Expected output

```text
Got it. I've added this task:
[D][ ] do homework (by: no idea :-p)
Now you have 1 tasks in the list.
Bye. Hope to see you again soon!
```

## Test case: Add and mark an event

Aim: Verify event parsing, list formatting, inheritance-based type formatting, and marking a task complete.

Match: contains

### Inputs

```text
event project meeting /from Mon 2pm /to 4pm
list
mark 1
bye
```

### Expected output

```text
Got it. I've added this task:
[E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 1 tasks in the list.
Here are the tasks in your list:
1.[E][ ] project meeting (from: Mon 2pm to: 4pm)
[E][X] project meeting (from: Mon 2pm to: 4pm)
Bye. Hope to see you again soon!
```

## Test case: Handle invalid commands with ChausistantException

Aim: Verify that invalid user inputs show a clear error message and the program continues accepting commands.

Match: contains

### Inputs

```text
todo
blah
deadline study
event meeting /from Mon
todo submit assignment
mark
mark one
mark 2
unmark
list now
bye later
bye
```

### Expected output

```text
Oops! The todo command needs a description.
Oops! I don't know the command "blah".
Oops! Use: deadline <task> /by <date or time>.
Oops! Use: event <task> /from <start> /to <end>.
Got it. I've added this task:
[T][ ] submit assignment
Now you have 1 tasks in the list.
Oops! The mark command needs a task number.
Oops! A task number must be a whole number.
Oops! There is no task numbered 2.
Oops! The unmark command needs a task number.
Oops! The list command does not take extra text.
Oops! The bye command does not take extra text.
Bye. Hope to see you again soon!
```

## Test case: Keep only valid tasks after malformed task commands

Aim: Verify that malformed task commands between valid commands do not add tasks to the list.

Match: contains

### Inputs

```text
todo read chapter
todo
deadline plan trip /by Friday
deadline write report
event club meeting /from 2pm /to 3pm
event study session /from 4pm /to
blah
list
bye
```

### Expected output

```text
Got it. I've added this task:
[T][ ] read chapter
Now you have 1 tasks in the list.
Oops! The todo command needs a description.
Got it. I've added this task:
[D][ ] plan trip (by: Friday)
Now you have 2 tasks in the list.
Oops! Use: deadline <task> /by <date or time>.
Got it. I've added this task:
[E][ ] club meeting (from: 2pm to: 3pm)
Now you have 3 tasks in the list.
Oops! Use: event <task> /from <start> /to <end>.
Oops! I don't know the command "blah".
Here are the tasks in your list:
1.[T][ ] read chapter
2.[D][ ] plan trip (by: Friday)
3.[E][ ] club meeting (from: 2pm to: 3pm)
Bye. Hope to see you again soon!
```

## Test case: Preserve task statuses after invalid mark commands

Aim: Verify that invalid mark and unmark commands do not alter a task's completion status.

Match: contains

### Inputs

```text
todo first task
todo second task
mark 2
mark 0
unmark 3
unmark two
unmark 2
mark 1 trailing
list
bye
```

### Expected output

```text
Got it. I've added this task:
[T][ ] first task
Now you have 1 tasks in the list.
Got it. I've added this task:
[T][ ] second task
Now you have 2 tasks in the list.
[T][X] second task
Oops! There is no task numbered 0.
Oops! There is no task numbered 3.
Oops! A task number must be a whole number.
[T][ ] second task
Oops! A task number must be a whole number.
Here are the tasks in your list:
1.[T][ ] first task
2.[T][ ] second task
Bye. Hope to see you again soon!
```

## Test case: Continue after invalid list and bye commands

Aim: Verify that command matching is case-insensitive and invalid list or bye commands neither change state nor exit the program.

Match: contains

### Inputs

```text
TODO  buy milk
LIST details
BYE now
list
bye
```

### Expected output

```text
Got it. I've added this task:
[T][ ] buy milk
Now you have 1 tasks in the list.
Oops! The list command does not take extra text.
Oops! The bye command does not take extra text.
Here are the tasks in your list:
1.[T][ ] buy milk
Bye. Hope to see you again soon!
```
