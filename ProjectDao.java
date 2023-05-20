package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

//Method class used JDBC to perform CRUD on the project tables.
public class ProjectDao extends DaoBase {
  private static final String CATEGORY_TABLE = "category";
  private static final String MATERIAL_TABLE = "material";
  private static final String PROJECT_TABLE = "project";
  private static final String PROJECT_CATEGORY_TABLE = "project_category";
  private static final String STEP_TABLE = "step";
  
  public void executeBatch(List<String> sqlBatch) {
    
  }//end of main method

//  Method inserts project row into the project table.
//  Establishes a DbConnection and starts a transaction
//  If unsuccessful will throw an DbException error
  public Project insertProject(Project project) {
    // @formatter:off
    String sql = ""
        + "INSERT INTO " + PROJECT_TABLE + " "
        + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
        + "VALUES "
        + "(?, ?, ?, ?, ? )";
        
    try(Connection conn = DbConnection.getConnection()) { 
      startTransaction(conn);
      
      try(PreparedStatement stmt = conn.prepareStatement(sql)){
        setParameter(stmt, 1, project.getProjectName(), String.class);
        setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
        setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
        setParameter(stmt, 4, project.getDifficulty(), Integer.class);
        setParameter(stmt, 5, project.getNotes(), String.class);
        
        stmt.executeUpdate();
        
        Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
        commitTransaction(conn);
        project.setProjectId(projectId);
        return project; 
        
      }
      catch(Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    }
    
    catch(SQLException e){
      throw new DbException(e);  
    }
  }

  //method prints out MySQL tables and parameters of tables by prepared statement
  public List<Project> fetchAllProjects() {
    String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
    
   try(Connection conn = DbConnection.getConnection()){
     startTransaction(conn);
     
     try(PreparedStatement stmt = conn.prepareStatement(sql)){
       try(ResultSet rs = stmt.executeQuery()){
         
         List<Project> projects = new LinkedList<>();
         
         while(rs.next()) {
           projects.add(extract(rs,Project.class));
           
           /** Alternative way to loop through result set. 
            * 
            * while(rs.next()){
            *   Project project = new Project();
            *   
            *   project.setActualHours(rs.getBigDecimal("actual_hours"));
            *   project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
            *   project.setDifficulty(rs.getObject("difficulty", Integer.class));
            *   project.setNotes(rs.getString("notes"));
            *   project.setProjectId(rs.getObject("project_id", Integer.class));
            *   project.setProjectName(rs.getString("project_name"));
            *   
            *   project.add(project);
            */
         }
         return projects;
       }
      
     }
     
     catch(Exception e) {
       rollbackTransaction(conn);
       throw new DbException(e);
     }
     
   } catch (SQLException e) {
    throw new DbException(e);
  }
 
  }

  public Optional<Project> fetchProjectById(Integer projectId) {
    
    String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";
       
   try(Connection conn = DbConnection.getConnection()){
     startTransaction(conn);
     
     try {
      Project project = null;
      
      try(PreparedStatement stmt = conn.prepareStatement(sql)){
        setParameter(stmt, 1, projectId, Integer.class);
        
        try(ResultSet rs = stmt.executeQuery()){
          if(rs.next()) {
            project = extract(rs, Project.class);
          }
        }
      }
      if(Objects.nonNull(project)) {
        project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
        project.getSteps().addAll(fetchStepsForProject(conn, projectId));
        project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
      }
      
      commitTransaction(conn);
      return Optional.ofNullable(project);
    }
    catch(Exception e) {
      rollbackTransaction(conn);
      throw new DbException(e);
    }
   
  }
  catch(SQLException e) {
    throw new DbException(e);
   
    }
  }
  
  private List<Category> fetchCategoriesForProject(Connection conn,Integer projectId) 
      throws SQLException {
//    @formatter: off
    String sql = ""
         + "SELECT c.* FROM " + CATEGORY_TABLE + " c "
         + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
         + "WHERE project_id = ?"; 
//    @formatter: on
    
    try(PreparedStatement stmt = conn.prepareStatement(sql)){
      setParameter(stmt, 1, projectId, Integer.class);
      
      try(ResultSet rs = stmt.executeQuery()){
        List<Category> categories = new LinkedList<>();
        
        while(rs.next()) {
          categories.add(extract(rs,Category.class));
        }
        return categories;
      }
    }
  }
  
  private List<Step> fetchStepsForProject(Connection conn, Integer projectId)
    throws Exception {
    
    String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
    
    try(PreparedStatement stmt = conn.prepareStatement(sql)){
      setParameter(stmt, 1, projectId, Integer.class);
      
      try(ResultSet rs = stmt.executeQuery()){
        List<Step> steps = new LinkedList<>();
        
        while(rs.next()) {
          steps.add(extract(rs,Step.class));
        }
    return steps;
      }
    }
  }

  private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId)
    throws SQLException {
    
    String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
    
    try(PreparedStatement stmt = conn.prepareStatement(sql)){
      setParameter(stmt, 1, projectId, Integer.class);
      
      try(ResultSet rs = stmt.executeQuery()){
        List<Material> materials = new LinkedList<>();
        
        while(rs.next()) {
          materials.add(extract(rs,Material.class));
      
      }
      return materials;
      }
    }
  }
 
  public boolean modifyProjectDetails(Project project) {
    // @formatter:off  
    String sql = " "
        + "Update " + PROJECT_TABLE + " SET "
        + "project_name = ?, "
        + "estimated_hours = ?, "
        + "actual_hours = ?, "
        + "difficulty = ?, "
        + "notes = ? "
        + "WHERE project_id = ?"; 
    // @formatter:on
    
    try(Connection conn = DbConnection.getConnection()){
      startTransaction(conn);
      
      try(PreparedStatement stmt = conn.prepareStatement(sql)){
       setParameter(stmt, 1, project.getProjectName(), String.class);
       setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
       setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
       setParameter(stmt, 4, project.getDifficulty(), Integer.class);
       setParameter(stmt, 5, project.getNotes(), String.class);
       setParameter(stmt, 6, project.getProjectId(), Integer.class);
       
       boolean modified = stmt.executeUpdate() == 1; 
       commitTransaction(conn);
       return modified;
      }
      catch(Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
      
    } catch (SQLException e) {
        throw new DbException(e);
    }
   
  }

  public boolean deleteProject(Integer projectId) {
   String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
   
   try(Connection conn = DbConnection.getConnection()){
     startTransaction(conn);
     
     try(PreparedStatement stmt = conn.prepareStatement(sql)){
       setParameter(stmt, 1, projectId, Integer.class);
       
       boolean deleted = stmt.executeUpdate() == 1; 
       
       commitTransaction(conn);
       return deleted;
       }
     
     catch(DbException e) {
       rollbackTransaction(conn);
         throw new DbException(e);      
     }
   } 
   catch (SQLException e) {
     throw new DbException(e);
  
  }
    
  }
}//end of main class
