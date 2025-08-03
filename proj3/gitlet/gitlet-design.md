# Gitlet Design Document
author: David Thai

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely
format and style a text file. Organize your design document in a way that
will make it easy for you or a course-staff member to read.

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### Main
Takes in arguments in forms of multiple strings. Will call a helper function
that determines whether operands/arguments are valid. Essentially calls whatever
command is appropriate when given input and the correct arguments.

Will use switch and cases to check input arguments from the String input to
check which commands the user desires to use (init, add, commit, rm, log,
global-log, find, status, checkout, branch, rm-branch, reset, merge), as well
as handle errors with specified error messages. Makes commit directories as
necessary based on commands.

### Commit Directory Class
Constructor called upon from Main and sets up persistence within the .gitlet
file. Will keep track of different commits and their associated data such as 
names, timestamp, message, parent, contents.
Map commits accordingly using a Tree Map, which is stored in a global array
of TreeMaps within the Main class. Commit contents will also be mapped in 
their own respective TreeMaps.

### Staging Area
Uses TreeMap and an ArrayList to keep track of added and removed files. As 
its name suggests, it stores what gitlet will stage and will thus be used in 
conjunction with commit commands. Will maintain persistence as the object 
created by calling the StagingArea method within this class will be stored 
within a directory.

### Repo 
Where the meat of the project will occur. Contains all methods that 
correspond to the gitlet commands that are supposed to implemented. Will set 
up directories and files that will set up persistence. The data structures 
that will be used are simply the ones that have already been mentioned 
through the other classes (TreeMaps & a bit of ArrayLists).

#### Fields:

##### Main:
None. Main simply executes appropriate commands. 

##### Commit Class:
name = String form of the name of the commit, which will be sha1 applied to 
the commit object.
message = Simple String type that holds a commit message.
timestamp = String that holds the timestamp of which a commit was created.
parent = String associated with the name of the parent of the commit that is 
being made.
contents = A TreeMap containing the contents, the files, tracked by a commit.

##### Staging Area
added = A TreeMap of files that have been staged for addition. The file's key 
will 
be the name of the file and its sha1 code will be the value in this tree.
removed = An ArrayList containing names of the files staged for removal. 
Does not contain sha1 codes like ADDED does because removing a file only 
requires the name in my implementation. 

##### Repo
CWD = The current working directory. A directory in which .gitlet will soon 
initialize itself in.
REPO = The initialized gitlet directory within the CWD.
head = File in REPO that essentially acts a pointer to a commit.
SA = Directory that stores the object of StagingArea().
BRANCHES = Directory containing all branches within REPO.
master = A file pointing at the initialized commit. A part of BRANCHES.
COMMITS = A directory that stores all the commit objects ever made.
BLOBS = A directory of files that are being tracked in the CWD.

## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

  * Checking if a merge is necessary.
  * Determining which files (if any) have a conflict.
  * Representing the conflict in the file.

* Try to clearly mark titles or names of classes with white space or
  some other symbols.

### Main:
setUpPersistence: Called each time main method is called (whenever some string
command is passed). Makes main directory if haven't already (mkdir). Can also
be called by other helper function and use mkdir for individual commit
directories that are created. Uses makefile for blobs and
set them into individual commit directories. Calls insert commit
TreeMap into list whenever new directory needs tracking

### Commit:
setUpPersistence: puts lower folders/blobs into its directory, which could be done
using mkdir and make file (alternatively can be called onto a relatively lower
class in terms of directories -> blobs)

updateMap: updates new commits into TreeMap through different keys of commits
generated from SHA-1 Hash Code (Util)

commitTraverse: can go back to previous commits/branches through calling of
TreeMap's parents and left/right.

### Blob:
setUpPersistence: called upon by Commit's function of the same name, sets
onto .gitlet/*whatever directory taken in as argument*/SHA-1 hash code.

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
  `java gitlet.Main add wug.txt`,
  on the next execution of
  `java gitlet.Main commit -m “modify wug.txt”`,
  the correct commit will be made.

* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.

* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

### Answer:
Persistence is set up by the method setPersistence set up in the Main class or
called upon other helper methods that call setPersistence. Each helper methods
should call upon individual classes (commit directories, blobs) to create
persistence by being (e.g .gitlet joined with directory name, .gitlet/*dir name*
joined with the SHA-1 hash value (sha1 Util method). Checking differences
between commits could use Diff function.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to
visualize the structure and workflow of your program.