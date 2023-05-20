package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import javax.print.PrintService;
import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

/**
 * This class will perform CRUD operations by utilizing user input from the console
 * 
 * @author rusmo
 *
 */
public class ProjectsApp {
  private Scanner scanner = new Scanner(System.in);
  private ProjectService projectService = new ProjectService();
  private Project curProject;

  // @formatter:off
    private List<String> operations = List.of(
        "1) Add a project",
        "2) List a project",
        "3) Select a project",
        "4) Update a project",
        "5) Delete a project"
    );
    // @formatter:on

  public static void main(String[] args) {
    new ProjectsApp().processUserSelections();
  }

  // end main method

  // Method prints out operations, gets user selection iterates through until users exits.
  private void processUserSelections() {
    boolean done = false;

    while (!done) {
      try {
        int selection = getUserSelection();

        switch (selection) {
          case -1:
            done = exitMenu();
            break;

          case 1:
            createProject();
            break;

          case 2:
            listProjects();
            break;

          case 3:
            selectProject();
            break;

          case 4:
            updateProjectDetails();
            break;

          case 5:
            deleteProject();
            break;


          default:
            System.out.println("\n" + selection + " is not a valid selection. Try again.");

        }
      } catch (Exception e) {
        System.out.println("\nError: " + e + " Try again.");
        // e.printStackTrace();
      }
    }
  }

  // method shows prompt "update a project" and takes user input.
  private void updateProjectDetails() {
    if (Objects.isNull(curProject)) {
      System.out.println("\nPlease select a project.");
      return;
    }
    String projectName =
        getStringInput("Enter the project name [" + curProject.getProjectName() + "]");

    BigDecimal estimatedHours =
        getDecimalInput("Enter the estimated hours [" + curProject.getEstimatedHours() + "]");

    BigDecimal actualHours =
        getDecimalInput("Enter the actual hours [" + curProject.getActualHours() + "]");

    Integer difficulty =
        getIntInput("Enter the project difficulty (1-5)[" + curProject.getDifficulty() + "]");

    String notes = ("Enter the project notes [" + curProject.getNotes() + "]");

    Project project = new Project();
    project.setProjectId(curProject.getProjectId());

    project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
    project.setEstimatedHours(
        Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
    project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
    project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
    project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);

    projectService.modifyProjectDetails(project);
    curProject = projectService.fetchProjectById(curProject.getProjectId());

  }

  // method shows prompt "Delete a project" and takes in user input.
  private void deleteProject() {
    listProjects();

    Integer projectId = getIntInput("Enter a project ID to delete");
    if (Objects.nonNull(projectId)) {
      projectService.deleteProject(projectId);
      System.out.println("Project" + projectId + " was successfully deleted.");

      if (Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
        curProject = null;
      }
    }
  }

  // Method prints out select project prompt and will take in user input
  private void selectProject() {
    listProjects();
    Integer projectId = getIntInput("Enter a project ID to select a project");
    // Un-select the current project in case exception is thrown.
    curProject = null;

    curProject = projectService.fetchProjectById(projectId);

  }

  // Method prints out List a project prompt and will take in user input
  private void listProjects() {
    List<Project> projects = projectService.fetchAllProjects();

    System.out.println("\nProjects:");
    // prints out project ID and project name for each recipe.
    projects.forEach(project -> System.out
        .println("  " + project.getProjectId() + ": " + project.getProjectName()));
  }

  // Method collects user input from row, then calls project service to create row.
  private void createProject() {
    String projectName = getStringInput("Enter the project name");
    BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
    BigDecimal actualHours = getDecimalInput("Enter the actual hours");
    Integer difficulty = getIntInput("Enter the project difficulty (1-5)"); // code to validate the
                                                                            // input from user?
    String notes = getStringInput("Enter the project notes");

    Project project = new Project();

    project.setProjectName(projectName);
    project.setEstimatedHours(estimatedHours);
    project.setActualHours(actualHours);
    project.setDifficulty(difficulty);
    project.setNotes(notes);

    Project dbProject = projectService.addProject(project);
    System.out.println("You have successfully created project: " + dbProject);
  }

  // Method will take users input and convert to BigDecimal if successful.
  // If unsuccessful will throw exception.
  private BigDecimal getDecimalInput(String prompt) {
    String input = getStringInput(prompt);

    if (Objects.isNull(input)) {
      return null;
    }

    try {
      return new BigDecimal(input).setScale(2);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid decimal number.");
    }
  }

  // Method will take user input and exit menu.
  private boolean exitMenu() {
    System.out.println("Exiting the menu.");
    return true;
  }

  // Method will take user input and throw exception if input is not a valid number.
  private int getUserSelection() {
    printOperations();
    Integer input = getIntInput("Enter a menu selection");

    return Objects.isNull(input) ? -1 : input;
  }

  private Integer getIntInput(String prompt) {
    String input = getStringInput(prompt);

    if (Objects.isNull(input)) {
      return null;
    }

    try {
      return Integer.valueOf(input);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid number.");
    }
  }

  // Method will take users input from console and print to console if inputs valid number.
  // Otherwise if number is not valid or null will be returned.
  private String getStringInput(String prompt) {
    System.out.print(prompt + ": ");
    String input = scanner.nextLine();

    return input.isBlank() ? null : input.trim();
  }

  // Method will print out menu selections, one per line.
  private void printOperations() {
    System.out.println("\nThese are the available selections. Press the enter key to quit:");

    operations.forEach(line -> System.out.println("  " + line));

    if (Objects.isNull(curProject)) {
      System.out.println("\nYou are not working with a project.");
    } else {
      System.out.println("\nYou are working with project: " + curProject);
    }
  }
}// end main class
