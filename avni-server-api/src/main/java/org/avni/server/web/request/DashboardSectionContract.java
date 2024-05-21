package org.avni.server.web.request;

import org.avni.server.domain.DashboardSection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardSectionContract extends CHSRequest {
    private String name;
    private String description;
    private String viewType;
    private Double displayOrder;
    private String dashboardUUID;
    private List<CardContract> cards = new ArrayList<>();
    private List<DashboardSectionCardMappingContract> dashboardSectionCardMappings = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CardContract> getCards() {
        return cards;
    }

    public void setCards(List<CardContract> cards) {
        this.cards = cards;
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getDashboardUUID() {
        return dashboardUUID;
    }

    public void setDashboardUUID(String dashboardUUID) {
        this.dashboardUUID = dashboardUUID;
    }

    public List<DashboardSectionCardMappingContract> getDashboardSectionCardMappings() {
        return dashboardSectionCardMappings;
    }

    public void setDashboardSectionCardMappings(List<DashboardSectionCardMappingContract> dashboardSectionCardMappings) {
        this.dashboardSectionCardMappings = dashboardSectionCardMappings;
    }

}
