package org.avni.report;

import org.avni.application.FormMapping;
import org.avni.domain.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AvniReportRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ReportHelper reportHelper;

    @Autowired
    public AvniReportRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                ReportHelper reportHelper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reportHelper = reportHelper;
    }

    public List<AggregateReportResult> generateAggregatesForCodedConcept(Concept concept, FormMapping formMapping, String startDate, String endDate, List<Long> lowestLocationIds) {
        String query = "with base_result as (\n" +
                "    select unnest(case\n" +
                "                      when jsonb_typeof(${obsColumn} -> ${conceptUUID}) = 'array'\n" +
                "                          then TRANSLATE((${obsColumn} -> ${conceptUUID})::jsonb::text, '[]', '{}')::TEXT[]\n" +
                "                      else ARRAY [${obsColumn} ->> ${conceptUUID}] end) as indicator,\n" +
                "           count(*)                                                     as count\n" +
                "    from ${dynamicFrom}\n" +
                "    where ${dynamicWhere}\n" +
                "    group by 1\n" +
                ")\n" +
                "select coalesce(concept_name(indicator), coalesce(indicator, 'Not answered')) indicator,\n" +
                "       count\n" +
                "from base_result";
        String queryWithConceptUUID = query.replace("${conceptUUID}", "'" + concept.getUuid() + "'");
        return jdbcTemplate.query(reportHelper.buildQuery(formMapping, queryWithConceptUUID, startDate, endDate, lowestLocationIds), new AggregateReportMapper());
    }

    public List<AggregateReportResult> generateAggregatesForEntityByType(String entity, String operationalType, String operationalTypeIdColumn, String dynamicWhere, String dynamicJoin) {
        String baseQuery = "select o.name as indicator,\n" +
                "       count(*) as count\n" +
                "from ${entity} e\n" +
                "         join ${operational_type} o on e.${operational_type_id} = o.${operational_type_id}\n" +
                "         ${dynamic_join}\n"+
                "where e.is_voided = false\n" +
                "  and o.is_voided = false\n" +
                "  ${dynamic_where}\n" +
                "group by o.name";
        String query = baseQuery
                .replace("${entity}", entity)
                .replace("${operational_type}", operationalType)
                .replace("${operational_type_id}", operationalTypeIdColumn)
                .replace("${dynamic_where}", dynamicWhere)
                .replace("${dynamic_join}", dynamicJoin);
        return jdbcTemplate.query(query, new AggregateReportMapper());
    }

    public List<CountForDay> generateDayWiseActivities(String dynamic_individual_where, String dynamic_encounter_where, String dynamic_program_enrolment_where, String dynamic_individual_join, String dynamic_encounter_join, String dynamic_enrolment_encounter_join) {
        String baseQuery = "select date for_date, sum(count) activity_count\n" +
                "from (select registration_date date, count(*) count\n" +
                "      from individual e\n" +
                "               join operational_subject_type o on o.subject_type_id = e.subject_type_id\n" +
                "               ${dynamic_individual_join}\n"+
                "      where e.is_voided is false\n" +
                "         ${dynamic_individual_where}\n" +
                "      group by registration_date\n" +
                "      union all\n" +
                "      select date(encounter_date_time) date, count(*) count\n" +
                "      from encounter e\n" +
                "               join operational_encounter_type o on o.encounter_type_id = e.encounter_type_id\n" +
                "               ${dynamic_enrolment_encounter_join}\n"+
                "      where encounter_date_time is not null\n" +
                "        and e.is_voided is false\n" +
                "         ${dynamic_encounter_where}\n" +
                "      group by date(encounter_date_time)\n" +
                "      union all\n" +
                "      select date(encounter_date_time) date, count(*) count\n" +
                "      from program_encounter e\n" +
                "               join operational_encounter_type o on o.encounter_type_id = e.encounter_type_id\n" +
                "               ${dynamic_encounter_join}\n"+
                "      where encounter_date_time is not null\n" +
                "        and e.is_voided is false\n" +
                "         ${dynamic_encounter_where}\n" +
                "      group by date(encounter_date_time)\n" +
                "      union all\n" +
                "      select date(enrolment_date_time) date, count(*) count\n" +
                "      from program_enrolment e\n" +
                "               join operational_program o on o.program_id = e.program_id\n" +
                "               ${dynamic_enrolment_encounter_join}\n"+
                "      where enrolment_date_time is not null\n" +
                "        and e.is_voided is false\n" +
                "         ${dynamic_program_enrolment_where}\n" +
                "      group by date(enrolment_date_time)) data\n" +
                "group by for_date\n" +
                "order by for_date";
        String query = baseQuery
                .replace("${dynamic_individual_where}", dynamic_individual_where)
                .replace("${dynamic_encounter_where}", dynamic_encounter_where)
                .replace("${dynamic_program_enrolment_where}", dynamic_program_enrolment_where)
                .replace("${dynamic_individual_join}", dynamic_individual_join)
                .replace("${dynamic_encounter_join}", dynamic_encounter_join)
                .replace("${dynamic_enrolment_encounter_join}", dynamic_enrolment_encounter_join);
        return jdbcTemplate.query(query, new CountForDayMapper());
    }

}
