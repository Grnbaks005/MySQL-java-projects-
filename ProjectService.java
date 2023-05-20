package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
  private static final String PROJECT_TABLE = null;
  private static ProjectDao projectDao = new ProjectDao();
    
//Method calls DAO class to insert a project row.
  public Project addProject(Project project) {
    
    return projectDao.insertProject(project);
  }
//Method calls Project DAO to get project rows.
  public List<Project> fetchAllProjects() {
    
    return projectDao.fetchAllProjects();
  }
//Method calls on Project DAO to get project materials, steps and categories. 
  public Project fetchProjectById(Integer projectId) {
    return projectDao.fetchProjectById(projectId).orElseThrow(() -> new NoSuchElementException("Project with project ID=" + projectId + " does not exist."));
  }
  
  //Method calls project DAO, if invalid input is inserted will throw DbException and error. 
    public void deleteProject(Integer projectId) {
      if(!projectDao.deleteProject(projectId)){
        throw new DbException("Project with ID=" + projectId + "does not exist.");
      }
    }
    // Method calls project DAO, if invalid input is inserted will throw DbException and error. 
  public void modifyProjectDetails(Project project) {
   if(!projectDao.modifyProjectDetails(project)) {
     throw new DbException("Project with ID=" + project.getProjectId() + "does not exist.");
   }
  }

}//end of class.
