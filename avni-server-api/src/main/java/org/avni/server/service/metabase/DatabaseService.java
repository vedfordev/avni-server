package org.avni.server.service.metabase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.avni.server.dao.metabase.DatabaseRepository;
import org.avni.server.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DatabaseService {

    private final DatabaseRepository databaseRepository;
    private final ObjectMapper objectMapper;
    private final MetabaseService metabaseService;

    @Value("${metabase.api.url}")
    private String metabaseApiUrl;

    @Value("${metabase.api.key}")
    private String apiKey;

    @Autowired
    public DatabaseService(DatabaseRepository databaseRepository, ObjectMapper objectMapper, MetabaseService metabaseService) {
        this.databaseRepository = databaseRepository;
        this.objectMapper = objectMapper;
        this.metabaseService = metabaseService;
    }

    public int getTableIdByName(int databaseId, String tableName) {
        JsonNode rootNode = databaseRepository.getDatabaseDetails(databaseId);
        JsonNode tablesArray = rootNode.path("tables");
        for (JsonNode tableNode : tablesArray) {
            if (tableName.equals(tableNode.path("display_name").asText())) {
                return tableNode.path("id").asInt();
            }
        }
        return -1;
    }

    public int getFieldIdByTableNameAndFieldName(int databaseId, String tableName, String fieldName) {
        JsonNode fieldsArray = databaseRepository.getFields(databaseId);
        String snakeCaseTableName = StringUtils.toSnakeCase(tableName);
        for (JsonNode fieldNode : fieldsArray) {
            if (snakeCaseTableName.equals(fieldNode.path("table_name").asText()) && fieldName.equals(fieldNode.path("name").asText())) {
                return fieldNode.path("id").asInt();
            }
        }
        return -1;
    }

    public String getInitialSyncStatus(int databaseId) {
        JsonNode responseBody = databaseRepository.getInitialSyncStatus(databaseId);
        return responseBody.path("initial_sync_status").asText();
    }

    public List<String> getSubjectTypeNames(int databaseId) {
        int tableId = getTableIdByName(databaseId, "Subject Type");
        String requestBody = "{\"database\":" + databaseId + ",\"query\":{\"source-table\":" + tableId + "},\"type\":\"query\",\"parameters\":[]}";

        JsonNode response = databaseRepository.getDataset(requestBody);

        JsonNode dataNode = response.path("data");
        JsonNode rows = dataNode.path("rows");

        List<String> subjectTypeNames = new ArrayList<>();
        for (JsonNode row : rows) {
            String name = row.get(4).asText();
            boolean isVoided = row.get(6).asBoolean();
            if (!isVoided) {
                subjectTypeNames.add(name);
            }
        }
        return subjectTypeNames;
    }

    public void createQuestionsForSubjectTypes() {
        int databaseId = metabaseService.getGlobalDatabaseId();
        int collectionId = metabaseService.getGlobalCollectionId();

        String syncStatus = getInitialSyncStatus(databaseId);
        if (!"complete".equals(syncStatus)) {
            throw new RuntimeException("Database initial sync is not complete.");
        }

        List<String> subjectTypeNames = getSubjectTypeNames(databaseId);

        for (String subjectTypeName : subjectTypeNames) {
            createQuestionForTable(databaseId, collectionId, subjectTypeName, "Address", "id", "address_id");
        }
    }

    private List<String> getProgramNamesFromOperationalProgramsTable(int databaseId) {
        int operationalProgramsTableId = getTableIdByName(databaseId, "All Operational Programs");

        String requestBody = "{\"database\":" + databaseId + ",\"query\":{\"source-table\":" + operationalProgramsTableId + "},\"type\":\"query\",\"parameters\":[]}";
        JsonNode response = databaseRepository.getDataset(requestBody);

        List<String> programNames = new ArrayList<>();
        JsonNode rows = response.path("data").path("rows");
        for (JsonNode row : rows) {
            String programName = row.get(1).asText(); 
            programNames.add(programName);
        }
        return programNames;
    }

    private List<String> extractTableNames(JsonNode databaseDetails) {
        List<String> tableNames = new ArrayList<>();
        JsonNode tablesArray = databaseDetails.path("tables");
        for (JsonNode tableNode : tablesArray) {
            tableNames.add(tableNode.path("display_name").asText());
        }
        return tableNames;
    }

    private void createQuestionForTable(int databaseId, int collectionId, String tableName, String addressTableName, String addressField, String tableField) {
        int addressTableId = getTableIdByName(databaseId, addressTableName);
        int joinFieldId1 = getFieldIdByTableNameAndFieldName(databaseId, addressTableName, addressField);
        int tableId = getTableIdByName(databaseId, tableName);
        int joinFieldId2 = getFieldIdByTableNameAndFieldName(databaseId, tableName, tableField);

        ObjectNode datasetQuery = objectMapper.createObjectNode();
        datasetQuery.put("database", databaseId);
        datasetQuery.put("type", "query");

        ObjectNode query = objectMapper.createObjectNode();
        query.put("source-table", addressTableId);

        ArrayNode joins = objectMapper.createArrayNode();
        ObjectNode join = objectMapper.createObjectNode();
        join.put("fields", "all");
        join.put("alias", tableName);

        ArrayNode condition = objectMapper.createArrayNode();
        condition.add("=");
        condition.add(objectMapper.createArrayNode().add("field").add(joinFieldId1).add(objectMapper.createObjectNode().put("base-type", "type/Integer")));
        condition.add(objectMapper.createArrayNode().add("field").add(joinFieldId2).add(objectMapper.createObjectNode().put("base-type", "type/Integer").put("join-alias", tableName)));

        join.set("condition", condition);
        join.put("source-table", tableId);
        joins.add(join);

        query.set("joins", joins);
        datasetQuery.set("query", query);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", "Address + " + tableName);
        body.set("dataset_query", datasetQuery);
        body.put("display", "table");
        body.putNull("description");
        body.set("visualization_settings", objectMapper.createObjectNode());
        body.put("collection_id", collectionId);
        body.putNull("collection_position");
        body.putNull("result_metadata");

        databaseRepository.postForObject(metabaseApiUrl + "/card", body, JsonNode.class);
    }

    public void createQuestionsForProgramsAndEncounters() {
        int databaseId = metabaseService.getGlobalDatabaseId();
        int collectionId = metabaseService.getGlobalCollectionId();

        String syncStatus = getInitialSyncStatus(databaseId);
        if (!"complete".equals(syncStatus)) {
            throw new RuntimeException("Database initial sync is not complete.");
        }

        List<String> programNames = getProgramNamesFromOperationalProgramsTable(databaseId);

        JsonNode databaseDetails = databaseRepository.getDatabaseDetails(databaseId);
        List<String> allTableNames = extractTableNames(databaseDetails);

        for (String programName : programNames) {
            for (String tableName : allTableNames) {
                if (tableName.contains(programName)) {
                    createQuestionForTable(databaseId, collectionId, tableName, "Address", "id", "address_id");
                }
            }
        }
    }
}
