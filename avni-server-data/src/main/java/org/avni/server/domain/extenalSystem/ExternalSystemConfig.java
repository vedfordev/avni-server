package org.avni.server.domain.extenalSystem;

import org.avni.server.domain.JsonObject;
import org.avni.server.domain.OrganisationAwareEntity;
import org.avni.server.framework.hibernate.JSONObjectUserType;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity(name="external_system_config")
public class ExternalSystemConfig extends OrganisationAwareEntity {

    @NotNull
    @Column(name = "system_name")
    @Enumerated(value = EnumType.STRING)
    private SystemName systemName;

    @NotNull
    @Column
    @Type(value = JSONObjectUserType.class)
    private JsonObject config;

    public SystemName getSystemName() {
        return systemName;
    }

    public void setSystemName(SystemName systemName) {
        this.systemName = systemName;
    }

    public JsonObject getConfig() {
        return config;
    }

    public void setConfig(JsonObject config) {
        this.config = config;
    }
}
