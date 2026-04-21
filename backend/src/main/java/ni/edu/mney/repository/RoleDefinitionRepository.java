package ni.edu.mney.repository;

import java.util.Collection;
import java.util.List;
import ni.edu.mney.domain.RoleDefinition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDefinitionRepository extends JpaRepository<RoleDefinition, String> {
    @Override
    @EntityGraph(attributePaths = { "permissions", "compositeRoles" })
    List<RoleDefinition> findAll();

    @EntityGraph(attributePaths = { "permissions", "compositeRoles" })
    List<RoleDefinition> findAllByRoleNameIn(Collection<String> roleNames);
}
