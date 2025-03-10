package org.avni.server.domain.metabase;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Dashcard {
    @JsonProperty("id")
    private int id;

    @JsonProperty("card_id")
    private int cardId;

    @JsonProperty("dashboard_tab_id")
    private Integer dashboardTabId;

    private int row;
    private int col;

    @JsonProperty("size_x")
    private int sizeX;

    @JsonProperty("size_y")
    private int sizeY;

    @JsonProperty("visualization_settings")
    private Object visualizationSettings;

    @JsonProperty("parameter_mappings")
    private List<ParameterMapping> parameterMappings;

    public Dashcard(int id, int cardId, Integer dashboardTabId, int row, int col, int sizeX, int sizeY, Object visualizationSettings, List<ParameterMapping> parameterMappings) {
        this.id = id;
        this.cardId = cardId;
        this.dashboardTabId = dashboardTabId;
        this.row = row;
        this.col = col;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.visualizationSettings = visualizationSettings;
        this.parameterMappings = parameterMappings;
    }

    public int getId() {
        return id;
    }

    public int getCardId() {
        return cardId;
    }

    public Integer getDashboardTabId() {
        return dashboardTabId;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", cardId=" + cardId +
                ", dashboardTabId=" + dashboardTabId +
                ", row=" + row +
                ", col=" + col +
                ", sizeX=" + sizeX +
                ", sizeY=" + sizeY +
                ", visualizationSettings=" + visualizationSettings +
                ", parameterMappings=" + parameterMappings +
                '}';
    }
}
