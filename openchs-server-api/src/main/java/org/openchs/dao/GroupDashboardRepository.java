package org.openchs.dao;

import org.openchs.domain.GroupDashboard;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(collectionResourceRel = "groupDashboard", path = "groupDashboard")
@PreAuthorize("hasAnyAuthority('user','admin','organisation_admin')")
public interface GroupDashboardRepository extends ReferenceDataRepository<GroupDashboard>, FindByLastModifiedDateTime<GroupDashboard> {

    default GroupDashboard findByName(String name) {
        throw new UnsupportedOperationException("No field 'name' in GroupDashboard");
    }

    default GroupDashboard findByNameIgnoreCase(String name) {
        throw new UnsupportedOperationException("No field 'name' in GroupDashboard");
    }

}
