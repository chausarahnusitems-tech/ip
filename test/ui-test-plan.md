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
